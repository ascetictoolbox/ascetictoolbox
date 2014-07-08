/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */



package com.amd.aparapi;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.amd.aparapi.BranchSet.SimpleLogicalExpressionNode;
import com.amd.aparapi.ClassModel.AttributePool.LocalVariableTableEntry.LocalVariableInfo;
import com.amd.aparapi.ClassModel.ConstantPool.FieldEntry;
import com.amd.aparapi.InstructionSet.AccessField;
import com.amd.aparapi.InstructionSet.AccessLocalVariable;
import com.amd.aparapi.InstructionSet.AssignToLocalVariable;
import com.amd.aparapi.InstructionSet.Branch;
import com.amd.aparapi.InstructionSet.ByteCode;
import com.amd.aparapi.InstructionSet.CompositeForEclipseInstruction;
import com.amd.aparapi.InstructionSet.CompositeForSunInstruction;
import com.amd.aparapi.InstructionSet.ConstantPoolEntryConstant;
import com.amd.aparapi.InstructionSet.FieldReference;
import com.amd.aparapi.InstructionSet.I_ALOAD_0;
import com.amd.aparapi.InstructionSet.I_GETFIELD;
import com.amd.aparapi.InstructionSet.I_IINC;
import com.amd.aparapi.InstructionSet.I_LDC;
import com.amd.aparapi.InstructionSet.I_PUTFIELD;
import com.amd.aparapi.InstructionSet.LocalVariableTableIndexAccessor;
import com.amd.aparapi.InstructionSet.MethodCall;
import com.amd.aparapi.InstructionSet.Switch;
import com.amd.aparapi.InstructionSet.TypeSpec;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.ExceptionTable;
import javassist.bytecode.MethodInfo;

//This class contains methods for converting the Java bytecode of a method with a loop
// into a Aparapi kernel method that can be executed with OpenCL
public class AutoParallel {
		
	private static final boolean DEBUG = false;
	
	public static String DumpByteCode(MethodModel mm) {
		Instruction c = mm.getPCHead();
		StringBuilder sb = new StringBuilder();
		
		while(c != null) {
			sb.append(c.toString());
			sb.append("\n");
			c = c.getNextPC();
		}
		
		return sb.toString();
	}
	
	private static int _dynamicClassNameCounter = 1;

	//Helper method that converts and executes a method in one go, helpful for debugging
	public static Object RunMethodAsParallel(Object instance, Method method, Object... realargs) throws Exception {
		Class<?> dynamicClass = CreateParallelMethod(method).toClass();
		Object newKernel = dynamicClass.newInstance();
		
		Method newMethod = dynamicClass.getDeclaredMethod("__SpecialKernelLauncher", new Class<?>[] {Object.class, Method.class, Object[].class});
		return newMethod.invoke(newKernel, new Object[] {instance == null ? method.getDeclaringClass() : instance, method, realargs});
	}
	
	//This methods converts a java.lang.reflection.Method to the Aparapi equivalent
	private static MethodModel getMethodModel(Method method) throws Exception {
		if (method.getDeclaringClass().isInterface())
			throw new CodeGenException("Cannot parallelize methods from an interface");
		
		ClassModel cm = new ClassModel(method.getDeclaringClass());

		//Invoke by reflection, because it is private and we use the stock aparapi.jar
		Method getEntryPointMethod = cm.getClass().getDeclaredMethod("getEntrypoint", String.class, String.class, Object.class);
		getEntryPointMethod.setAccessible(true);
		Entrypoint ep = (Entrypoint)getEntryPointMethod.invoke(cm, method.getName(), Descriptor.toJvmName(getMethodSignature(method)), null);
		
		return ep.getMethodModel();
	}
	
	//Main work entry method, converts a method with a loop into a dynamic kernel class.
	// This first level mainly works with an Aparapi representation of the code,
	// in an attempt to find the loop and isolate the code parts
	public static CtClass CreateParallelMethod(Method method) throws Exception {		
		MethodModel mm = getMethodModel(method);
		
		Instruction ic = mm.getExprHead();
		
		//System.out.println(DumpByteCode(mm));
		
		Instruction bestLoopCandidate = null;
		
		//Find the best loop candidate, for now we just pick the largest,
		// loop body. Another simple strategy would be to pick the first
		//More advanced strategies would look at largest loop counter,
		// or highest computational complexity, or even pick multiple
		while(ic != null) {
			if (ic instanceof CompositeForEclipseInstruction || ic instanceof CompositeForSunInstruction) {
				if (bestLoopCandidate == null)
					bestLoopCandidate = ic;
				else if(ic.getLength() > bestLoopCandidate.getLength()) {
					bestLoopCandidate = ic;
				}
			}
			
			ic = ic.getNextExpr();
		}
		
		//Do not proceed if there is no loop
		if (bestLoopCandidate == null)
			throw new CodeGenException("No loop found in " + mm.getName());
		
		Instruction initializer = null;
		BranchSet exitCondition = null;
		Instruction increment = null;
		ArrayList<Instruction> body = null;
		
		//The Sun and Eclipse compilers make different bytecodes for the same loop
		if (bestLoopCandidate instanceof CompositeForEclipseInstruction) {
			CompositeForEclipseInstruction loop = (CompositeForEclipseInstruction)bestLoopCandidate;
			
			Instruction topStack = loop.getFirstChild();
			
			if (topStack instanceof AssignToLocalVariable) {
				initializer = topStack;
				topStack = topStack.getNextExpr();
			}
			
	        Instruction last = loop.getLastChild();
	        while (last.getPrevExpr().isBranch()) {
	           last = last.getPrevExpr();
	        }
			
	        exitCondition = loop.getBranchSet();
			
	        increment = last.getPrevExpr();
	        
	        body = new ArrayList<Instruction>();
	        while(topStack.getNextExpr() != null && topStack.getNextExpr() != increment)
	        	body.add(topStack = topStack.getNextExpr());
	        
		} else if (bestLoopCandidate instanceof CompositeForSunInstruction) {
			CompositeForSunInstruction loop = (CompositeForSunInstruction)bestLoopCandidate;
			
			Instruction topStack = loop.getFirstChild();
			
			if (topStack instanceof AssignToLocalVariable) {
				initializer = topStack;
				topStack = topStack.getNextExpr();
			}
			
			exitCondition = loop.getBranchSet();
			
			Instruction lastGoto = loop.getLastChild();
			
			if (exitCondition.getFallThrough() != lastGoto) {
				increment = lastGoto.getPrevExpr();
				
				Instruction top = exitCondition.getLast();
				body = new ArrayList<Instruction>();
				while(top != null && top != increment)
					body.add(top = top.getNextExpr());
			}
		} else {
			throw new CodeGenException("Loop instruction type was not recognized");
		}
		
		//For simplicity we only support a basic loop construct where all parts are accounted for
		if(body == null || increment == null || initializer == null || exitCondition == null)
			throw new CodeGenException("Loop was missing a required component");
		
		//First we find the loop counter variable
		LocalVariableInfo counterVariable = ((AssignToLocalVariable)initializer).getLocalVariableInfo();
		
		//The initializer and increment parts are assumed to be simple non-branching code
		List<Instruction> init_list = UnrollInstructionList(initializer);
		List<Instruction> incr_list = UnrollInstructionList(increment);
		List<Instruction> exit_list = null;
		boolean upperBoundIsInclusive = false;
		
		//The exit condition is supposed to be produced from a comparison operator and two arguments
		if (!(exitCondition.getLogicalExpression() instanceof SimpleLogicalExpressionNode))
			throw new RuntimeException("Unsupported loop exit condition");
			
		//Get operator and both sides
		SimpleLogicalExpressionNode slxp = (SimpleLogicalExpressionNode)exitCondition.getLogicalExpression();
		Instruction counterVar = slxp.getBranch().getFirstChild();
		Instruction boundExpr = slxp.getBranch().getLastChild();
		boolean swappedInstructions = false;

		//Swap if needed, we assume one side is the loop counter 
		if (boundExpr instanceof AccessLocalVariable) {
			if (((AccessLocalVariable)boundExpr).getLocalVariableInfo().getVariableIndex() == counterVariable.getVariableIndex()) {
				counterVar = slxp.getBranch().getLastChild();
				boundExpr = slxp.getBranch().getFirstChild();
				swappedInstructions = true;
			}
		}
		
		//Make sure the counter is referencing the local variable
		if (counterVar instanceof AccessLocalVariable && ((AccessLocalVariable)counterVar).getLocalVariableInfo().getVariableIndex() == counterVariable.getVariableIndex()) {
			
			//Ok, all is good with the loop, and we assume the expression is non-branching code
			exit_list = UnrollInstructionList(boundExpr);
			
			//In case the code has something else than "i > length"
			switch (slxp.getBranch().getByteCode().getCode())
			{
				case Bytecode.IF_ICMPLT:
				case Bytecode.IF_ICMPGE:
					upperBoundIsInclusive = false;
					break;
				case Bytecode.IF_ICMPLE:
				case Bytecode.IF_ICMPEQ:
					upperBoundIsInclusive = true;
					break;
				default:
					exit_list = null;
			}
			
			//We do the opposite if "length > i"
			if (swappedInstructions) {
				upperBoundIsInclusive = !upperBoundIsInclusive;
			}

		} else {
			throw new CodeGenException("The loop counter was not in the exit condition, only exit conditions that compare with the loop counter are supported");
		}

		//If something looks fishy, bail
		if (init_list.size() <= 0 || incr_list.size() <= 0 || exit_list == null || exit_list.size() <= 0)
			throw new CodeGenException("It appears that one of the loop components are empty, which is not supported");
		
		//We skip the step with data dependency detection for now :(
		
		//Obtain the parts of the code that are before and after the loop, 
		// as well as the loop body (without the init,exit and incr parts) 
		ArrayList<Instruction> before_loop = new ArrayList<Instruction>();
		ArrayList<Instruction> after_loop = new ArrayList<Instruction>();
		ArrayList<Instruction> loop_body = new ArrayList<Instruction>();
		
		Instruction term = bestLoopCandidate.getStartInstruction();
		Instruction begin;
		
		if (term != null) {
			begin = mm.getPCHead();
			while(begin != null && begin != term) {
				before_loop.add(begin);
				begin = begin.getNextPC();
			}
		}

		begin = bestLoopCandidate.getNextExpr();
		if (begin != null) {
			begin = begin.getStartInstruction();
			while(begin != null) {
				after_loop.add(begin);
				begin = begin.getNextPC();
			}
		}

		for(Instruction exp : body) {
			Instruction top = exp.getStartInstruction();
			//TODO: If getNextExpr() == null ?
			Instruction bottom = exp.getNextExpr().getStartInstruction();

			while(top.pc < bottom.pc) {
				loop_body.add(top);
				top = top.getNextPC();
			}
		}
		
		//We then construct the special launcher method,
		// this gets dirty and deals with the bytecode transformations
		return BuildAparapiKernel(method, mm, before_loop, loop_body, after_loop, counterVariable.getVariableIndex(), init_list, incr_list, exit_list, upperBoundIsInclusive);
	}
	
