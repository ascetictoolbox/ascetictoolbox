package integratedtoolkit.loader.total;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.log4j.Logger;

import javassist.CannotCompileException;
//import javassist.CtClass;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;
import integratedtoolkit.api.COMPSsRuntime.DataDirection;
import integratedtoolkit.api.COMPSsRuntime.DataType;
import integratedtoolkit.loader.LoaderConstants;
import integratedtoolkit.loader.LoaderUtils;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.annotations.Parameter;
import integratedtoolkit.types.annotations.Parameter.Direction;
import integratedtoolkit.types.annotations.Service;


public class ITAppEditor extends ExprEditor {

    private Method[] remoteMethods;
    private CtMethod[] instrCandidates; // methods that will be instrumented if they are not remote 
    private String itApiVar;
    private String itSRVar;
    private String itORVar;
    private String itAppIdVar;
	private CtClass appClass;
	
	// Inserted method calls
	private static final String GET_INTERNAL_OBJECT = ".getInternalObject(";
	private static final String NEW_OBJECT_ACCESS 	= ".newObjectAccess(";
	private static final String SERIALIZE_LOCALLY 	= ".serializeLocally(";
	private static final String NEW_FILTER_STREAM   = ".newFilterStream(";
	private static final String STREAM_CLOSED 		= ".streamClosed(";
	private static final String GET_CANONICAL_PATH  = ".getCanonicalPath(";
	private static final String ADD_TASK_FILE       = ".addTaskFile(";
	private static final String IS_TASK_FILE 		= ".isTaskFile(";
	private static final String OPEN_FILE 			= ".openFile(";
	private static final String DELETE_FILE 		= ".deleteFile(";
	private static final String EXECUTE_TASK 		= ".executeTask(";
	private static final String PROCEED 			= "$_ = $proceed(";
	
	private static final String DATA_TYPES 				= DataType.class.getCanonicalName();
	private static final String DATA_DIRECTION 			= DataDirection.class.getCanonicalName();
	
	private static final String CHECK_SCO_TYPE 			= "LoaderUtils.checkSCOType(";
	private static final String CHECK_SCO_PERSISTENT 	= "LoaderUtils.checkSCOPersistent(";
	private static final String RUN_METHOD_ON_OBJECT 	= "LoaderUtils.runMethodOnObject(";
	
	// Logger
    private static final Logger logger = Logger.getLogger(Loggers.LOADER);
    private static final boolean debug = logger.isDebugEnabled();

    
    public ITAppEditor(Method[] remoteMethods, CtMethod[] instrCandidates, String itApiVar, String itSRVar, 
    		String itORVar, String itAppIdVar, CtClass appClass) {
    	
        super();
        this.remoteMethods = remoteMethods;
        this.instrCandidates = instrCandidates;
        this.itApiVar = itApiVar;
        this.itSRVar = itSRVar;
        this.itORVar = itORVar;
        this.itAppIdVar = itAppIdVar;
        this.appClass = appClass;
    }
    
    public CtClass getAppClass() {
    	return this.appClass;
    }
    
    /**
     * Instruments the creation of streams and stream wrappers
     * @param ne New expression
     */
    public void edit(NewExpr ne) throws CannotCompileException {
        String fullName = ne.getClassName();
        boolean isInternal = fullName.startsWith(LoaderConstants.LOADER_INTERNAL_PREFIX);
        boolean isIO = fullName.startsWith(LoaderConstants.LOADER_IO_PREFIX);

        // Only edit non-internal calls
        if (!isInternal) {
            StringBuilder modifiedExpr = new StringBuilder();
            StringBuilder callPars = new StringBuilder();
            StringBuilder toSerialize = new StringBuilder();
            try {
                CtClass[] paramTypes = ne.getConstructor().getParameterTypes();
                if (paramTypes.length > 0) {
                    int i = 1;
                    for (CtClass parType : paramTypes) {
                        if (i > 1) {
                            callPars.append(',');
                        }
                        String parId = "$" + (i++);
                        if (parType.isPrimitive()) {
                            callPars.append(parId);
                        } else { // Object (also array)
                            if (debug) {
                                logger.debug("Parameter " + (i - 1) + " of constructor " + ne.getConstructor() + " is an object, adding access");
                            }

                            String internalObject = itORVar + GET_INTERNAL_OBJECT + parId + ")";
                            modifiedExpr.insert(0, itORVar + NEW_OBJECT_ACCESS + parId + ");");
                            callPars.append(internalObject).append(" == null ? ").append(parId).append(" : ").append("(" + parType.getName() + ")").append(internalObject);
                            toSerialize.append(itORVar).append(SERIALIZE_LOCALLY).append(parId).append(");");
                        }
                    }
                }
            } catch (NotFoundException e) {
                throw new CannotCompileException(e);
            }

            if (isIO) {
                String className = fullName.substring(8);
                modifiedExpr.append( inspectCreation(className, callPars) );
            } else {
                modifiedExpr.append(PROCEED).append(callPars).append(");");
                modifiedExpr.append(toSerialize);
            }

            if (debug) {
                logger.debug("Replacing regular constructor call of class " + fullName + " by " + modifiedExpr.toString());
            }

            // Update new expression
            ne.replace(modifiedExpr.toString());
        }
    }
    
