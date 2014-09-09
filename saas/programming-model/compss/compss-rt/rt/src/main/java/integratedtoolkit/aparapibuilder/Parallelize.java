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



package integratedtoolkit.aparapibuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javassist.CtClass;


public class Parallelize {
	
	//This class wraps the integratedtoolkit.types.annotations.Method class so the project can run/build without a compss.jar
	private static class ITMethod {
		private static Method _parallelMethod = null;
		private static Method _classNameMethod = null;
		
		private Annotation mOwner;
		
		public ITMethod(Annotation ant) throws Exception {
			mOwner = ant;
			
			if (_parallelMethod == null) {
				_parallelMethod = mOwner.annotationType().getDeclaredMethod("isParallel");
				_parallelMethod.setAccessible(true);
			}
			if (_classNameMethod == null) {
				_classNameMethod = mOwner.annotationType().getDeclaredMethod("declaringClass");
				_classNameMethod.setAccessible(true);
			}
		}
		
		public static ITMethod asITMethod(Annotation ant) throws Exception {
			if (ant.annotationType().getName().equals("integratedtoolkit.types.annotations.Method"))
				return new ITMethod(ant);
			
			return null;
		}
		
		public boolean isParallel() throws Exception {
			return (Boolean)_parallelMethod.invoke(mOwner);
		}

		public String declaringClass() throws Exception {
			return (String)_classNameMethod.invoke(mOwner);
		}
	}
	
	private static void CopyStream(InputStream in, OutputStream out) throws Exception {
		byte[] data = new byte[8 * 1024];
		int r;
		while((r = in.read(data)) > 0)
			out.write(data, 0, r);
	}
	
	private static void printUsage() {
		String myname = Parallelize.class.getName();
		System.out.println(String.format("java %s namespace.interfacename", myname));
		System.out.println(String.format("java %s package.jar", myname));
		System.out.println(String.format("java %s folder/with/class/files", myname));
		System.out.println("");
		System.out.println("Example");
		System.out.println(String.format("  java %s matmul.sequential.MatmulItf", myname));
		System.out.println("");
	}

	public static void main(String[] args) {
		if (args == null || args.length != 1) {
			printUsage();
			return;
		}
		
		String update = buildAddOnJar(args[0], "addOn.jar");
		if (update != null) {
			System.out.println("Built add-on jar: " + update);
			String interfaceJar = getClassJarFile(args[0]);
			if (interfaceJar != null) {
				String combinedJar = mergeJarFiles(interfaceJar, update, "combined.jar");
				if (combinedJar != null) {
					System.out.println("Built fully-patched jar: " + combinedJar);
					String aparapiJar = getClassJarFile(com.amd.aparapi.Kernel.class.getName());
					if (aparapiJar != null) {
						String bundledJar = mergeJarFiles(combinedJar, aparapiJar, "bundle.jar");
						if (bundledJar != null) {
							System.out.println("Built bundled jar: " + bundledJar);
						}
					}
				}
			}
		}
	}
	
	public interface EnumerateFsCallback {
		boolean onFolder(File f);
		void onFile(File f);
	}

	public static ArrayList<File> enumerateFolder(String rootfolder) {
		final ArrayList<File> files = new ArrayList<File>();
		enumerateFolder(rootfolder, new EnumerateFsCallback() {
			
			@Override
			public boolean onFolder(File f) {
				files.add(f);
				return true;
			}
			
			@Override
			public void onFile(File f) {
				files.add(f);
			}
		});
		return files;
	}

	public static void enumerateFolder(String rootfolder, EnumerateFsCallback handler) {
		LinkedList<File> folders = new LinkedList<File>();
		
		folders.push(new File(rootfolder));
		
		while(!folders.isEmpty()) {
			File folder = folders.pop();
			
			for(String s : folder.list()) {
				File x = new File(folder, s);
				if (x.isDirectory()) {
					if (handler.onFolder(x))
						folders.push(x);
				} else {
					handler.onFile(x);
				}
			}
		}
	}
	
