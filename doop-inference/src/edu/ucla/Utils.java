package edu.ucla;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {
	
	private static Map<String, String> typeToSymbol = new HashMap<String, String>();
	
	static {
		typeToSymbol.put("byte", "B");
		typeToSymbol.put("char", "C");
		typeToSymbol.put("double", "D");
		typeToSymbol.put("float", "F");
		typeToSymbol.put("int", "I");
		typeToSymbol.put("long", "J");
		typeToSymbol.put("short", "S");
		typeToSymbol.put("void", "V");
		typeToSymbol.put("boolean", "Z");
	}	
	
	private static String processType(String type) {
		StringBuffer typeBuffer = new StringBuffer();
		type = type.trim();
		if(type.indexOf("[]") > 0) {
			for(int i = 1; i < type.split("\\[").length;i++)
				typeBuffer.append("[");
			type = type.split("\\[")[0];
			typeBuffer.append(typeToSymbol.containsKey(type)?typeToSymbol.get(type): "L"+type.replaceAll("\\.", "/")+";");
		}
		else
			typeBuffer.append(typeToSymbol.containsKey(type)?typeToSymbol.get(type): "L"+type.replaceAll("\\.", "/")+";");
		return typeBuffer.toString();
	}
	
	public static MethodDescriptor getMethodDesc(String signature) {
		// get return type
		String temp = signature.split(": ")[1].trim();
		String owner = signature.split(": ")[0].trim().substring(1).replace(".", "/");
		String name = temp.split("\\(")[0].split(" ")[1];
		
		String returnTypeSymbol = processType(temp.substring(0, temp.indexOf(" ")).trim());
		 
		// get args list
		temp = signature.substring(signature.indexOf("(")+1, signature.indexOf(")"));
		StringBuffer argsBuffer = new StringBuffer();
	
		argsBuffer.append("(");
		if(temp != null && !temp.isEmpty()) {
			for(String arg : temp.split(",")) 
				argsBuffer.append(processType(arg.trim()));
		}
		argsBuffer.append(")");
	
		argsBuffer.append(returnTypeSymbol);
		return new MethodDescriptor(name, owner, argsBuffer.toString());
	}
	
	public static String getMethodDesc(MethodDescriptor desc) {
		String owner = desc.getOwner().replaceAll("/", ".");
		String methodName = desc.getName();
		String returnType = desc.getDesc().substring(desc.getDesc().indexOf(")")+1);
		String actualReturnType = processReverse(returnType);
		String args = desc.getDesc().substring(desc.getDesc().indexOf("(")+1, desc.getDesc().indexOf(")"));
		boolean noargs = (args.length() == 0);
		int idx = 0;
		List<String> arguments = new ArrayList<String>(); 
		while(args.length() > 0) {
			idx = 0;
			if(args.charAt(idx) == 'L') {
				arguments.add(processReverse(args.substring(idx, args.indexOf(";")+1)));
				idx=args.indexOf(";")+1;
			} else if(args.charAt(idx) == '[') {
				while(args.charAt(idx) == '[') 
					idx++;
				if(args.charAt(idx) == 'L') {
					arguments.add(processReverse(args.substring(0,args.indexOf(";")+1)));
					idx=args.indexOf(";")+1; 
				} else {
					arguments.add(processReverse(args.substring(0,idx+1)));
					idx=idx+1;
				}
			} else {
				arguments.add(processReverse(args.charAt(idx)+""));
				idx=idx+1;
			}
			args = args.substring(idx);
		}
		StringBuffer buf = new StringBuffer();
		buf.append("<").append(owner).append(": ").append(actualReturnType).append(" ").append(methodName).append("(");
		for(String s : arguments)
			buf.append(s).append(",");
		if(!noargs)
			buf.setLength(buf.length()-1);
		buf.append(")>");
		return buf.toString();
	}
	
	private static String processReverse(String type) {
		type = type.trim();
		if(type.length() == 1)  {
			for(String s : typeToSymbol.keySet()) 
				if(typeToSymbol.get(s).equals(type))
					return s;
			throw new IllegalArgumentException("Invalid type string");
		}
			
		if(type.startsWith("[")) {
			// is an array
			int idx = 0;
			String suffix = "";
			while(type.charAt(idx) == '[') {
				idx++;
				suffix = suffix+"[]";
			}
			return processReverse(type.substring(idx))+suffix;
			
		} else {
			type = type.replaceAll("/", ".");
			type = type.substring(1, type.length()-1); //remove L and ;
			return type;
		}
	}
	
	public static void writeToFile(File file, String content) {
		FileOutputStream fop = null;
		
		try {
			fop = new FileOutputStream(file); 
			if (!file.exists()) 
				file.createNewFile();		 
			byte[] contentInBytes = content.getBytes();
 			fop.write(contentInBytes);
			fop.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fop != null) {
					fop.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		String s = "<java.awt.geom.PathIterator: int currentSegment(float[])>";
		MethodDescriptor desc = getMethodDesc(s);
		System.out.println(s);
		System.out.println(getMethodDesc(desc));
		
	}
}