    /**
     * Class creation inspection
     * 
     * @param className
     * @param callPars
     * @return
     */
    private String inspectCreation(String className, StringBuilder callPars) {
    	String modifiedExpr = "";
    	
    	if (debug) {
            logger.debug("Inspecting the creation of an object of class " + className);
        }

        // $$ = pars separated by commas, $args = pars in an array of objects
    	boolean found = false;
    	for (String streamClass : LoaderConstants.SUPPORTED_STREAM_TYPES) {
    		if (className.equals(streamClass)) {
    			modifiedExpr = "$_ = " + itSRVar + ".new" + streamClass + "(" + callPars + ");";
    			found = true;
    			break;
    		}
    	}
    	if (!found) { // Not a stream
    		String internalObject = itORVar + GET_INTERNAL_OBJECT + "$1)";
            String par1 = internalObject + " == null ? (Object)$1 : " + internalObject;
            modifiedExpr = PROCEED + callPars + "); "
                    + "if ($_ instanceof " + FilterInputStream.class.getCanonicalName() 
                    + " || $_ instanceof " + FilterOutputStream.class.getCanonicalName() + ") {"
                    + itSRVar + NEW_FILTER_STREAM + par1 + ", (Object)$_); }";
    	}
        
        return modifiedExpr;
    }

    /**
     * Replaces calls to remote methods by calls to executeTask or black-boxes methods
     * 
     */
    public void edit(MethodCall mc) throws CannotCompileException {
        Method declaredMethod = null;
        CtMethod calledMethod = null;
        try {
            calledMethod = mc.getMethod();
            declaredMethod = LoaderUtils.checkRemote(calledMethod, remoteMethods);
        } catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }

