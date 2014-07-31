package com.ibm.wala.dalvik.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import com.ibm.wala.classLoader.JarStreamModule;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.dalvik.classLoader.DexFileModule;
import com.ibm.wala.dalvik.dex.util.config.DexAnalysisScopeReader;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.io.FileSuffixes;

public class AndroidAnalysisScope {
	
	public final static String STD_EXCLUSION_REG_EXP =
			"java\\/awt\\/.*\n"
			+ "javax\\/swing\\/.*\n"
			+ "java\\/nio\\/.*\n"
			+ "java\\/net\\/.*\n"
			+ "sun\\/awt\\/.*\n"
			+ "sun\\/swing\\/.*\n"
			+ "com\\/sun\\/.*\n"
			+ "sun\\/.*\n"
			+ "apple\\/awt\\/.*\n"
			+ "com\\/apple\\/.*\n"
			+ "org\\/omg\\/.*\n"
			+ "javax\\/.*\n";
	
	
	public static AnalysisScope setUpAndroidAnalysisScope(String androidLib, String classpath) throws IOException {
		AnalysisScope scope = DexAnalysisScopeReader.makeAndroidBinaryAnalysisScope(classpath, STD_EXCLUSION_REG_EXP);
		setUpAnalysisScope(scope, new File(androidLib).toURI());
		return scope;
	}
	
	public static AnalysisScope setUpAndroidAnalysisScope(String androidLib, String classpath, String exclusions) throws IOException {
		AnalysisScope scope = DexAnalysisScopeReader.makeAndroidBinaryAnalysisScope(classpath, exclusions);
		setUpAnalysisScope(scope, new File(androidLib).toURI());
		return scope;
	}

/** BEGIN Custom change: Fixes in AndroidAnalysisScope */    
    public static AnalysisScope setUpAndroidAnalysisScope(String androidLib, String classpath, File exclusions) throws IOException {
		AnalysisScope scope = DexAnalysisScopeReader.makeAndroidBinaryAnalysisScope(classpath, exclusions);
        setUpAnalysisScope(scope, new File(androidLib).toURI());
		return scope;
	}
/** END Custom change: Fixes in AndroidAnalysisScope */    

	public static AnalysisScope setUpAndroidAnalysisScope(URI androidLib, URI classpath, File exclusions) throws IOException {
		AnalysisScope scope = DexAnalysisScopeReader.makeAndroidBinaryAnalysisScope(classpath, exclusions);
        setUpAnalysisScope(scope, androidLib);
		return scope;
	}
	
	private static void setUpAnalysisScope(AnalysisScope scope, URI androidLib) throws IOException {
/** BEGIN Custom change: Fixes in AndroidAnalysisScope */        
        if (androidLib == null) {
            throw new IllegalArgumentException("The argument androidLib may not be null.");
        }
/** END Custom change: Fixes in AndroidAnalysisScope */

		scope.setLoaderImpl(ClassLoaderReference.Application,
				"com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");

		scope.setLoaderImpl(ClassLoaderReference.Primordial,
				"com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");

/** BEGIN Custom change: Fixes in AndroidAnalysisScope */
        if (FileSuffixes.isDexFile(androidLib)) {
/** END Custom change: Fixes in AndroidAnalysisScope */            
			Module dexMod = new DexFileModule(new File(androidLib));
			
//			Iterator<ModuleEntry> mitr = dexMod.getEntries();
//			while (mitr.hasNext()) {
//				ModuleEntry moduleEntry = (ModuleEntry) mitr.next();
//				logger.error("dex module: {}", moduleEntry.getName());
//			}

			scope.addToScope(ClassLoaderReference.Primordial, dexMod);
		} else {
/** BEGIN Custom change: Fixes in AndroidAnalysisScope */            
            if (FileSuffixes.isRessourceFromJar(androidLib)) {
                //final FileProvider fileProvider = new FileProvider();
                final InputStream is = androidLib.toURL().openStream();
                assert (is != null);
                final Module libMod = new JarStreamModule(new JarInputStream(is));
                scope.addToScope(ClassLoaderReference.Primordial, libMod);
                //throw new UnsupportedOperationException("Cannot extract lib from jar");
            } else {
    			scope.addToScope(ClassLoaderReference.Primordial, new JarFile(new File(
	    			androidLib)));
            }
/** END Custom change: Fixes in AndroidAnalysisScope */            
		}
	}
    
}