	// This method finds the jar file for a given class, if the class is in a directory it will build a jar file with the folder contents
	public static String getClassJarFile(String arg) {
		try
		{
			String baseJar = null;
			
			File f = new File(arg);
			if (f.exists()) {
				baseJar = arg;
			} else {
				Class<?> intf = Class.forName(arg);
				baseJar = intf.getProtectionDomain().getCodeSource().getLocation().toURI().toString();

				if (baseJar.startsWith("file:"))
					baseJar = baseJar.substring("file:".length());
				else
					throw new RuntimeException("The url reported for the .jar seems to not be a file: " + baseJar);
			}
			
			
			f = new File(baseJar);
			if (f.isDirectory()) {
				
				//We package the folder into a .jar prioir to running the merge process
				System.out.println("Source for class files is a folder not a .jar, auto-creating a " + baseJar);

				ArrayList<File> fs = enumerateFolder(baseJar);
				
				String rootfolder = f.getAbsolutePath();
				if (!rootfolder.endsWith(File.separator))
					rootfolder += File.separator;
		
				baseJar = "temp-source.jar";
	
				f = new File(baseJar);
				if (f.exists())
					f.delete();
				
				FileOutputStream fout = new FileOutputStream(baseJar);
				JarOutputStream jarOut = new JarOutputStream(fout);
	
				//First we create the namespace folders
				for(File cf : fs) {
					if (cf.isDirectory()) {
						//Create namespace folders as we go
						String relname = cf.getAbsolutePath();
						if (!relname.endsWith(File.separator))
							relname += File.separator;
						relname = relname.substring(rootfolder.length());
		
						if (relname.length() > 0) {
							jarOut.putNextEntry(new ZipEntry(relname.replace(File.separatorChar, '/')));
							jarOut.closeEntry();
						}
					}
				}
				
				//Then we add all the .class files
				for(File cf : fs) {
					if(cf.isFile() && cf.getName().endsWith(".class")) {
						//Put class files in the .jar
						String zipname = cf.getAbsolutePath().substring(rootfolder.length()).replace(File.separatorChar, '/');
						jarOut.putNextEntry(new ZipEntry(zipname));
						FileInputStream fin = new FileInputStream(cf);
						CopyStream(fin, jarOut);
						fin.close();
						jarOut.closeEntry();
					}
				}
				
				jarOut.close();
				fout.close();
			}
			
			return baseJar;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
	//This function merges the contents of two jar files
	public static String mergeJarFiles(String baseJar, String addOnJar, String outputJar) {
		//Pretty straightforward: copy all files from baseJar to outputJar, unless there is a replacement entry in addOnJar
		
		try
		{
			File f = new File(outputJar);
			if (f.exists())
				f.delete();			
			
			//Build a list of current entries in update set
			Hashtable<String, Boolean> updateEntries = new Hashtable<String, Boolean>();
			FileInputStream finUpdate = new FileInputStream(addOnJar);
			JarInputStream jarInUpdate = new JarInputStream(finUpdate);
			ZipEntry curEntry;
			while((curEntry = jarInUpdate.getNextEntry()) != null)
				updateEntries.put(curEntry.getName(), curEntry.isDirectory());
			jarInUpdate.close();
			finUpdate.close();
			
			//Prepare the output file
			FileOutputStream fout = new FileOutputStream(outputJar);
			JarOutputStream jarOut = new JarOutputStream(fout);

			//First we copy all directory entries from the update into the output
			for(String k : updateEntries.keySet())
				if (updateEntries.get(k)) {
					jarOut.putNextEntry(new ZipEntry(k));
					jarOut.closeEntry();
				}
			
			//Now simply copy all existing entries to the new jar, but filter updates
			FileInputStream finBase = new FileInputStream(baseJar);
			JarInputStream jarInBase = new JarInputStream(finBase);
			while((curEntry = jarInBase.getNextEntry()) != null) {
				if (!updateEntries.containsKey(curEntry.getName())) {
					jarOut.putNextEntry(new ZipEntry(curEntry.getName()));
					if (!curEntry.isDirectory())
						CopyStream(jarInBase, jarOut);
					jarOut.closeEntry();
				}
			}
			
			jarInBase.close();
			finBase.close();
			
			//Now copy the file entries from the update into the output
			finUpdate = new FileInputStream(addOnJar);
			jarInUpdate = new JarInputStream(finUpdate);
			while((curEntry = jarInUpdate.getNextEntry()) != null) {
				if (!curEntry.isDirectory()) {
					jarOut.putNextEntry(new ZipEntry(curEntry.getName()));
					CopyStream(jarInUpdate, jarOut);
					jarOut.closeEntry();
				}
			}

			jarInUpdate.close();
			finUpdate.close();
			
			jarOut.close();
			fout.close();
			
			return outputJar;
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;

	}
	
	//This function will produce special DynamicKernel classes that are subclasses of com.amd.aparapi.Kernel and suited
	// for OpenCL execution. The methods that are converted are chosen by looking for the isParallel attribute in the
	// method annotation. The produced .jar file will contain the dynamically generated classes as well as replacement
	// classes for those classes that contained the methods. The replacement classes has methods that have identical
	// function name/signature but invokes the dynamically generated classes. The original methods are still present
	// in the files, but their names are prefixed with "__Orig_". If the dynamic kernel invocation fails for some reason,
	// the original method is then invoked.
	public static String buildAddOnJar(String arg, String outputName) {
		
		//Keeps track of what classes/methods have been modified, and their result dynamic classes
		Hashtable<Class<?>, Hashtable<Method, CtClass>> mappedMethods = new Hashtable<Class<?>, Hashtable<Method, CtClass>>();
		//Keeps track of all replacement and dynamically generated classes
		ArrayList<CtClass> generatedClasses = new ArrayList<CtClass>();
		
		try {
			
			File f = new File(outputName);
			if (f.exists())
				f.delete();
			
			final ArrayList<String> interfaceNames = new ArrayList<String>();
			
			f = new File(arg);
			if (f.exists()) {
				
				System.out.println("Searching for *Itf.class in " + arg);

				if (f.isDirectory()) {
					String tmp = f.getAbsolutePath();
					if (!tmp.endsWith(File.separator))
						tmp += File.separator;
					final String rootfolder = tmp;
					
					//Find all .class files that have a name that ends with Itf
					enumerateFolder(arg, new EnumerateFsCallback() {
						@Override public boolean onFolder(File f) { return true; }
						
						@Override public void onFile(File f) {
							if (f.getName().endsWith("Itf.class")) {
								String relname = f.getAbsolutePath().substring(rootfolder.length()).replace(File.separatorChar, '.');
								interfaceNames.add(relname.substring(0, relname.length() - ".class".length()));
							}
						}
					});
				} else {
					//It must be a .jar file
					FileInputStream finUpdate = new FileInputStream(arg);
					JarInputStream jarInUpdate = new JarInputStream(finUpdate);
					ZipEntry curEntry;
					while((curEntry = jarInUpdate.getNextEntry()) != null)
						if (!curEntry.isDirectory() && curEntry.getName().endsWith("Itf.class")) {
							String tmpname = curEntry.getName().replace('/', '.');
							interfaceNames.add(tmpname.substring(0, tmpname.length() - ".class".length()));
						}
					jarInUpdate.close();
					finUpdate.close();
				}
				
			} else {
				//We assume the name passed is the name of a single interface
				interfaceNames.add(arg);
			}
			
			System.out.println("Evaluating " + interfaceNames.size() + " interface" + (interfaceNames.size() == 1 ? "" : "s"));
			
			//First we find all methods that are marked parallel, and create their parallel versions
			for(String itfName : interfaceNames) {
				Class<?> intf = null;
				
				try { intf = Class.forName(itfName); }
				catch (Throwable t) {
					if (interfaceNames.size() == 1)
						throw (Exception)t;
					
					System.out.println("Unable to load class " + itfName + ": " + t.getMessage());
				}
				
				if( intf != null) {
					for(Method method : intf.getMethods()) {
						for(Annotation _ant : method.getAnnotations()) {
							ITMethod ant = ITMethod.asITMethod(_ant);
							if (ant != null && ant.isParallel()) {
								Class<?> srcClass = Class.forName(ant.declaringClass());
								Method srcMethod = srcClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
		
								//Make sure we only generate each method once
								if (!mappedMethods.containsKey(srcClass))
									mappedMethods.put(srcClass, new Hashtable<Method, CtClass>());
								if (!mappedMethods.get(srcClass).contains(srcMethod)) {
									try {
										//Create a parallel version
										CtClass genClass = com.amd.aparapi.AutoParallel.CreateParallelMethod(srcMethod);
										if (genClass == null)
											throw new Exception("Failed to analyze " + srcMethod.getName());
										generatedClasses.add(genClass);
										mappedMethods.get(srcClass).put(srcMethod, genClass);
									} catch (Exception ex) {
										//Not fatal, we just use the non-parallel version
										System.out.println(String.format("Failed to generate class for %s.%s(...), message: %s", srcClass.getName(), srcMethod.getName(), ex.getMessage()));
										ex.printStackTrace();
									}
								}
							}
						}
					}
				}
			}

			System.out.println("Found " + mappedMethods.size() + " class" + (interfaceNames.size() == 1 ? "" : "es") + " with parallel methods");

			//Now that we have generated all classes, we modify all the classes that have parallel methods
			for(Class<?> cls : mappedMethods.keySet()) {
				Hashtable<Method, CtClass> methodset = mappedMethods.get(cls);
				
				//If this class has any modified methods, we must now build a new version of it
				if (methodset.size() > 0) {
					generatedClasses.add(com.amd.aparapi.AutoParallel.PatchClass(cls, methodset));
				}
			}
			
			if (generatedClasses.size() != 0)
				System.out.println("Writing " + generatedClasses.size() + " modified class" + (interfaceNames.size() == 1 ? "" : "es") + " into " + outputName);
			
			if (generatedClasses.size() > 0) {
				//We now have all classes modified, write out a supplementary .jar file
				FileOutputStream fout = new FileOutputStream(outputName);
				JarOutputStream jarOut = new JarOutputStream(fout);
	
				//First we generate stub directories for the namespaces
				Hashtable<String, String> generatedNamespaces = new Hashtable<String, String>();
				for(CtClass cls : generatedClasses) {
					String namespaceFolder = cls.getPackageName().replace('.', '/');
					if (!namespaceFolder.endsWith("/"))
						namespaceFolder += "/";
					
					if (!generatedNamespaces.containsKey(namespaceFolder)) {
						jarOut.putNextEntry(new ZipEntry(namespaceFolder));
						jarOut.closeEntry();
						generatedNamespaces.put(namespaceFolder, namespaceFolder);
					}
				}
				
				//Then we add all the modified class files
				for(CtClass cls : generatedClasses) {
					String filename = cls.getName().replace('.', '/') + ".class";
					
					jarOut.putNextEntry(new ZipEntry(filename));
					byte[] data = cls.toBytecode();
					
					jarOut.write(data);
					jarOut.closeEntry();
				}
				
				jarOut.close();
				fout.close();

				return outputName;
				
			} else {
				System.out.println("No classes modified, not writing new jar");
				return null;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
}