        if (declaredMethod != null) { 
        	// Current method must be executed remotely, change the call
        	String executeTask = replaceLocalMethodCall(mc.getMethodName(), mc.getClassName(), declaredMethod, calledMethod);
        	
            if (debug) {
                logger.debug("Replacing local method call by: " + executeTask);
            }

            // Replace the call to the method by the call to executeTask
        	mc.replace(executeTask);
        } else if (LoaderUtils.isStreamClose(mc)) {
            if (debug) {
                logger.debug("Replacing close on a stream of class " + mc.getClassName());
            }
            
            // Close call on a stream
            // No need to instrument the stream object, assuming it will always be local
            String streamClose = replaceCloseStream();
            mc.replace(streamClose);
        } else if (LoaderUtils.isFileDelete(mc)) {
        	String deleteFile = replaceDeleteFile();
            mc.replace(deleteFile);
        } else if ( mc.getClassName().equals(LoaderConstants.CLASS_COMPSS_API) ) {
            // The method is an API call
        	// boolean isAPICall = mc.getClassName().equals(LoaderConstants.CLASS_COMPSS_API);
    		String modifiedAPICall = replaceAPICall(mc.getMethodName(), calledMethod);
    		if (debug) {
                logger.debug("Replacing API Call method call by " + modifiedAPICall);
            }
    		
    		mc.replace(modifiedAPICall);
        } else if ( !LoaderUtils.contains(instrCandidates, calledMethod) ) {
        	// The method is a black box
	        // boolean isBlackBox = !LoaderUtils.contains(instrCandidates, calledMethod);
            String modifiedCall = replaceBlackBox(mc.getMethodName(), mc.getClassName(), calledMethod);
            if (debug) {
                logger.debug("Replacing regular method call by " + modifiedCall);
            }

            mc.replace(modifiedCall);
        } else {
        	// The method is an instrumented method
        	// Nothing to do
        }
    }
    
    /**
     * Replaces calls to local methods by executeTask
     * 
     * @param methodName
     * @param className
     * @param declaredMethod
     * @param calledMethod
     * @return
     */
    private String replaceLocalMethodCall(String methodName, String className, Method declaredMethod, CtMethod calledMethod) {
        if (debug) {
            logger.debug("Found call to remote method " + methodName);
        }

        Class<?> retType = declaredMethod.getReturnType();
        boolean isVoid = retType.equals(void.class);
        boolean isStatic = Modifier.isStatic(calledMethod.getModifiers());
        boolean isMethod = declaredMethod.isAnnotationPresent(integratedtoolkit.types.annotations.Method.class);
        Class<?> paramTypes[] = declaredMethod.getParameterTypes();
        int numParams = paramTypes.length;
        if (!isStatic) {
            numParams++;
        }
        if (!isVoid) {
            numParams++;
        }
        Annotation[][] paramAnnot = declaredMethod.getParameterAnnotations();

        //	Build the executeTask call string
        StringBuilder executeTask = new StringBuilder();
        executeTask.append(itApiVar).append(EXECUTE_TASK);
        executeTask.append(itAppIdVar).append(',');

        if (isMethod) {
            integratedtoolkit.types.annotations.Method methodAnnot = declaredMethod.getAnnotation(integratedtoolkit.types.annotations.Method.class);
            executeTask.append("\"").append(className).append("\"").append(',');
            executeTask.append("\"").append(methodName).append("\"").append(',');
            executeTask.append(methodAnnot.priority()).append(',');
        } else { // Service
            Service serviceAnnot = declaredMethod.getAnnotation(Service.class);
            executeTask.append("\"").append(serviceAnnot.namespace()).append("\"").append(',');
            executeTask.append("\"").append(serviceAnnot.name()).append("\"").append(',');
            executeTask.append("\"").append(serviceAnnot.port()).append("\"").append(',');
            executeTask.append("\"").append(methodName).append("\"").append(',');
            executeTask.append(serviceAnnot.priority()).append(',');
        }

        executeTask.append(!isStatic).append(',');
        
        // Add parameters
        executeTask.append(numParams);
        if (numParams == 0) {
            executeTask.append(",null);");
        } else {
        	CallInformation callInformation = processParameters(declaredMethod, paramAnnot, paramTypes, isVoid, isStatic, isMethod, numParams, retType);
        	executeTask.append(callInformation.getToAppend());
        	executeTask.insert(0, callInformation.getToPrepend());
        }

        return executeTask.toString();
    }
    
    /**
     * Process the parameters, the target object and the return value of a given method
     * 
     * @param declaredMethod
     * @param paramAnnot
     * @param paramTypes
     * @param isVoid
     * @param isStatic
     * @param isMethod
     * @param numParams
     * @param retType
     * @return
     */
    private CallInformation processParameters(Method declaredMethod, Annotation[][] paramAnnot, Class<?>[] paramTypes, boolean isVoid, boolean isStatic, 
    		boolean isMethod, int numParams, Class<?> retType) {
    	
    	StringBuilder toAppend = new StringBuilder("");
    	StringBuilder toPrepend = new StringBuilder("");
    	
    	toAppend.append(",new Object[]{");
        // Add the actual parameters of the method
        for (int i = 0; i < paramAnnot.length; i++) {
            Class<?> formalType = paramTypes[i];
            Parameter.Type annotType = ((Parameter) paramAnnot[i][0]).type();

            /* Append the value of the current parameter according to the type.
             * Basic types must be wrapped by an object first
             */
            ParameterInformation infoParam = processParameterValue( i, formalType, annotType, ((Parameter) paramAnnot[i][0]).direction() );
            toAppend.append(infoParam.getToAppend());
            toPrepend.insert(0, infoParam.getToPrepend());
            toAppend.append(infoParam.getType()).append(",");
            toAppend.append(infoParam.getDirection());
            
            if (i < paramAnnot.length - 1) {
            	toAppend.append(",");
            }
        }

        // Add the target object of the call as an IN/INOUT parameter, for class methods
        String targetObject = processTargetObject(declaredMethod, isStatic, numParams, isVoid, isMethod);
        toAppend.append(targetObject);

        // Add the return value as an OUT parameter, if any
        ReturnInformation returnInfo = processReturnParameter(isVoid, numParams, retType);
        toAppend.append(returnInfo.getToAppend());
        toPrepend.insert(0, returnInfo.getToPrepend());
        toAppend.append("});");
        toAppend.append(returnInfo.getAfterExecution());
        

    	CallInformation callInformation = new CallInformation(toAppend.toString(), toPrepend.toString());
        return callInformation;
    }
    
    /**
     * Process the parameter values of a method call
     * 
     * @param paramIndex
     * @param formalType
     * @param annotType
     * @param paramDirection
     * @return
     */
    private ParameterInformation processParameterValue(int paramIndex, Class<?> formalType, Parameter.Type annotType, Direction paramDirection) {
    	StringBuilder infoToAppend = new StringBuilder("");
    	StringBuilder infoToPrepend = new StringBuilder("");
    	String type = "";
    	String direction = "";
    	
    	if (annotType.equals(Parameter.Type.FILE)) {
            // The File type needs to be specified explicitly, since its formal type is String
            type = DATA_TYPES + ".FILE_T";
            infoToAppend.append('$').append(paramIndex + 1).append(',');
            infoToPrepend.insert(0, itSRVar + ADD_TASK_FILE + "$" + (paramIndex + 1) + ");");
        } else if (annotType.equals(Parameter.Type.STRING)) {
            /* Mechanism to make a String be treated like a list of chars instead of like another object.
             * Dependencies won't be watched for the string.
             */
        	type = DATA_TYPES + ".STRING_T";
        	infoToAppend.append('$').append(paramIndex + 1).append(',');
        } else if (formalType.isPrimitive()) {
            if (formalType.equals(boolean.class)) {
            	type = DATA_TYPES + ".BOOLEAN_T";
                infoToAppend.append("new Boolean(").append("$").append(paramIndex + 1).append("),");
            } else if (formalType.equals(char.class)) {
            	type = DATA_TYPES + ".CHAR_T";
            	infoToAppend.append("new Character(").append("$").append(paramIndex + 1).append("),");
            } else if (formalType.equals(byte.class)) {
            	type = DATA_TYPES + ".BYTE_T";
            	infoToAppend.append("new Byte(").append("$").append(paramIndex + 1).append("),");
            } else if (formalType.equals(short.class)) {
            	type = DATA_TYPES + ".SHORT_T";
            	infoToAppend.append("new Short(").append("$").append(paramIndex + 1).append("),");
            } else if (formalType.equals(int.class)) {
            	type = DATA_TYPES + ".INT_T";
            	infoToAppend.append("new Integer(").append("$").append(paramIndex + 1).append("),");
            } else if (formalType.equals(long.class)) {
            	type = DATA_TYPES + ".LONG_T";
            	infoToAppend.append("new Long(").append("$").append(paramIndex + 1).append("),");
            } else if (formalType.equals(float.class)) {
            	type = DATA_TYPES + ".FLOAT_T";
            	infoToAppend.append("new Float(").append("$").append(paramIndex + 1).append("),");
            } else if (formalType.equals(double.class)) {
            	type = DATA_TYPES + ".DOUBLE_T";
            	infoToAppend.append("new Double(").append("$").append(paramIndex + 1).append("),");
            }
        } else { // Object or Self-Contained Object or Persistent SCO
        	type = CHECK_SCO_TYPE + "$" + (paramIndex + 1) + ")";
        	infoToAppend.append(CHECK_SCO_PERSISTENT).append("$").append(paramIndex + 1).append("),");
        }
    	
    	switch (paramDirection) {
	        case IN:
	            direction = DATA_DIRECTION +  ".IN";
	            break;
	        case OUT:
	            direction = DATA_DIRECTION +  ".OUT";
	            break;
	        case INOUT:
	            direction = DATA_DIRECTION +  ".INOUT";
	            break;
	        default: // null
	            direction = DATA_DIRECTION +  ".IN";
	            break;
	    }
    	
    	ParameterInformation infoParam = new ParameterInformation(infoToAppend.toString(), infoToPrepend.toString(), type, direction);
    	return infoParam;
    }
    
    /**
     * Process the target object of a given method call
     * 
     * @param declaredMethod
     * @param isStatic
     * @param numParams
     * @param isVoid
     * @param isMethod
     * @return
     */
    private String processTargetObject(Method declaredMethod, boolean isStatic, int numParams, boolean isVoid, boolean isMethod) {
    	StringBuilder targetObj = new StringBuilder("");
    	
    	if (!isStatic) {
            // Assuming object, it is unlikely that a user selects a method invoked on an array
    		int numRealParams = (isVoid ? numParams : numParams - 1);
            if (numRealParams > 1) {
            	targetObj.append(',');
            }
            targetObj.append(CHECK_SCO_PERSISTENT + "$0),").append(CHECK_SCO_TYPE + "$0)");
            // Check if the method will modify the target object (default yes)
            if (isMethod) {
                integratedtoolkit.types.annotations.Method methodAnnot = declaredMethod.getAnnotation(integratedtoolkit.types.annotations.Method.class);
                if (methodAnnot.isModifier()) {
                	targetObj.append(',').append(DATA_DIRECTION + ".INOUT");
                } else {
                	targetObj.append(',').append(DATA_DIRECTION + ".IN");
                }
            } else {// Service
            	targetObj.append(',').append(DATA_DIRECTION + ".INOUT");
            }
        }
    	
    	return targetObj.toString();
    }
    
    /**
     * Process the return parameter of a given method call
     * 
     * @param isVoid
     * @param numParams
     * @param retType
     * @return
     */
    private ReturnInformation processReturnParameter(boolean isVoid, int numParams, Class<?> retType) {
    	StringBuilder infoToAppend = new StringBuilder("");
    	StringBuilder infoToPrepend = new StringBuilder("");
    	StringBuilder afterExecute = new StringBuilder("");
    	
        if (!isVoid) {
            if (numParams > 1) {
            	infoToAppend.append(',');
            }

            String typeName = retType.getName();
            if (retType.isPrimitive()) {
            	/* *********************************
            	 * *********************************/
                String tempRetVar = "ret" + System.nanoTime();
                infoToAppend.append(tempRetVar).append(',').append(DATA_TYPES + ".OBJECT_T").append(',').append(DATA_DIRECTION + ".OUT");

                String retValueCreation = "Object " + tempRetVar + " = ";
                String cast;
                String converterMethod;
                if (typeName.equals(boolean.class.getName())) {
                    retValueCreation += "new Boolean(false);";
                    cast = "(Boolean)";
                    converterMethod = "booleanValue()";
                } else if (typeName.equals(char.class.getName())) {
                    retValueCreation += "new Character(Character.MIN_VALUE);";
                    cast = "(Character)";
                    converterMethod = "charValue()";
                } else if (typeName.equals(byte.class.getName())) {
                    retValueCreation += "new Byte(Byte.MIN_VALUE);";
                    cast = "(Byte)";
                    converterMethod = "byteValue()";
                } else if (typeName.equals(short.class.getName())) {
                    retValueCreation += "new Short(Short.MIN_VALUE);";
                    cast = "(Short)";
                    converterMethod = "shortValue()";
                } else if (typeName.equals(int.class.getName())) {
                    retValueCreation += "new Integer(Integer.MIN_VALUE);";
                    cast = "(Integer)";
                    converterMethod = "intValue()";
                } else if (typeName.equals(long.class.getName())) {
                    retValueCreation += "new Long(Long.MIN_VALUE);";
                    cast = "(Long)";
                    converterMethod = "longValue()";
                } else if (typeName.equals(float.class.getName())) {
                    retValueCreation += "new Float(Float.MIN_VALUE);";
                    cast = "(Float)";
                    converterMethod = "floatValue()";
                } else { // (typeName.equals(double.class.getName()))
                    retValueCreation += "new Double(Double.MIN_VALUE);";
                    cast = "(Double)";
                    converterMethod = "doubleValue()";
                }

                // Before paramsModified, declare and instance a temp wrapper object containing the primitive value
                infoToPrepend.insert(0, retValueCreation);

                /* After execute task, register an access to the wrapper object,
                 * get its (remotely) generated value and
                 * assign it to the application's primitive type var
                 */
                afterExecute.append(itORVar).append(NEW_OBJECT_ACCESS).append(tempRetVar).append(");");
                afterExecute.append("$_ = (").append(cast).append(itORVar).append(GET_INTERNAL_OBJECT)
                			.append(tempRetVar).append(")).").append(converterMethod).append(";");
            } else if (retType.isArray()) {
            	/* *********************************
            	 * ARRAY
            	 * *********************************/
                Class<?> compType = retType.getComponentType();
                int numDim = typeName.lastIndexOf('[');
                String dims = "[0]";
                while (numDim-- > 0) {
                    dims += "[]";
                }
                while (compType.getComponentType() != null) {
                    compType = compType.getComponentType();
                }
                String compTypeName = compType.getName();
                infoToPrepend.insert(0, "$_ = new " + compTypeName + dims + ';');
                infoToAppend.append("$_,").append(DATA_TYPES + ".OBJECT_T");
                infoToAppend.append(',').append(DATA_DIRECTION + ".OUT");
            } else {
            	/* *********************************
            	 * OBJECT
            	 * *********************************/
                // Wrapper for a primitive type: return a default value
                if (typeName.equals(Boolean.class.getName())) {
                	infoToPrepend.insert(0, "$_ = new Boolean(false);");
                } else if (typeName.equals(Character.class.getName())) {
                	infoToPrepend.insert(0, "$_ = new Character(Character.MIN_VALUE);");
                } else if (typeName.equals(Byte.class.getName())) {
                	infoToPrepend.insert(0, "$_ = new Byte(Byte.MIN_VALUE);");
                } else if (typeName.equals(Short.class.getName())) {
                	infoToPrepend.insert(0, "$_ = new Short(Short.MIN_VALUE);");
                } else if (typeName.equals(Integer.class.getName())) {
                	infoToPrepend.insert(0, "$_ = new Integer(Integer.MIN_VALUE);");
                } else if (typeName.equals(Long.class.getName())) {
                	infoToPrepend.insert(0, "$_ = new Long(Long.MIN_VALUE);");
                } else if (typeName.equals(Float.class.getName())) {
                	infoToPrepend.insert(0, "$_ = new Float(Float.MIN_VALUE);");
                } else if (typeName.equals(Double.class.getName())) {
                	infoToPrepend.insert(0, "$_ = new Double(Double.MIN_VALUE);");
                } // Object (maybe String): use the no-args constructor
                else {
                	infoToPrepend.insert(0, "$_ = new " + typeName + "();");
                }

                infoToAppend.append("$_,").append(CHECK_SCO_TYPE + "$_)");
                infoToAppend.append(',').append(DATA_DIRECTION + ".OUT"); 
            }
        }
        
        ReturnInformation returnInfo = new ReturnInformation(infoToAppend.toString(), infoToPrepend.toString(), afterExecute.toString());
        return returnInfo;
    }
    
    /**
     * Replaces the close stream call
     * 
     * @return
     */
    private String replaceCloseStream() {
    	String streamClose = PROCEED + "$$); " + itSRVar + STREAM_CLOSED + "$0);";
    	return streamClose;
    }
    
    /**
     * Replaces the delete file call
     * 
     * @return
     */
    private String replaceDeleteFile() {
    	String deleteFile = "$_ = " + itApiVar + DELETE_FILE + "$0" + GET_CANONICAL_PATH + "));";
    	return deleteFile;
    }
    
    /**
     * Replaces the API calls
     * 
     * @return 
     * @throws NotFoundException 
     */
    private String replaceAPICall(String methodName, CtMethod method) throws CannotCompileException {
    	boolean isVoid = false;
    	boolean hasArgs = false;
    	
    	try {
    		Class<?> retType = method.getReturnType().getClass();
            isVoid = retType.equals(void.class);
            hasArgs = (method.getParameterTypes().length != 0);
        } catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
        
    	// Add the COMPSsRuntime API Call with the given appId ALWAYS as FIRST parameter
    	// Something like: itApiVar.methodName(itAppIdVar, $$);
    	StringBuilder apiCall = new StringBuilder("");
        if (isVoid) {
        	apiCall.append("$_ = ").append(itApiVar);
        } else {
        	apiCall.append(itApiVar);
        }
        
        apiCall.append(".").append(methodName).append("(").append(itAppIdVar);
        
        if (hasArgs) {
        	apiCall.append(", $$");
        } else {
        	// Nothing to add
        }
        
        apiCall.append(");");
    	
    	return apiCall.toString();
    }

    /**
     * Replaces the blackBox calls
     * 
     * @return
     */
    private String replaceBlackBox(String methodName, String className, CtMethod method) throws CannotCompileException {
    	if (debug) {
            logger.debug("Inspecting method call to black-box method " + methodName + ", looking for objects");
        }

        StringBuilder modifiedCall = new StringBuilder();
        StringBuilder toSerialize = new StringBuilder();

        // Check if the black-box we're going to is one of the array watch methods
        boolean isArrayWatch = method.getDeclaringClass().getName().equals(LoaderConstants.CLASS_ARRAY_ACCESS_WATCHER);

        // First check the target object
        modifiedCall.append(itORVar).append(NEW_OBJECT_ACCESS + "$0);");
        toSerialize.append(itORVar).append(SERIALIZE_LOCALLY + "$0);");

        /* Now add the call.
         * If the target object of the call is a task object,
         * invoke the method on the internal object stored by the runtime.
         * Also check the parameters.
         * We need to control the parameters of non-remote and non-instrumented methods (black box),
         * since they represent the border to the code where we can't intercept anything.
         * If any of these parameters is an object we kept track of, synchronize
         */
        String redirectedCallPars = null;
        try {
            CtClass[] paramTypes = method.getParameterTypes();
            if (paramTypes.length > 0) {
                int i = 1;
                StringBuilder aux1 = new StringBuilder("new Object[] {");
                for (CtClass parType : paramTypes) {
                    if (i > 1) {
                        aux1.append(','); /*aux2.append(',');*/ 
                    }
                    String parId = "$" + i;
                    if (parType.isPrimitive()) {
                        if (parType.equals(CtClass.booleanType)) {
                            aux1.append("new Boolean(").append(parId).append(')');
                        } else if (parType.equals(CtClass.charType)) {
                            aux1.append("new Character(").append(parId).append(')');
                        } else if (parType.equals(CtClass.byteType)) {
                            aux1.append("new Byte(").append(parId).append(')');
                        } else if (parType.equals(CtClass.shortType)) {
                            aux1.append("new Short(").append(parId).append(')');
                        } else if (parType.equals(CtClass.intType)) {
                            aux1.append("new Integer(").append(parId).append(')');
                        } else if (parType.equals(CtClass.longType)) {
                            aux1.append("new Long(").append(parId).append(')');
                        } else if (parType.equals(CtClass.floatType)) {
                            aux1.append("new Float(").append(parId).append(')');
                        } else { //if (parType.equals(CtClass.doubleType))
                            aux1.append("new Double(").append(parId).append(')');
                        }
                    } else if(parType.getName().equals(String.class.getName())){ //This is a string
                    	if (debug) {
                            logger.debug("Parameter " + i + " of black-box method " + methodName + " is an String, adding File/object access");
                        }
                    	if (isArrayWatch && i == 3) {
                            // Prevent from synchronizing task return objects to be stored in an array position
                            aux1.append(parId);
                        } else {
                        	String calledClass = className;
                            if (calledClass.equals(PrintStream.class.getName()) || calledClass.equals(StringBuilder.class.getName())){
                            	// If the call is inside a PrintStream or StringBuilder, only synchronize objects files already has the name
                            	String internalObject = itORVar + GET_INTERNAL_OBJECT + parId + ')';
                                modifiedCall.insert(0, itORVar + NEW_OBJECT_ACCESS + parId + ");");
                                aux1.append(internalObject).append(" == null ? ").append(parId).append(" : ").append("(" + parType.getName() + ")").append(internalObject);
                                toSerialize.append(itORVar).append(SERIALIZE_LOCALLY).append(parId).append(");");
                        	} else {
                        		String internalObject = itORVar + GET_INTERNAL_OBJECT + parId + ')';
                        		String taskFile = itSRVar + IS_TASK_FILE + parId +")";
                        		String apiOpenFile = itApiVar + OPEN_FILE + parId + ", " + DATA_DIRECTION + ".INOUT)";
                        		modifiedCall.insert(0, itORVar + NEW_OBJECT_ACCESS + parId + ");");
                        		// Adding check of task files
                        		aux1.append(taskFile).append(" ? ").append(apiOpenFile).append(" : ").append(internalObject).append(" == null ? ").append(parId).append(" : ").append("(" + parType.getName() + ")").append(internalObject);
                        		toSerialize.append(itORVar).append(SERIALIZE_LOCALLY).append(parId).append(");");
                        	}
                        }
                    } else { // Object (also array)
                        if (debug) {
                            logger.debug("Parameter " + i + " of black-box method " + methodName + " is an object, adding access");
                        }

                        if (isArrayWatch && i == 3) {
                            // Prevent from synchronizing task return objects to be stored in an array position
                            aux1.append(parId);
                        } else {
                            String internalObject = itORVar + GET_INTERNAL_OBJECT + parId + ')';
                            modifiedCall.insert(0, itORVar + NEW_OBJECT_ACCESS + parId + ");");
                            aux1.append(internalObject).append(" == null ? ").append(parId).append(" : ").append("(" + parType.getName() + ")").append(internalObject);
                            toSerialize.append(itORVar).append(SERIALIZE_LOCALLY).append(parId).append(");");
                        }
                    }
                    i++;
                }
                aux1.append("}");
                redirectedCallPars = aux1.toString();
            }
        } catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
        String internalObject = itORVar + GET_INTERNAL_OBJECT + "$0)";
        modifiedCall.append("if (").append(internalObject).append(" != null) {")
        			.append("$_ = ($r)" + RUN_METHOD_ON_OBJECT).append(internalObject).append(",$class,\"").append(methodName).append("\",")
        			.append(redirectedCallPars).append(",$sig);").append("}else { $_ = ($r)" + RUN_METHOD_ON_OBJECT + "$0,$class,\"").append(methodName)
        			.append("\",").append(redirectedCallPars).append(",$sig); }");
        		
        // Serialize the (internal) objects locally after the call
        modifiedCall.append(toSerialize);

        // Return all the modified call
        return modifiedCall.toString();
    }   
    
	/** 
	 * Check the access to fields of objects
	 * 
     */
    public void edit(FieldAccess fa) throws CannotCompileException {
        CtField field = null;
        try {
            field = fa.getField();
            if (Modifier.isStatic(field.getModifiers())) {
                return;
            }
        } catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
        String fieldName = field.getName();

        if (debug) {
            logger.debug("Keeping track of access to field " + fieldName + " of class " + field.getDeclaringClass().getName());
        }

        boolean isWriter = fa.isWriter();

        // First check the object containing the field
        StringBuilder toInclude = new StringBuilder();
        toInclude.append(itORVar).append(NEW_OBJECT_ACCESS).append("$0,").append(isWriter).append(");");

        // Execute the access on the internal object
        String internalObject = itORVar + GET_INTERNAL_OBJECT + "$0)";
        String objectClass = fa.getClassName();
        toInclude.append("if (").append(internalObject).append(" != null) {");
        if (isWriter) {
            toInclude.append("((").append(objectClass).append(')').append(internalObject).append(").").append(fieldName).append(" = $1;"); // store a new value in the field
            toInclude.append("} else { " + PROCEED + "$$); }");
            // Serialize the (internal) object locally after the access
            toInclude.append(itORVar).append(SERIALIZE_LOCALLY + "$0);");
        } else {
            toInclude.append("$_ = ((").append(objectClass).append(')').append(internalObject).append(").").append(fieldName).append(';'); // read the field value
            toInclude.append("} else { " + PROCEED + "$$); }");
        }

        fa.replace(toInclude.toString());
        
        if (debug) {
            logger.debug("Replaced regular field access by " + toInclude.toString());
        }
    }
    
    
    private class ParameterInformation {
    	
    	private String toAppend;
    	private String toPrepend;
    	private String type;
    	private String direction;
    	
    	ParameterInformation(String toAppend, String toPrepend, String type, String direction) {
    		this.toAppend = toAppend;
    		this.toPrepend = toPrepend;
    		this.type = type;
    		this.direction = direction;
    	}
    	
    	public String getToAppend() {
			return toAppend;
		}
		
		public String getToPrepend() {
			return toPrepend;
		}
		
		public String getType() {
			return type;
		}
		
		public String getDirection() {
			return direction;
		}

    }
    
    private class ReturnInformation {
    	
    	private String toAppend;
    	private String toPrepend;
    	private String afterExecution;
    	
    	ReturnInformation(String toAppend, String toPrepend, String afterExecution) {
    		this.toAppend = toAppend;
    		this.toPrepend = toPrepend;
    		this.afterExecution = afterExecution;
    	}
    	
    	public String getToAppend() {
			return toAppend;
		}
		
		public String getToPrepend() {
			return toPrepend;
		}
		
		public String getAfterExecution() {
			return afterExecution;
		}

    }
    
   private class CallInformation {
    	
    	private String toAppend;
    	private String toPrepend;
    	
    	CallInformation(String toAppend, String toPrepend) {
    		this.toAppend = toAppend;
    		this.toPrepend = toPrepend;
    	}
    	
    	public String getToAppend() {
			return toAppend;
		}
		
		public String getToPrepend() {
			return toPrepend;
		}
		
    }

}
