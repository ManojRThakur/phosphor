package edu.columbia.cs.psl.phosphor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class SelectiveInstrumentationManager {
	
	public static Set<MethodDescriptor> methodsToInstrument = new HashSet<MethodDescriptor>();
	
	public static void populateMethodsToInstrument(String file) {
		
		FileInputStream fis = null;
		BufferedReader br = null;
		try {
			fis = new FileInputStream(new File(file));
			br = new BufferedReader(new InputStreamReader(fis));
		 
			String line = null;
			while ((line = br.readLine()) != null)
				if(line.length() > 0)
					methodsToInstrument.add(TaintUtils.getMethodDesc(line));
			
		} catch(IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		String s = "<org.apache.regexp.RECompiler: int terminal()>";
		MethodDescriptor desc = TaintUtils.getMethodDesc(s);
		System.out.println(s);
		System.out.println(TaintUtils.getMethodDesc(desc));
		
	}
}