	//This class is never used from code, it serves to add 
	// support code to the launcher so it can be executed from
	// the worker without access to AutoParallel.java.
	//Keeping the code here makes it more readable than bytecode
	@SuppressWarnings("unused")
	private abstract static class CodeContainer {

		public void __CopyFieldsFromInstance(Object instanceOrClass) throws Exception {
			Field[] own_fields = this.getClass().getDeclaredFields();
			
			//Map up the variables to support static launch method as well
			Class<?> targetClass = instanceOrClass instanceof Class<?> ? (Class<?>)instanceOrClass : instanceOrClass.getClass();
			Object targetInstance = instanceOrClass instanceof Class<?> ? null : instanceOrClass;
			
			//Copy fields to new kernel class
			for(int i = 0; i < own_fields.length; i++) {
				Field f = own_fields[i];
				if (!f.getName().startsWith("_local_var_")) {
					Field f2 = targetClass.getDeclaredField(f.getName());
					f.setAccessible(true);
					f2.setAccessible(true);
					
					f.set(this, f2.get(targetInstance));
				}
			}
		}
		
		public void __CopyFieldsToInstance(Object instanceOrClass) throws Exception {
			Field[] own_fields = this.getClass().getDeclaredFields();

			//Map up the variables to support static launch method as well
			Class<?> targetClass = instanceOrClass instanceof Class<?> ? (Class<?>)instanceOrClass : instanceOrClass.getClass();
			Object targetInstance = instanceOrClass instanceof Class<?> ? null : instanceOrClass;

			//Copy fields back to instance class
			for(int i = 0; i < own_fields.length; i++) {
				Field f = own_fields[i];
				if (!f.getName().startsWith("_local_var_")) {
					Field f2 = targetClass.getDeclaredField(f.getName());
					
					f.setAccessible(true);
					f2.setAccessible(true);
					f2.set(targetInstance, f.get(this));
				}
			}
		}

		public void __DebugPrintFields() throws Exception {
			Field[] own_fields = this.getClass().getDeclaredFields();

			//Copy fields back to instance class
			for(int i = 0; i < own_fields.length; i++) {
				Field f = own_fields[i];
				System.out.println(f.getName() + "=" + (f.get(this) == null ? "<null>" : f.get(this).toString()));
			}
		}

		//Helper to allow simple invocation with a reflection reference to the method
		public Object __SpecialKernelLauncher(Object instance, Method old_method, Object[] args) throws Exception {
				
			__CopyFieldsFromInstance(instance);
			
			//Get the special method that looks like the original method and invoke it
			Method new_method = this.getClass().getDeclaredMethod(old_method.getName(), old_method.getParameterTypes());
			new_method.setAccessible(true);
			Object result = new_method.invoke(this, args);
			
			__CopyFieldsToInstance(instance);
			
			return result;
		}
	}
	
	//Debug helper, returns the OpenCL C code string from a com.amd.aparapi.Kernel class 
	public static String KernelAsString(Class<?> cls) throws Exception {
		ClassModel cm = new ClassModel(cls);
		return KernelAsString(cm.getEntrypoint());
	}
	
	
	//Debug helper, returns the OpenCL C code string from a Aparapi method
	public static String KernelAsString(Entrypoint entryPoint) throws Exception {
		
        final StringBuilder openCLStringBuilder = new StringBuilder();
        KernelWriter openCLWriter = new KernelWriter(){
           @Override public void write(String _string) {
              openCLStringBuilder.append(_string);
           }
        };

        // Emit the OpenCL source into a string
       openCLWriter.write(entryPoint);
       
       return openCLStringBuilder.toString();
	}
	
	
	static Instruction getFinalInstruction(Instruction ent) {
		while(ent.getLastChild() != null)
			ent = ent.getLastChild();
		
		return ent;
	}
	
	//The javassist function is broken when dealing with overloaded methods
	static MethodInfo getMethod(ClassFile cf, String name, String signature) {
		MethodInfo quick = cf.getMethod(name);
		if (quick != null && quick.getDescriptor() == signature)
			return quick;
		
		List<?> mix = cf.getMethods();
		for(int i = 0; i < mix.size(); i++) {
			MethodInfo mi = (MethodInfo)mix.get(i);
			if (mi.getName().equals(name) && mi.getDescriptor().equals(signature))
				return mi;
		}
		
		return null;
	}
	
