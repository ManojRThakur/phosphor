package edu.columbia.cs.psl.phosphor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SelectiveInstrumentationManager {
	
	public static List<MethodDescriptor> methodsToInstrument = new ArrayList<MethodDescriptor>();
	
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
}
