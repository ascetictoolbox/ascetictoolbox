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



package integratedtoolkit.loader;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

import org.apache.log4j.Logger;

import integratedtoolkit.types.annotations.Orchestration;
import integratedtoolkit.ITConstants;
import integratedtoolkit.log.Loggers;

public class AddOrchestration {

	private static final Logger logger = Logger.getLogger(Loggers.LOADER);
	
	public static void main(String[] args) throws Exception {
		
		if (args.length<2){
			logger.fatal("Error: missing arguments for loader");
            System.exit(1);
		}
		String className = args[0];
		String classPackage = getPackage(className);
		//pool creation
		ClassPool pool = ClassPool.getDefault();
		if (classPackage!=null && classPackage.trim().length()>0)
			 pool.importPackage(classPackage);
		//extracting the class
		CtClass cc = pool.getCtClass(className);
		ClassFile ccFile = cc.getClassFile();
		ConstPool constpool = ccFile.getConstPool();
		for (int i=1; i<args.length;i++){
			String methodLabel = args[i];
			String methodName = getMethodName(methodLabel);
			CtClass[] params = getParamClasses(methodLabel, pool); 
			CtMethod methodDescriptor = cc.getDeclaredMethod(methodName, params);
			AnnotationsAttribute attr = (AnnotationsAttribute) methodDescriptor.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
			if (attr == null){
				// create the annotation
				attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
			}
			Annotation annot = new Annotation("integratedtoolkit.types.annotations.Orchestration", constpool);
			attr.addAnnotation(annot);
			methodDescriptor.getMethodInfo().addAttribute(attr);
		}
		 cc.writeFile(); 
		  
		 // transform the ctClass to java class
		 /*Class<?> dynamiqueBeanClass = cc.toClass();
		 
		 //instanciating the updated class
		 AddOrchestration ao = (AddOrchestration) dynamiqueBeanClass.newInstance();
		  
		  try{
		         Method fooMethod = ao.getClass().getDeclaredMethod(methodName, new Class<?>[] { int.class });
		         //getting the annotation
		         Orchestration o = (Orchestration) fooMethod.getAnnotation(Orchestration.class);
		         System.out.println("METHOD: " + fooMethod);
		         System.out.println("ANNOTATION: " + o);
		  }
		  catch(Exception e){
		         e.printStackTrace();
		  }*/
	}

	private static CtClass[] getParamClasses(String label, ClassPool pool) throws NotFoundException, Exception {
		List<CtClass> classes = new LinkedList<CtClass>();
		List<String> params = getParametersTypeFromLabel(label);
		if (params!= null && params.size()>0){
			for (String className:params){
				String pack = getPackage(className);
				if (pack!=null){
					pool.importPackage(pack);
				}
				classes.add(pool.getCtClass(className));
			}
			return classes.toArray(new CtClass[classes.size()]);
		}else
			return new CtClass[0];
	}
	
	public static List<String> getParametersTypeFromLabel(String label) throws Exception{
		int begin = label.indexOf("(");
		int end = label.indexOf(")");
		if (begin>0 && end>0 && end>begin){
			String parsString = label.substring(begin+1,end);
			logger.debug("Parameters: "+ parsString );
			List<String> parameters = new LinkedList<String>();
			if(parsString!=null && parsString.trim().length()>0){
				String[] parametersArray = parsString.split(", ");
				if (parametersArray!=null && parametersArray.length>0){
					for (String parameter:parametersArray){
							parameters.add(parameter);
						
					}
				}
			}
			return parameters ;
		}else
			throw(new Exception("Error incorrect label "+ label));
	}

	private static String getMethodName(String label) throws Exception {
			int i = label.indexOf("(");
			if (i>0)
				return label.substring(0,i);
			else
				throw(new Exception("Error method name from label "+ label));
	}

	private static String getPackage(String className) throws Exception {
		if (className != null && className.trim().length()>0){
			int i = className.lastIndexOf(".");
			if (i>=0)
				return className.substring(i+1).trim();
			else
				return null;
		}else
			throw(new Exception("className is null"));
		 
	}
			
}
	