	//The low-level workhorse that does the bytecode manipulation
	static CtClass BuildAparapiKernel(Method m, MethodModel mm, List<Instruction> before, List<Instruction> body, List<Instruction> after, int loopCounterVarIndex, List<Instruction> init_bytecodes, List<Instruction> incr_bytecodes, List<Instruction> exit_bytecodes, boolean upperBoundIsInclusive) throws Exception {
		final ClassPool pool = ClassPool.getDefault();

		//Get the bytecode for the originating class
		CtClass ct = pool.getCtClass(m.getDeclaringClass().getCanonicalName());
		ClassFile cf = ct.getClassFile();

		//Create the container class
		String pkgname = m.getDeclaringClass().getPackage().getName();
		CtClass kernel_cls = pool.getCtClass(Kernel.class.getCanonicalName());
		CtClass cls = pool.makeClass(pkgname + ".DynamicKernel_" + _dynamicClassNameCounter++, kernel_cls);
		CtConstructor constr = new CtConstructor(null, cls);
		int superindex = cls.getClassFile().getConstPool().addMethodrefInfo(cls.getClassFile().getConstPool().addClassInfo(kernel_cls), "<init>", "()V");
		
		//Create the parameterless constructor
		byte[] super_caller = new byte[] {
				(byte)Bytecode.ALOAD_0,
				(byte)Bytecode.INVOKESPECIAL,
				(byte)(superindex >> 8),
				(byte)(superindex & 0xff),
				(byte)Bytecode.RETURN
		};
		constr.getMethodInfo().setCodeAttribute(new CodeAttribute(cls.getClassFile().getConstPool(), 1, 1, super_caller, new ExceptionTable(cls.getClassFile().getConstPool())));
		cls.addConstructor(constr);
		
		//Ensure that we map fields from the old class instance to the new
		Map<String, String> classname_mapper = new Hashtable<String, String>();
		classname_mapper.put(Descriptor.toJvmName(m.getDeclaringClass().getCanonicalName()), Descriptor.toJvmName(cls));
		
		//Get the source method as a javassist method
		String launcherName = m.getName();
		MethodInfo mi_source = getMethod(cf, launcherName, Descriptor.toJvmName(getMethodSignature(m)));
		
		//Construct a similar method to the one we are parallelizing
		MethodInfo runner_method = new MethodInfo(cls.getClassFile().getConstPool(), launcherName, Descriptor.toJvmName(getMethodSignature(m)));
		cls.getClassFile().addMethod(runner_method);
		
		//We register a temporary code attribute, otherwise we cannot get the declaring class from the method reference :(
		runner_method.setCodeAttribute(new CodeAttribute(runner_method.getConstPool(), 0, 0, new byte[] {(byte)ByteCode.RETURN.getCode()}, new ExceptionTable(runner_method.getConstPool())));

		//Examine the code to find out what fields we need to copy to/from the original instance
		Instruction cur = mm.getPCHead();
		Hashtable<String, CtField> accessedFieldLookup = new Hashtable<String, CtField>();
		while(cur != null) {
			if (cur instanceof AccessField) {
				FieldEntry i = ((AccessField)cur).getConstantPoolFieldEntry();
				String name = i.getNameAndTypeEntry().getNameUTF8Entry().getUTF8();
				String srccls = i.getClassEntry().getNameUTF8Entry().getUTF8();
				
				if (srccls.equals(Descriptor.toJvmName(m.getDeclaringClass().getCanonicalName())) && !accessedFieldLookup.containsKey(name)) {
					CtField nf = new CtField(ct.getField(name).getType(), name, cls);
					accessedFieldLookup.put(name, nf);
				}
			}
			cur = cur.getNextPC();
		}
		
		//Now copy all called methods into the new instance, excluding the launcher
		// which will be modified before being copied. We also register what fields
		// these methods access so they can be copied as well
		Hashtable<MethodModel, MethodInfo> calledMethods = new Hashtable<MethodModel, MethodInfo>();
		calledMethods.put(mm, runner_method);
		copyCalledMethods(mm, cls, accessedFieldLookup, calledMethods, classname_mapper);
		
		//We now know exactly what fields we need to have in the new class
		for(CtField ctf : accessedFieldLookup.values()) {
			cls.addField(ctf);
		}
		
		boolean isStaticMethod = (m.getModifiers() & java.lang.reflect.Modifier.STATIC) != 0;
		
		//Since the kernel code will expect there to be certain local variables, 
		// we need to somehow pass these variables to the kernel. Due to limitations
		// in Aparapi, we choose to store these as fields in the class.
		//
		//Using these fields, we can then pass local variables across a function
		// call boundary, which is otherwise not possible with the JVM
		Hashtable<Integer, String> localVarsBefore = findLocalVarAccess(isStaticMethod, before, null);
		Hashtable<Integer, String> localVarsAfter = findLocalVarAccess(isStaticMethod, after, null);
		Hashtable<Integer, String> localVarsBody = findLocalVarAccess(isStaticMethod, body, null);
		Hashtable<Integer, CtField> localReadVars = new Hashtable<Integer, CtField>();
		Hashtable<Integer, CtField> localWriteVars = new Hashtable<Integer, CtField>();
		
		Hashtable<Integer, Integer> runnerLvarMap = new Hashtable<Integer, Integer>();
		Hashtable<Integer, Integer> kernelLvarMap = new Hashtable<Integer, Integer>();

		Hashtable<Integer, String> localVarsInLoopHeader = new Hashtable<Integer, String>();
		findLocalVarAccess(isStaticMethod, init_bytecodes, localVarsInLoopHeader);
		findLocalVarAccess(isStaticMethod, incr_bytecodes, localVarsInLoopHeader);
		findLocalVarAccess(isStaticMethod, exit_bytecodes, localVarsInLoopHeader);

		//We have special handling for the loop counter
		localVarsBody.remove(loopCounterVarIndex);
		
		//Add the this variable, because the kernel method is never static
		kernelLvarMap.put(0, 0);
		
		//All arguments are treated as local vars, and we cannot remap their index
		int arg_lvar_index = isStaticMethod ? 0 : 1;
		for(int i = 0; i < m.getParameterTypes().length; i++)
			runnerLvarMap.put(arg_lvar_index + i, runnerLvarMap.size() + 1);


		//First we handle any arguments, because they could be used solely inside the loop
		for(int i = arg_lvar_index; i < m.getParameterTypes().length + arg_lvar_index; i++) {
			if (!localReadVars.containsKey(i) && localVarsBody.containsKey(i)) {
				String type = localVarsBody.get(i);
				CtField cx = new CtField(pool.getCtClass(getTypeFromTypeString(type).getCanonicalName()), "_local_var_" + i, cls);
				cls.addField(cx);
				localReadVars.put(i, cx);
			}
		}

		//Then we handle any local vars used before and inside the loop
		for(Integer i : localVarsBefore.keySet()) {
			if (!runnerLvarMap.containsKey(i))
				runnerLvarMap.put(i, runnerLvarMap.size() + 1);

			if (!localReadVars.containsKey(i) && localVarsBody.containsKey(i)) {
				String type = localVarsBody.get(i);
				CtField cx = new CtField(pool.getCtClass(getTypeFromTypeString(type).getCanonicalName()), "_local_var_" + i, cls);
				cls.addField(cx);
				localReadVars.put(i, cx);
			}
		}
		
		//Finally we handle local vars used inside the loop and afterwards
		for(Integer i : localVarsAfter.keySet()) {
			if (!runnerLvarMap.containsKey(i))
				runnerLvarMap.put(i, runnerLvarMap.size() + 1);

			if (!localWriteVars.containsKey(i) && localVarsBody.containsKey(i)) {
				if (localReadVars.containsKey(i)) {
					localWriteVars.put(i, localReadVars.get(i));
				} else {
					String type = localVarsBody.get(i);
					CtField cx = new CtField(pool.getCtClass(getTypeFromTypeString(type).getCanonicalName()), "_local_var_" + i, cls);
					cls.addField(cx);
					localWriteVars.put(i, cx);
				}
			}
		}
		
		//We also need to keep any local variables that are used to calculate the loop offsets
		for(Integer i : localVarsInLoopHeader.keySet()) {
			if (!runnerLvarMap.containsKey(i))
				runnerLvarMap.put(i, runnerLvarMap.size() + 1);
		}

		//Add the "this" variable because the launcher method is never static (we cannot re-map it)
		if (!isStaticMethod)
			runnerLvarMap.put(0, 0);
		
		final String LOOP_COUNTER_TYPE = Integer.TYPE.getCanonicalName();
		final String LOOP_COUNTER_DESCRIPTOR = getTypeStringFromType(Integer.TYPE);

		//Add the loop data so we can re-use the kernel for different invocations
		CtField loopMinValue = new CtField(pool.getCtClass(LOOP_COUNTER_TYPE), "_local_var_loop_min_value", cls);
		CtField loopMaxValue = new CtField(pool.getCtClass(LOOP_COUNTER_TYPE), "_local_var_loop_max_value", cls);
		CtField loopIncValue = new CtField(pool.getCtClass(LOOP_COUNTER_TYPE), "_local_var_loop_inc_value", cls);
		cls.addField(loopMinValue);
		cls.addField(loopMaxValue);
		cls.addField(loopIncValue);
		
		//Setup the map for the kernel
		kernelLvarMap.put(loopCounterVarIndex, 1);
		
		for(Integer i : localVarsBody.keySet())
			if (!kernelLvarMap.containsKey(i))
				kernelLvarMap.put(i, kernelLvarMap.size());

		//System.out.println(DumpMethod(mi_source));

		//Now we build a new method with the code before the loop, an invocation and the code after
		ArrayList<Byte> replacement = new ArrayList<Byte>();
		byte[] source_data = mi_source.getCodeAttribute().getCode();
		
		//Copy whatever code comes before the loop,
		// but update const table references and local variable indices
		CopyInstructionsAndOffsets(before, source_data, replacement, mi_source, runner_method, classname_mapper, runnerLvarMap);

		//Copy local vars to fields
		for(Integer ix : localReadVars.keySet()) {
			CtField f = localReadVars.get(ix);
			replacement.add((byte)Bytecode.ALOAD_0);
			AppendArray(CreateReadLVarInstruction(runnerLvarMap.get(ix), f.getFieldInfo().getDescriptor()), replacement);
			AppendArray(CreateWriteFieldInstruction(f), replacement);
		}

		//The code below uses the loop counter variable to store intermediate results.
		//This is needed to avoid having elements on stack when running the injected code,
		// which could cause problem with method calls
		
		//Set the upper bound value, we abuse the local loop index variable
		CopyInstructionsAndOffsets(exit_bytecodes, source_data, replacement, mi_source, runner_method, classname_mapper, runnerLvarMap);
		if (upperBoundIsInclusive) {
			replacement.add((byte)Bytecode.ICONST_1);
			replacement.add((byte)Bytecode.IADD);
		}
		AppendArray(CreateWriteLVarInstruction(runnerLvarMap.get(loopCounterVarIndex), LOOP_COUNTER_DESCRIPTOR), replacement);
		
		replacement.add((byte)Bytecode.ALOAD_0);
		AppendArray(CreateReadLVarInstruction(runnerLvarMap.get(loopCounterVarIndex), LOOP_COUNTER_DESCRIPTOR), replacement);
		AppendArray(CreateWriteFieldInstruction(loopMaxValue), replacement);

		//Set the incr value, again we use the loop counter variable, which is what the code should do anyway
		replacement.add((byte)Bytecode.ICONST_0);
		AppendArray(CreateWriteLVarInstruction(runnerLvarMap.get(loopCounterVarIndex), LOOP_COUNTER_DESCRIPTOR), replacement);
		CopyInstructionsAndOffsets(incr_bytecodes, source_data, replacement, mi_source, runner_method, classname_mapper, runnerLvarMap);
		replacement.add((byte)Bytecode.ALOAD_0);
		AppendArray(CreateReadLVarInstruction(runnerLvarMap.get(loopCounterVarIndex), LOOP_COUNTER_DESCRIPTOR), replacement);
		AppendArray(CreateWriteFieldInstruction(loopIncValue), replacement);

		//Finally set the initial loop value, using the loop counter variable, which is what the code should do anyway
		CopyInstructionsAndOffsets(init_bytecodes, source_data, replacement, mi_source, runner_method, classname_mapper, runnerLvarMap);
		replacement.add((byte)Bytecode.ALOAD_0);
		AppendArray(CreateReadLVarInstruction(runnerLvarMap.get(loopCounterVarIndex), LOOP_COUNTER_DESCRIPTOR), replacement);
		AppendArray(CreateWriteFieldInstruction(loopMinValue), replacement);
		
		if (DEBUG) {
			replacement.add((byte)Bytecode.ALOAD_0);
			int methodIndex_ = cls.getClassFile().getConstPool().addMethodrefInfo(cls.getClassFile().getConstPool().addClassInfo(cls), "__DebugPrintFields", Descriptor.toJvmName(getMethodSignature(CodeContainer.class.getDeclaredMethod("__DebugPrintFields"))));
			replacement.add((byte)Bytecode.INVOKEVIRTUAL);
			replacement.add((byte)(methodIndex_ >> 8));
			replacement.add((byte)(methodIndex_ & 0xff));
		}
		
		//Prepare for the invocation of this.execute(int rounds)
		//Calculate the number of threads to run: ((max - min) + (inc - 1)) / inc;

		//(inc - 1)
		replacement.add((byte)Bytecode.ALOAD_0);
		AppendArray(CreateReadFieldInstruction(loopIncValue), replacement);
		AppendArray(CreateLoadConstIntInstruction(1, cls.getClassFile().getConstPool()), replacement);
		replacement.add((byte)Bytecode.ISUB);

		//(max - min)
		replacement.add((byte)Bytecode.ALOAD_0);
		AppendArray(CreateReadFieldInstruction(loopMaxValue), replacement);
		replacement.add((byte)Bytecode.ALOAD_0);
		AppendArray(CreateReadFieldInstruction(loopMinValue), replacement);
		
		//+
		replacement.add((byte)Bytecode.IADD);
		
		// / inc
		replacement.add((byte)Bytecode.ALOAD_0);
		AppendArray(CreateReadFieldInstruction(loopIncValue), replacement);
		replacement.add((byte)Bytecode.IDIV);
		
		//Save the result in local variable
		AppendArray(CreateWriteLVarInstruction(runnerLvarMap.get(loopCounterVarIndex), LOOP_COUNTER_DESCRIPTOR), replacement);

		//Invoke this.execute(int rounds)
		replacement.add((byte)Bytecode.ALOAD_0);
		AppendArray(CreateReadLVarInstruction(runnerLvarMap.get(loopCounterVarIndex), LOOP_COUNTER_DESCRIPTOR), replacement);
		int methodIndex = cls.getClassFile().getConstPool().addMethodrefInfo(cls.getClassFile().getConstPool().addClassInfo(cls), "execute", Descriptor.toJvmName(getMethodSignature(Kernel.class.getDeclaredMethod("execute", Integer.TYPE))));
		replacement.add((byte)Bytecode.INVOKEVIRTUAL);
		replacement.add((byte)(methodIndex >> 8));
		replacement.add((byte)(methodIndex & 0xff));
		//Discard the return value
		replacement.add((byte)Bytecode.POP);
		
		//Copy fields to local vars
		for(Integer ix : localWriteVars.keySet()) {
			CtField f = localWriteVars.get(ix);
			replacement.add((byte)Bytecode.ALOAD_0);
			AppendArray(CreateReadFieldInstruction(f), replacement);
			AppendArray(CreateWriteLVarInstruction(runnerLvarMap.get(ix), f.getFieldInfo().getDescriptor()), replacement);
		}
		
		//We also write the counter with the next value
		if (localVarsAfter.containsKey(loopCounterVarIndex)) {
			replacement.add((byte)Bytecode.ALOAD_0);
			AppendArray(CreateReadFieldInstruction(loopMaxValue), replacement);
			AppendArray(CreateWriteLVarInstruction(runnerLvarMap.get(loopCounterVarIndex), localVarsAfter.get(loopCounterVarIndex)), replacement);
		}

		//Append whatever code comes after the loop,
		// but update const table references and local variable indices
		CopyInstructionsAndOffsets(after, source_data, replacement, mi_source, runner_method, classname_mapper, runnerLvarMap);

		//We now have the entire code for the launcher, so we generate the code attribute
		runner_method.setCodeAttribute(new CodeAttribute(cls.getClassFile().getConstPool(), mi_source.getCodeAttribute().getMaxStack(), mi_source.getCodeAttribute().getMaxLocals(), getByteArray(replacement), new ExceptionTable(cls.getClassFile().getConstPool())));
		runner_method.getCodeAttribute().computeMaxStack();
		//System.out.println("\n" + DumpMethod(runner_method));

		//Next step is to create the inner loop code in a separate method
		replacement.clear();

		//Inside the method, we must set the loop counter with: 
		// i = (loopOffset + getGlobalID()) * loopIncrement
		
		//Read the loop counter values, and calculate the local counter value
		replacement.add((byte)Bytecode.ALOAD_0); //Push "this" on stack
		AppendArray(CreateReadFieldInstruction(loopIncValue), replacement);
		replacement.add((byte)Bytecode.ALOAD_0); //Push "this" on stack
		AppendArray(CreateReadFieldInstruction(loopMinValue), replacement);		
		replacement.add((byte)Bytecode.ALOAD_0); //Push "this" on stack
		methodIndex = cls.getClassFile().getConstPool().addMethodrefInfo(cls.getClassFile().getConstPool().addClassInfo(cls), "getGlobalId", Descriptor.toJvmName(getMethodSignature(Kernel.class.getDeclaredMethod("getGlobalId"))));
		replacement.add((byte)Bytecode.INVOKEVIRTUAL);
		replacement.add((byte)(methodIndex >> 8));
		replacement.add((byte)(methodIndex & 0xff));		
		replacement.add((byte)Bytecode.IADD);
		replacement.add((byte)Bytecode.IMUL);
		AppendArray(CreateWriteLVarInstruction(kernelLvarMap.get(loopCounterVarIndex), getTypeStringFromType(Integer.TYPE)), replacement);
		
		//To safeguard against executions that do not honor the loop counter, we check if we are within bounds
		//This can happen because the hardware sometimes needs to schedule a certain number of threads
		AppendArray(CreateReadLVarInstruction(kernelLvarMap.get(loopCounterVarIndex), getTypeStringFromType(Integer.TYPE)), replacement);
		replacement.add((byte)Bytecode.ALOAD_0); //Push "this" on stack
		AppendArray(CreateReadFieldInstruction(loopMaxValue), replacement);
		replacement.add((byte)Bytecode.IF_ICMPLT); //Jump two bytes, i.e. past the return statement
		replacement.add((byte)0);
		replacement.add((byte)4);
		replacement.add((byte)Bytecode.RETURN);
		
		//Now copy the fields to local vars so we can execute the code without too much modification
		for(Integer ix : localReadVars.keySet()) {
			CtField f = localReadVars.get(ix);
			replacement.add((byte)Bytecode.ALOAD_0);
			AppendArray(CreateReadFieldInstruction(f), replacement);
			AppendArray(CreateWriteLVarInstruction(kernelLvarMap.get(ix), f.getFieldInfo().getDescriptor()), replacement);
		}

		//Construct the method being executed by the kernel
		MethodInfo run_method = new MethodInfo(cls.getClassFile().getConstPool(), "run", "()V");
		cls.getClassFile().addMethod(run_method);
		
		//We register a temporary code attribute, otherwise we cannot get the declaring class from the method reference :(
		run_method.setCodeAttribute(new CodeAttribute(runner_method.getConstPool(), 0, 0, new byte[] {(byte)ByteCode.RETURN.getCode()}, new ExceptionTable(runner_method.getConstPool())));

		//We can now run through the instructions,
		// unfortunately we cannot just copy the instructions,
		// because we need to update references to the const pool,
		// update the local variable map and the branch offsets
		CopyInstructionsAndOffsets(body, source_data, replacement, mi_source, run_method, classname_mapper, kernelLvarMap);
		
		//Copy fields back to local vars
		for(Integer ix : localWriteVars.keySet()) {
			CtField f = localWriteVars.get(ix);
			replacement.add((byte)Bytecode.ALOAD_0);
			AppendArray(CreateReadLVarInstruction(kernelLvarMap.get(ix), f.getFieldInfo().getDescriptor()), replacement);
			AppendArray(CreateWriteFieldInstruction(f), replacement);
		}

		//The loop code has completed
		replacement.add((byte)Bytecode.RETURN);
		
		//Construct the run method that will contain the loop body
		run_method.setCodeAttribute(new CodeAttribute(cls.getClassFile().getConstPool(), mi_source.getCodeAttribute().getMaxStack(), mi_source.getCodeAttribute().getMaxLocals(), getByteArray(replacement), new ExceptionTable(cls.getClassFile().getConstPool())));
		run_method.getCodeAttribute().computeMaxStack();

		//System.out.println("\n" + DumpMethod(run_method));

		//We now copy the __SpecialKernelLaucher and helper methods into the new class,
		// so we can easily launch the new method, even if the AutoParallel class is not present on the worker
		Method launcher_method_refl = CodeContainer.class.getDeclaredMethod("__SpecialKernelLauncher", new Class<?>[] {Object.class, Method.class, Object[].class});
		javassist.CtClass ctcls = pool.getCtClass(CodeContainer.class.getName());
		cls.addMethod(new CtMethod(ctcls.getMethod(launcher_method_refl.getName(), Descriptor.toJvmName(getMethodSignature(launcher_method_refl))), cls, null));
		launcher_method_refl = CodeContainer.class.getDeclaredMethod("__CopyFieldsFromInstance", new Class<?>[] {Object.class});
		ctcls = pool.getCtClass(CodeContainer.class.getName());
		cls.addMethod(new CtMethod(ctcls.getMethod(launcher_method_refl.getName(), Descriptor.toJvmName(getMethodSignature(launcher_method_refl))), cls, null));
		launcher_method_refl = CodeContainer.class.getDeclaredMethod("__CopyFieldsToInstance", new Class<?>[] {Object.class});
		ctcls = pool.getCtClass(CodeContainer.class.getName());
		cls.addMethod(new CtMethod(ctcls.getMethod(launcher_method_refl.getName(), Descriptor.toJvmName(getMethodSignature(launcher_method_refl))), cls, null));
		
		if (DEBUG) {
			launcher_method_refl = CodeContainer.class.getDeclaredMethod("__DebugPrintFields", new Class<?>[] {});
			ctcls = pool.getCtClass(CodeContainer.class.getName());
			cls.addMethod(new CtMethod(ctcls.getMethod(launcher_method_refl.getName(), Descriptor.toJvmName(getMethodSignature(launcher_method_refl))), cls, null));
		}

		return cls;
	}
	
