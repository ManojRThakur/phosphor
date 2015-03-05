package edu.ucla;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.ClassReader;


public class Main {
	public static ClassLoader loader;
	static int cnt;
	public static void main(String[] args) {
		if(args.length < 1)
		{
			 System.err.println("Usage: java -jar doop-inferencer.jar [source]");
			 return;
		}
		SelectiveInstrumentationManager.populateMethodsToInstrument(System.getProperty("user.dir")+"/methods");
		
		String inputFolder = args[0];
		// Setup the class loader
		final ArrayList<URL> urls = new ArrayList<URL>();
		Path input = FileSystems.getDefault().getPath(args[0]);
		try {
			if (Files.isDirectory(input)){
				Files.walkFileTree(input, new FileVisitor<Path>() {

//							@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						return FileVisitResult.CONTINUE;
					}

//							@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						if (file.getFileName().toString().endsWith(".jar"))
							urls.add(file.toUri().toURL());
						return FileVisitResult.CONTINUE;
					}

//							@Override
					public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
						return FileVisitResult.CONTINUE;
					}

//							@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						return FileVisitResult.CONTINUE;
					}
				});
			}
			else if (inputFolder.endsWith(".jar"))
				urls.add(new File(inputFolder).toURI().toURL());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			urls.add(new File(inputFolder).toURI().toURL());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		URL[] urlArray = new URL[urls.size()];
		urlArray = urls.toArray(urlArray);
		loader = new URLClassLoader(urlArray, Main.class.getClassLoader());
		File f = new File(inputFolder);
		if (!f.exists()) {
			System.err.println("Unable to read path " + inputFolder);
			System.exit(-1);
		}
		if (f.isDirectory())
			processDirectory(f, true);
		else if (inputFolder.endsWith(".jar"))
			processJar(f);
		else if (inputFolder.endsWith(".class"))
			try {
				processClass(f.getName(), new FileInputStream(f));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		else if (inputFolder.endsWith(".zip")) {
			processZip(f);
		} else {
			System.err.println("Unknown type for path " + inputFolder);
			System.exit(-1);
		}
		
		StringBuffer buf = new StringBuffer();
		for(MethodDescriptor desc : SelectiveInstrumentationManager.methodsToInstrument) 
			buf.append(Utils.getMethodDesc(desc)).append("\n");
		
		Utils.writeToFile(new File(System.getProperty("user.dir")+"/methods_inst"), buf.toString());
	}
	
	private static void processDirectory(File f, boolean isFirstLevel) {
		if (f.getName().equals(".AppleDouble"))
			return;
		
		for (File fi : f.listFiles()) {
			if (fi.isDirectory())
				processDirectory(fi, false);
			else if (fi.getName().endsWith(".class"))
				try {
					processClass(fi.getName(), new FileInputStream(fi));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			else if (fi.getName().endsWith(".jar"))
				processJar(fi);
			else if (fi.getName().endsWith(".zip"))
				processZip(fi);
			else {}
		}
	}
	
	public static void processJar(File f) {
		try {
			JarFile jar = new JarFile(f);
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry e = entries.nextElement();
				if (e.getName().endsWith(".class")) {
					analyzeClass(jar.getInputStream(e));
				} 
			}
		} catch (Exception e) {
			System.err.println("Unable to process jar: " + f.getAbsolutePath());
		}
	}
	
	public static void analyzeClass(InputStream is) {
		ClassReader cr;
		System.out.println("cnt " + (cnt++));
		try {
			cr = new ClassReader(is);
			
			try{
				cr.accept(new PartialInstrumentationInferencerCV(), ClassReader.EXPAND_FRAMES);
			}
			catch(ClassFormatError ex){
				ex.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void processClass(String name, InputStream is) {
		try {
			analyzeClass(is);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static void processZip(File f) {
		try {
			ZipFile zip = new ZipFile(f);
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry e = entries.nextElement();

				if (e.getName().endsWith(".class")) {
						analyzeClass(zip.getInputStream(e));
				} else if (e.getName().endsWith(".jar")) {
					ZipEntry outEntry = new ZipEntry(e.getName());
					File tmp = new File("/tmp/classfile");
					if (tmp.exists())
						tmp.delete();
					FileOutputStream fos = new FileOutputStream(tmp);
					byte buf[] = new byte[1024];
					int len;
					InputStream is = zip.getInputStream(e);
					while ((len = is.read(buf)) > 0) {
						fos.write(buf, 0, len);
					}
					is.close();
					fos.close();
					
					processJar(tmp);
					//						jos.closeEntry();
				} else {}
			}
		} catch (Exception e) {
			System.err.println("Unable to process zip: " + f.getAbsolutePath());
			e.printStackTrace();
		}
	}
}