	//In theory this is a simple method that copies bytes from one stream to another,
	// but in practice this is more difficult because each class has its own
	// constant pool, so all entries get new indices when added to the new const pool.
	// Furthermore, we need to re-map the local variables so they appear as a
	// consecutive set of numbers with no unused holes.
	// Finally we need to keep track of all branches because branches have relative offsets,
	// and we modify the instructions and thus potentially their length.
	private static void CopyInstructionsAndOffsets(List<Instruction> src, byte[] src_data, List<Byte> target, MethodInfo src_method, MethodInfo dst_method, Map<String, String> mapper, Map<Integer, Integer> lvmap) throws Exception {
		//Basic book-keeping for all branch related data
		Hashtable<Integer, Integer> instructionOffsetMap = new Hashtable<Integer, Integer>(); //Map of original instruction index -> new instruction index
		Hashtable<Integer, Integer> jumpInstructions = new Hashtable<Integer, Integer>(); //Map of jump instruction original index -> original branch target index
		Hashtable<Switch, Integer> switchInstructions = new Hashtable<Switch, Integer>(); //Not used, we do not support switch statements

		//Copy instructions and keep track of source and destination offset,
		// and record branch instructions and their offsets
		for(Instruction i : src) {
			instructionOffsetMap.put(i.pc, target.size());
			copyInstruction(target, src_data, i, src_method, dst_method, mapper, lvmap);
			if (i instanceof Branch) {
				
				if (i instanceof Switch) {
					switchInstructions.put((Switch)i, 0);
				} else {
					jumpInstructions.put(i.pc, ((Branch)i).target.pc);
				}
			}
		}
		
		//Now process all the branch instructions
		for(Integer br : jumpInstructions.keySet()) {
			Integer old_trg = jumpInstructions.get(br);
			
			int new_br = instructionOffsetMap.get(br);
			int new_trg = instructionOffsetMap.get(old_trg);
			int distance = new_trg - new_br;
			
			byte bc = src_data[br];
			if (bc == Bytecode.GOTO_W || bc == Bytecode.JSR_W) {
				if (distance > Integer.MAX_VALUE || distance < Integer.MIN_VALUE) {
					//Can be fixed by adding extra bytes and updating offsets
					throw new RuntimeException("Jump out of supported range");
				}
				
				//Wide instructions have 4 byte signed relative index
				target.set(new_br+1, (byte)((distance >> 24) & 0xff));
				target.set(new_br+2, (byte)((distance >> 16) & 0xff));
				target.set(new_br+3, (byte)((distance >> 8) & 0xff));
				target.set(new_br+4, (byte)(distance & 0xff));

			} else {
			
				if (distance > Short.MAX_VALUE || distance < Short.MIN_VALUE) {
					//Can be fixed by adding extra bytes and updating offsets
					throw new RuntimeException("Jump out of supported range");
				}
				
				//Other branch instructions have 2 byte signed relative index
				target.set(new_br+1, (byte)(distance >> 8));
				target.set(new_br+2, (byte)(distance & 0xff));
			}
		}
		
		//Make sure we do not try to output anything if we have to deal with a switch statement
		for(Switch s : switchInstructions.keySet()) {
			int[] offsets = s.getOffsets();
			for(int i = 0; i < offsets.length; i++) {
				throw new RuntimeException("Unsupported switch statment");				
			}
		}
	}
	
	//Scans a list of instructions and finds all instructions that access a local variable and records the variable type
	private static Hashtable<Integer, String> findLocalVarAccess(boolean isStatic, List<Instruction> src, Hashtable<Integer, String> p) {
		Hashtable<Integer, String> lst = p == null ? new Hashtable<Integer, String>() : p;
		
		for(Instruction i : src) {
			LocalVariableInfo lvi = null;
			if (i instanceof LocalVariableTableIndexAccessor)
				lvi = ((LocalVariableTableIndexAccessor)i).getLocalVariableInfo();
			else if (i instanceof I_IINC) //Not mapped correctly in class hierarchy
				lvi = ((I_IINC)i).getLocalVariableInfo();
			
			if (lvi != null && !lst.containsKey(lvi.getVariableIndex())) {
				if (isStatic || lvi.getVariableIndex() != 0)
					lst.put(lvi.getVariableIndex(), lvi.getVariableDescriptor());
			}
		}
		
		return lst;
	}

	//Copies all called methods from one class to another without changing them, while also recording all field access
	private static void copyCalledMethods(MethodModel src, CtClass targetClass, Map<String, CtField> fields, Map<MethodModel, MethodInfo> methods, Map<String, String> mapper) throws Exception {
		for(MethodModel mm : src.getCalledMethods()) {
			copyMethod(mm, targetClass, fields, methods, mapper, true);
		}
	}
	
	//Copies a single method from one class to another while updating const pool references and recording all field access
	private static void copyMethod(MethodModel mm, CtClass targetClass, Map<String, CtField> fields, Map<MethodModel, MethodInfo> methods, Map<String, String> mapper, boolean includeCalled) throws Exception {
		ClassPool pool = ClassPool.getDefault();
		if (!methods.containsKey(mm)) {
			CtClass src_class = pool.getCtClass(mm.getMethod().getClassModel().getClassWeAreModelling().getCanonicalName());
			MethodInfo oldMethod = getMethod(src_class.getClassFile(), mm.getMethod().getName(), mm.getMethod().getDescriptor());
			Instruction cur = mm.getPCHead();
			
			byte[] source = oldMethod.getCodeAttribute().getCode();
			ArrayList<Byte> newsource = new ArrayList<Byte>();
			ArrayList<Instruction> instructions = new ArrayList<Instruction>();
			
			MethodInfo newMethod = new MethodInfo(targetClass.getClassFile().getConstPool(), mm.getSimpleName(), mm.getMethod().getDescriptor());
			
			//Loop through the instructions, and gather the field accesses
			while(cur != null) {
				instructions.add(cur);
				if (cur instanceof AccessField) {
					
					FieldEntry i = ((AccessField)cur).getConstantPoolFieldEntry();
					String name = i.getNameAndTypeEntry().getNameUTF8Entry().getUTF8();
					String srccls = i.getClassEntry().getNameUTF8Entry().getUTF8();
					
					if (srccls.equals(Descriptor.toJvmName(mm.getMethod().getClassModel().getClassWeAreModelling().getCanonicalName())) && !fields.containsKey(name)) {
						CtField nf = new CtField(src_class.getField(name).getType(), name, targetClass);
						fields.put(name, nf);
					}
					
				}
				cur = cur.getNextPC();
			}
			
			//Copy all instructions to the new method, and take care of branches, constants, etc.
			CopyInstructionsAndOffsets(instructions, source, newsource, oldMethod, newMethod, mapper, null);

			newMethod.setCodeAttribute(new CodeAttribute(targetClass.getClassFile().getConstPool(), oldMethod.getCodeAttribute().getMaxStack(), oldMethod.getCodeAttribute().getMaxLocals(), getByteArray(newsource), oldMethod.getCodeAttribute().getExceptionTable()));
			targetClass.getClassFile().addMethod(newMethod);
			
			methods.put(mm, newMethod);
		}
	}
	
	//This class fixes the addFieldRef method of Javassist constpool, so that it does not create duplicates 
	private static int AddFieldReference(ConstPool pool, String classname, String fieldname, String typename) throws Exception {
		Field el_field = ConstPool.class.getDeclaredField("items");
		el_field.setAccessible(true);
		Object old_list = el_field.get(pool);
		Method getMethod = old_list.getClass().getMethod("elementAt", Integer.TYPE);
		getMethod.setAccessible(true);

		int n_cls = pool.addClassInfo(Descriptor.toJavaName(classname));
		int n_name = pool.addUtf8Info(fieldname);
		int n_type = pool.addUtf8Info(typename);
		
		int n_nat = -1;
		
		Object new_list = el_field.get(pool);
		Method size_method = new_list.getClass().getDeclaredMethod("size");
		size_method.setAccessible(true);
		
		int n_count = (Integer)size_method.invoke(new_list);
		for(int j = 1; j < n_count; j++) {
			Object ne = getMethod.invoke(new_list, j);
			if (ne.getClass().getName().endsWith(".NameAndTypeInfo")) {
				int p_n = pool.getNameAndTypeName(j);
				int p_t = pool.getNameAndTypeDescriptor(j);
				
				if (p_n == n_name && p_t == n_type) {
					n_nat = j;
					break;
				}
			}
			
		}
		
		if (n_nat == -1)
			n_nat = pool.addNameAndTypeInfo(n_name, n_type);
		
		int newindex = -1;
			
		n_count = (Integer)size_method.invoke(new_list);
		for(int j = 1; j < n_count; j++) {
			Object ne = getMethod.invoke(new_list, j);
			if (ne.getClass().getName().endsWith(".FieldrefInfo")) {
				int p_cls = pool.getFieldrefClass(j);
				int p_nat = pool.getFieldrefNameAndType(j);
				
				if (p_cls == n_cls && p_nat == n_nat) {
					newindex = j;
					break;
				}
			}
			
		}
		
		if (newindex == -1)
			newindex = pool.addFieldrefInfo(n_cls, n_nat);
		
		return newindex;
	}
	
	//This class fixes the copy method of Javassist constpool, so that it does not create duplicates 
	private static int copyConstEntry(Instruction i, ConstPool oldpool, ConstPool newpool, Map<String, String> classname_mapper) throws Exception {
		int oldindex;
		if (i instanceof ConstantPoolEntryConstant)
			oldindex = ((ConstantPoolEntryConstant)i).getConstantPoolIndex();
		else if (i instanceof MethodCall)
			oldindex = ((MethodCall)i).getConstantPoolMethodIndex();
		else
			oldindex = ((FieldReference)i).getConstantPoolFieldIndex();

		//This looks really nasty because we have to do it all by reflection
		Field el_field = ConstPool.class.getDeclaredField("items");
		el_field.setAccessible(true);
		Object old_list = el_field.get(oldpool);
		Method getMethod = old_list.getClass().getMethod("elementAt", Integer.TYPE);
		getMethod.setAccessible(true);

		Object fe = getMethod.invoke(old_list, oldindex);
		
		int newindex;
		if (fe.getClass().getSuperclass() != null && fe.getClass().getSuperclass().getName().endsWith(".MemberrefInfo")) {
			
			String o_cls;
			String o_name;
			String o_type;
			if (i instanceof MethodCall) {
				o_cls = Descriptor.toJvmName(oldpool.getMethodrefClassName(oldindex));
				o_name = oldpool.getMethodrefName(oldindex);
				o_type = oldpool.getMethodrefType(oldindex);

				if (classname_mapper.containsKey(o_cls))
					o_cls = classname_mapper.get(o_cls);
				
			} else {
				o_cls = Descriptor.toJvmName(oldpool.getFieldrefClassName(oldindex));
				o_name = oldpool.getFieldrefName(oldindex);
				o_type = oldpool.getFieldrefType(oldindex);
				
				//Only map class if the field access is done on "this"
				// this simple check can be defeated by manually constructed 
				// bytecode, but I have not seen it done differently by a compiler
				if (i instanceof I_GETFIELD) {
					if (i.getPrevPC() instanceof I_ALOAD_0) {
						if (classname_mapper.containsKey(o_cls))
							o_cls = classname_mapper.get(o_cls);
					}
				} else if (i instanceof I_PUTFIELD) {
					if (i.getPrevPC().getPrevPC() instanceof I_ALOAD_0) {
						if (classname_mapper.containsKey(o_cls))
							o_cls = classname_mapper.get(o_cls);
					}
				} else {
					throw new RuntimeException("Unexpected instruction for field access");
				}
			}
			
			
			int n_cls = newpool.addClassInfo(Descriptor.toJavaName(o_cls));
			int n_name = newpool.addUtf8Info(o_name);
			int n_type = newpool.addUtf8Info(o_type);
			
			int n_nat = -1;
			
			Object new_list = el_field.get(newpool);
			Method size_method = new_list.getClass().getDeclaredMethod("size");
			size_method.setAccessible(true);
			
			int n_count = (Integer)size_method.invoke(new_list);
			for(int j = 1; j < n_count; j++) {
				Object ne = getMethod.invoke(new_list, j);
				if (ne.getClass().getName().endsWith(".NameAndTypeInfo")) {
					int p_n = newpool.getNameAndTypeName(j);
					int p_t = newpool.getNameAndTypeDescriptor(j);
					
					if (p_n == n_name && p_t == n_type) {
						n_nat = j;
						break;
					}
				}
				
			}
			
			if (n_nat == -1)
				n_nat = newpool.addNameAndTypeInfo(n_name, n_type);
			
			newindex = -1;
			if (i instanceof MethodCall) {
				
				n_count = (Integer)size_method.invoke(new_list);
				for(int j = 1; j < n_count; j++) {
					Object ne = getMethod.invoke(new_list, j);
					if (ne.getClass().getName().endsWith(".MethodrefInfo")) {
						int p_cls = newpool.getMethodrefClass(j);
						int p_nat = newpool.getMethodrefNameAndType(j);
						
						if (p_cls == n_cls && p_nat == n_nat) {
							newindex = j;
							break;
						}
					}
					
				}
				
				if (newindex == -1)
					newindex = newpool.addMethodrefInfo(n_cls, n_nat);
			} else {
				
				n_count = (Integer)size_method.invoke(new_list);
				for(int j = 1; j < n_count; j++) {
					Object ne = getMethod.invoke(new_list, j);
					if (ne.getClass().getName().endsWith(".FieldrefInfo")) {
						int p_cls = newpool.getFieldrefClass(j);
						int p_nat = newpool.getFieldrefNameAndType(j);
						
						if (p_cls == n_cls && p_nat == n_nat) {
							newindex = j;
							break;
						}
					}
					
				}
				
				if (newindex == -1)
					newindex = newpool.addFieldrefInfo(n_cls, n_nat);
			}
			
		} else {
			newindex = oldpool.copy(oldindex, newpool, classname_mapper);
		}
		
		return newindex;
	}
	
	//This function copies a single instruction from one method to another and updates the const pool for the target class
	// If a lvarMap argument is supplied, it will also change references to local variables accordingly 
	private static void copyInstruction(List<Byte> replacement, byte[] source_data, Instruction i, MethodInfo oldMethod, MethodInfo newMethod, Map<String, String> classname_mapper, Map<Integer, Integer> lvarMap) throws Exception {
		if (i instanceof ConstantPoolEntryConstant || i instanceof FieldReference || i instanceof MethodCall) {
						
			//We need to update the const pool entry and the instruction
			ConstPool oldpool = oldMethod.getConstPool();
			ConstPool newpool = newMethod.getConstPool();
			
			int newindex = copyConstEntry(i, oldpool, newpool, classname_mapper);
			
			//Special case (ldc uses one byte index)
			if (i instanceof I_LDC) {
				if (newindex < 255) {
					replacement.add((byte)Bytecode.LDC);
					replacement.add((byte)newindex);
				} else {
					replacement.add((byte)Bytecode.LDC_W);
					replacement.add((byte)(newindex >> 8));
					replacement.add((byte)(newindex & 0xff));
				}
			} else {
				if (i.getLength() < 3)
					throw new RuntimeException();
				if (i.pc >= source_data.length)
					throw new RuntimeException();
				
				replacement.add(source_data[i.pc]);
				replacement.add((byte)(newindex >> 8));
				replacement.add((byte)(newindex & 0xff));
				
				//Some instructions have extra bytes, fortunately the index 
				// is always right after the opcode, so we can handle it
				// generically
				for(int j = 3; j < i.getLength(); j++) {
					replacement.add(source_data[i.pc + j]);
				}
				
				//If we are accessing something from outside of the Dynamic class, 
				// it may be blocked if it is private or protected 
				if (i instanceof FieldReference) {
					String className = newpool.getFieldrefClassName(newindex);
					
					//NOTE: Due to the Java security model, it is not possible to do this without
					// changing the container class. It is not possible to change the class of 
					// an already loaded instance either. For these two reasons, the access
					// to private fields only works if we generate a .jar and load the .class files
					// from there. In theory it should be possible to replace the field access,
					// with an equivalent chunk of code that accesses the field via. reflection,
					// but that is probably more work than it is work (and it will be slower)
					
					//The java compiler will grant access by adding special access$n methods, 
					// but since we are not terribly security concerned, it is much easier to just modify the
					// access modifiers and make it public
					if (!className.equals(newMethod.getCodeAttribute().getDeclaringClass())) {
						CtClass rClass = ClassPool.getDefault().getCtClass(className);
						CtField rField = rClass.getDeclaredField(newpool.getFieldrefName(newindex));
						int modifiers = rField.getModifiers();
						int origModifiers = modifiers;
						if ((modifiers & javassist.Modifier.PRIVATE) != 0)
							modifiers &= ~javassist.Modifier.PRIVATE;
						if ((modifiers & javassist.Modifier.PROTECTED) != 0)
							modifiers &= ~javassist.Modifier.PROTECTED;
						if ((modifiers & javassist.Modifier.PUBLIC) == 0)
							modifiers |= javassist.Modifier.PUBLIC;
						
						if (origModifiers != modifiers) {
							rField.setModifiers(modifiers);
							
							if (!className.equals(oldMethod.getCodeAttribute().getDeclaringClass())) {
								//To support this, we basically have to register the class as being modified so it gets written to the addOn.jar
								throw new Exception(String.format("Unsupported cross-class access to non-public field %s in %s", rField.getName(), rClass.getName()));
							}
						}
					}
				}
				
			}
		} else if (lvarMap != null && i instanceof LocalVariableTableIndexAccessor) {
			int ix = lvarMap.get(((LocalVariableTableIndexAccessor)i).getLocalVariableTableIndex());
			if (i instanceof AssignToLocalVariable)
				AppendArray(CreateWriteLVarInstruction(ix, ((LocalVariableTableIndexAccessor)i).getLocalVariableInfo().getVariableDescriptor()), replacement);
			else if (i instanceof AccessLocalVariable)
				AppendArray(CreateReadLVarInstruction(ix, ((LocalVariableTableIndexAccessor)i).getLocalVariableInfo().getVariableDescriptor()), replacement);
			else
				throw new RuntimeException("Unexpected instruction");
			
		} else if (lvarMap != null && i instanceof I_IINC) {
			int ix = lvarMap.get(((I_IINC)i).getLocalVariableTableIndex());
			if (ix < 255) {
				replacement.add((byte)Bytecode.IINC);
				replacement.add((byte)ix);
				replacement.add(source_data[i.pc + 2]);
				
			} else {
				AppendArray(CreateReadLVarInstruction(ix, ((LocalVariableTableIndexAccessor)i).getLocalVariableInfo().getVariableDescriptor()), replacement);
				AppendArray(CreateLoadConstIntInstruction(source_data[i.pc + 2], newMethod.getConstPool()), replacement);
				replacement.add((byte)Bytecode.IADD);
				AppendArray(CreateWriteLVarInstruction(ix, ((LocalVariableTableIndexAccessor)i).getLocalVariableInfo().getVariableDescriptor()), replacement);
			}
		} else {
			//No special stuff, just copy it
			AppendArray(source_data, i.pc, i.getLength(), replacement);
		}		
	}
	
	@SuppressWarnings("unused")
	private static String DumpMethod(MethodInfo mi) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		CodeIterator ci = mi.getCodeAttribute().iterator();
		while(ci.hasNext()) {
			int ix = ci.next();
			ByteCode bc = ByteCode.get(ci.byteAt(ix));
			sb.append(ix + ": ");
			sb.append(bc);

			int nb = ci.hasNext() ? ci.lookAhead() - 1 : ci.getCodeLength() - 1;
			int ix2 = ix;
			
			while (ix2 < nb) {
				sb.append(" ");
				sb.append(ci.byteAt(ix2 + 1));
				ix2++;
			}
			
			if (bc == ByteCode.GETFIELD || bc == ByteCode.PUTFIELD) {
				int cx = (((int)ci.byteAt(ix + 1)) & 0xff << 8) | (((int)ci.byteAt(ix + 2)) & 0xff) ;
				sb.append(" -> ");
				sb.append(mi.getConstPool().getFieldrefName(cx));
				sb.append(" : ");
				sb.append(mi.getConstPool().getFieldrefType(cx));
			}
			
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	//No comment :)
	static void AppendArray(byte[] elements, List<Byte> lst) {
		for(int i = 0; i < elements.length; i++)
			lst.add(elements[i]);
	}

	//No comment :)
	static void AppendArray(byte[] elements, int offset, int length, List<Byte> lst) {
		for(int i = 0; i < length; i++)
			lst.add(elements[offset + i]);
	}

	//No comment :)
	static byte[] getByteArray(List<Byte> bytes) {
		byte[] res = new byte[bytes.size()];
		for(int i = 0; i < res.length; i++)
			res[i] = bytes.get(i);
		
		return res;
	}
	
	//Creates the most compact possible instruction sequence for loading a constant integer value onto stack
	static byte[] CreateLoadConstIntInstruction(int value, ConstPool cp) {
		switch(value) {
			case -1:
				return new byte[] { Bytecode.ICONST_M1 };
			case 0:
				return new byte[] { Bytecode.ICONST_0 };
			case 1:
				return new byte[] { Bytecode.ICONST_1 };
			case 2:
				return new byte[] { Bytecode.ICONST_2 };
			case 3:
				return new byte[] { Bytecode.ICONST_3 };
			case 4:
				return new byte[] { Bytecode.ICONST_4 };
			case 5:
				return new byte[] { Bytecode.ICONST_5 };
			default:
			{
				if (value < Byte.MAX_VALUE) {
					return new byte[] {
							Bytecode.BIPUSH,
							(byte)(value)
					};
				} else if (value < Short.MAX_VALUE) {
					return new byte[] {
							Bytecode.SIPUSH,
							(byte)(value >> 8),
							(byte)(value & 0xff)
					};
				} else {
					int ix = cp.addIntegerInfo(value);
					if (ix < 0xff)
						return new byte[] { Bytecode.LDC, (byte)ix };
					else
						return new byte[] { Bytecode.LDC_W, (byte)(ix >> 8), (byte)(ix & 0xff) };
				}
			}
		}
	}
	
	//Generates a PUTFIELD instruction and with a correct reference to the constant pool
	static byte[] CreateWriteFieldInstruction(CtField f) throws Exception {
		ConstPool cp = f.getFieldInfo().getConstPool(); 
		 //cp.addFieldrefInfo(cp.addClassInfo(f.getDeclaringClass()), f.getName(), f.getSignature());
		int index = AddFieldReference(cp, f.getDeclaringClass().getName(), f.getName(), f.getSignature());
		return new byte[] {
				(byte)Bytecode.PUTFIELD,
				(byte)(index >> 8),
				(byte)(index & 0xff)
		};
	}

	//Generates a GETFIELD instruction and with a correct reference to the constant pool
	static byte[] CreateReadFieldInstruction(CtField f) throws Exception {
		ConstPool cp = f.getFieldInfo().getConstPool();
		//cp.addFieldrefInfo(cp.addClassInfo(f.getDeclaringClass()), f.getName(), f.getSignature());
		int index =  AddFieldReference(cp, f.getDeclaringClass().getName(), f.getName(), f.getSignature());
		return new byte[] {
				(byte)Bytecode.GETFIELD,
				(byte)(index >> 8),
				(byte)(index & 0xff)
		};
	}

	//Generates the most compact instruction sequence for of writing a local variable of the specified type
	static byte[] CreateWriteLVarInstruction(int index, String typedesc) {
		if (typedesc.equals(TypeSpec.I.getShortName()) || typedesc.equals(TypeSpec.Z.getShortName()) || typedesc.equals(TypeSpec.S.getShortName()) || typedesc.equals(TypeSpec.C.getShortName())) {
			switch(index) {
				case 0:
					return new byte[] { Bytecode.ISTORE_0 };
				case 1:
					return new byte[] { Bytecode.ISTORE_1 };
				case 2:
					return new byte[] { Bytecode.ISTORE_2 };
				case 3:
					return new byte[] { Bytecode.ISTORE_3 };
				default:
					return new byte[] { Bytecode.ISTORE, (byte)index};
			}
		} else if (typedesc.equals(TypeSpec.J.getShortName()) || typedesc.equals(TypeSpec.L.getShortName())) {
			switch(index) {
				case 0:
					return new byte[] { Bytecode.LSTORE_0 };
				case 1:
					return new byte[] { Bytecode.LSTORE_1 };
				case 2:
					return new byte[] { Bytecode.LSTORE_2 };
				case 3:
					return new byte[] { Bytecode.LSTORE_3 };
				default:
					return new byte[] { Bytecode.LSTORE, (byte)index};
			}
		} else if (typedesc.equals(TypeSpec.F.getShortName())) {
			switch(index) {
				case 0:
					return new byte[] { Bytecode.FSTORE_0 };
				case 1:
					return new byte[] { Bytecode.FSTORE_1 };
				case 2:
					return new byte[] { Bytecode.FSTORE_2 };
				case 3:
					return new byte[] { Bytecode.FSTORE_3 };
				default:
					return new byte[] { Bytecode.FSTORE, (byte)index};
			}
		} else if (typedesc.equals(TypeSpec.D.getShortName())) {
			switch(index) {
				case 0:
					return new byte[] { Bytecode.DSTORE_0 };
				case 1:
					return new byte[] { Bytecode.DSTORE_1 };
				case 2:
					return new byte[] { Bytecode.DSTORE_2 };
				case 3:
					return new byte[] { Bytecode.DSTORE_3 };
				default:
					return new byte[] { Bytecode.DSTORE, (byte)index};
			}
		} else {
			switch(index) {
				case 0:
					return new byte[] { Bytecode.ASTORE_0 };
				case 1:
					return new byte[] { Bytecode.ASTORE_1 };
				case 2:
					return new byte[] { Bytecode.ASTORE_2 };
				case 3:
					return new byte[] { Bytecode.ASTORE_3 };
				default:
					return new byte[] { Bytecode.ASTORE, (byte)index};
			}
		}
	}

	//Generates the most compact instruction sequence for of reading a local variable of the specified type
	static byte[] CreateReadLVarInstruction(int index, String typedesc) {
		if (typedesc.equals(TypeSpec.I.getShortName()) || typedesc.equals(TypeSpec.Z.getShortName()) || typedesc.equals(TypeSpec.S.getShortName()) || typedesc.equals(TypeSpec.C.getShortName())) {
			switch(index) {
				case 0:
					return new byte[] { Bytecode.ILOAD_0 };
				case 1:
					return new byte[] { Bytecode.ILOAD_1 };
				case 2:
					return new byte[] { Bytecode.ILOAD_2 };
				case 3:
					return new byte[] { Bytecode.ILOAD_3 };
				default:
					return new byte[] { Bytecode.ILOAD, (byte)index};
			}
		} else if (typedesc.equals(TypeSpec.J.getShortName()) || typedesc.equals(TypeSpec.L.getShortName())) {
			switch(index) {
				case 0:
					return new byte[] { Bytecode.LLOAD_0 };
				case 1:
					return new byte[] { Bytecode.LLOAD_1 };
				case 2:
					return new byte[] { Bytecode.LLOAD_2 };
				case 3:
					return new byte[] { Bytecode.LLOAD_3 };
				default:
					return new byte[] { Bytecode.LLOAD, (byte)index};
			}
		} else if (typedesc.equals(TypeSpec.F.getShortName())) {
			switch(index) {
				case 0:
					return new byte[] { Bytecode.FLOAD_0 };
				case 1:
					return new byte[] { Bytecode.FLOAD_1 };
				case 2:
					return new byte[] { Bytecode.FLOAD_2 };
				case 3:
					return new byte[] { Bytecode.FLOAD_3 };
				default:
					return new byte[] { Bytecode.FLOAD, (byte)index};
			}
		} else if (typedesc.equals(TypeSpec.D.getShortName())) {
			switch(index) {
				case 0:
					return new byte[] { Bytecode.DLOAD_0 };
				case 1:
					return new byte[] { Bytecode.DLOAD_1 };
				case 2:
					return new byte[] { Bytecode.DLOAD_2 };
				case 3:
					return new byte[] { Bytecode.DLOAD_3 };
				default:
					return new byte[] { Bytecode.DLOAD, (byte)index};
			}
		} else {
			switch(index) {
				case 0:
					return new byte[] { Bytecode.ALOAD_0 };
				case 1:
					return new byte[] { Bytecode.ALOAD_1 };
				case 2:
					return new byte[] { Bytecode.ALOAD_2 };
				case 3:
					return new byte[] { Bytecode.ALOAD_3 };
				default:
					return new byte[] { Bytecode.ALOAD, (byte)index};
			}
		}
	}

	//Unrolls an aparapi instruction sequence
	static List<Instruction> UnrollInstructionList(Instruction root) {
		//First step is unrolling the instruction dependencies
		ArrayList<Instruction> list = new ArrayList<Instruction>();
		
		while(root != null) {
			list.add(root);
			root = root.getFirstChild();
		}
		
		java.util.Collections.reverse(list);

		return list;
	}

	//Returns the JVM name for a class
	static String getTypeStringFromType(Class<?> c) {
	   //I would think this function was somewhere,
	   // but I cannot find it...
	   if (c == Boolean.TYPE)
		   return TypeSpec.Z.getShortName();
	   else if (c == Character.TYPE)
		   return TypeSpec.C.getShortName();
	   else if (c == Float.TYPE)
		   return TypeSpec.F.getShortName();
	   else if (c == Double.TYPE)
		   return TypeSpec.D.getShortName();
	   else if (c == Byte.TYPE)
		   return TypeSpec.B.getShortName();
	   else if (c == Short.TYPE)
		   return TypeSpec.S.getShortName();
	   else if (c == Integer.TYPE)
		   return TypeSpec.I.getShortName();
	   else if (c == Long.TYPE)
		   return TypeSpec.L.getShortName();
	   else if (c == Void.TYPE)
		   return "V";
	   else if (c.isArray())
		   return "[" + getTypeStringFromType(c.getComponentType());
	   else
		   return "L" + c.getCanonicalName() + ";";
	}
   
	//Returns a class given the JVM name
	static Class<?> getTypeFromTypeString(String s) throws Exception {
	   if (s.equals(TypeSpec.Z.getShortName()))
		   return Boolean.TYPE;
	   else if (s.equals(TypeSpec.C.getShortName()))
		   return Character.TYPE;
	   else if (s.equals(TypeSpec.F.getShortName()))
		   return Float.TYPE;
	   else if (s.equals(TypeSpec.D.getShortName()))
		   return Double.TYPE;
	   else if (s.equals(TypeSpec.B.getShortName()))
		   return Byte.TYPE;
	   else if (s.equals(TypeSpec.S.getShortName()))
		   return Short.TYPE;
	   else if (s.equals(TypeSpec.I.getShortName()))
		   return Integer.TYPE;
	   else if (s.equals(TypeSpec.L.getShortName()))
		   return Long.TYPE;
	   else if (s.equals("V"))
		   return Void.TYPE;
	   else if (s.startsWith("["))
		   return Array.newInstance(getTypeFromTypeString(s.substring(1)), 0).getClass(); //TODO: It should not be required to create an instance?
	   else if (s.startsWith("L"))
		   return Class.forName(Descriptor.toJavaName(s.substring(1, s.length() - 1)));
	   else
		   throw new RuntimeException("Class not found: " + s);
	}

	
	//Returns the JVM signature for a method
	static String getMethodSignature(java.lang.reflect.Method method) {
	   Class<?>[] args = method.getParameterTypes();
		   
	   //Optimized for common case
	   if (args.length == 0)
		   return "()" + getTypeStringFromType(method.getReturnType());
	   else if (args.length == 1)
		   	return '(' + getTypeStringFromType(args[0]) + ')' + getTypeStringFromType(method.getReturnType());
	   
	   StringBuilder sb = new StringBuilder();
	   sb.append('('); 
   
	   for(int i = 0; i < args.length; i++) {
		   sb.append(getTypeStringFromType(args[i]));
	   }
   
	   sb.append(')'); 
	   
	   sb.append(getTypeStringFromType(method.getReturnType()));
	   
	   return sb.toString();
	}

	//Returns a javassist array of classes, given an array of java.lang.reflection classes
	static CtClass[] ConvertClassArray(Class<?>[] types) throws Exception {
		final ClassPool pool = ClassPool.getDefault();
		
		CtClass[] res = new CtClass[types.length];
		
		for(int i = 0; i < types.length; i++) {
			res[i] = pool.getCtClass(types[i].getName()); 
		}
		
		return res;
	}
	
	
	//Patches a class that contains a parallelizable method so that all methods are instead calling the
	// dynamically generated kernel. The original code is saved with the original name, but prefixed with
	// __Orig_. This ensures that the modified class looks (almost) identical to the original class.
	public static CtClass PatchClass(Class<?> sourceCls, Hashtable<Method, CtClass> methodset) throws Exception {
		final ClassPool pool = ClassPool.getDefault();

		//The launcher replacement:
		// %KC% = name of kernel class
		// %MN% = name of the method
		// %NV_TMP% = temp variable for holding return value, either "Object r =" or ""
		// %NT_RET_TMP% = return temp value, either "return r;" or ""
		// %NV_RETURN% = return statement, either "return" or ""
		// %THIS% = the source pointer, either "$0" or "package.Container.class" 
		final String replacedLauncher = "" +
				"try {" +
				"   /*System.out.println(\"Calling special launcher\");*/" + "\n" +
				"	%KC% kern = new %KC%();" + "\n" +
				"	kern.__CopyFieldsFromInstance(%THIS%);" + "\n" +
				"	%NV_TMP% kern.%MN%($$);" + "\n" +
				"	kern.__CopyFieldsToInstance(%THIS%);" + "\n" +
				"   /*System.out.println(\"Done with special launcher\"); */" + "\n" +
				"	%NV_RET_TMP%" + "\n" +
				"} catch (Exception ex) {" + "\n" +
				"   System.out.println(\"Failed to invoke Aparapi launcher\");" + "\n" +
				"	ex.printStackTrace();" + "\n" +
				"	%NV_RETURN%	__Orig_%MN%($$);" + "\n" +
				"}";
		
		
		//Create a fake new class, based on the original
		CtClass targetCt = pool.getCtClass(sourceCls.getName());
		
		for(Method srcMethod : methodset.keySet()) {
			CtMethod cm = targetCt.getDeclaredMethod(srcMethod.getName(), ConvertClassArray(srcMethod.getParameterTypes()));
			//We now replace the method with a specially crafted launcher
			
			CtClass kernelClass = methodset.get(srcMethod);
			
			//Copy the original method to a new name
			CtMethod newMethod = CtNewMethod.copy(cm, targetCt, null);
			cm.setName("__Orig_" + cm.getName());
						
			//Generate the method body
			String newBody = replacedLauncher
					.replace("%KC%", kernelClass.getName())
					.replace("%MN%", newMethod.getName())
					.replace("%NV_TMP%", srcMethod.getReturnType() == Void.TYPE ? "" : "Object r =")
					.replace("%NV_RET_TMP%", srcMethod.getReturnType() == Void.TYPE ? "" : "return r;")
					.replace("%NV_RETURN%", srcMethod.getReturnType() == Void.TYPE ? "" : "return")
					.replace("%THIS%", (srcMethod.getModifiers() & javassist.Modifier.STATIC) == 0 ? "$0" : srcMethod.getDeclaringClass().getName() + ".class");
					
			newMethod.setBody(newBody);
			targetCt.addMethod(newMethod);
		}
		
		return targetCt;
	}
}
