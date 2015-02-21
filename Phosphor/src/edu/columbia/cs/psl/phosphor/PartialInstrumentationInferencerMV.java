package edu.columbia.cs.psl.phosphor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.MethodVisitor;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;

public class PartialInstrumentationInferencerMV extends MethodVisitor {

	List<FieldDescriptor> multidim_array_fields = new ArrayList<FieldDescriptor>();
	MethodDescriptor desc;
	Map<MethodDescriptor, List<MethodDescriptor>> map = new HashMap<MethodDescriptor, List<MethodDescriptor>>();
	
	public PartialInstrumentationInferencerMV(int api, MethodDescriptor desc, MethodVisitor next,
			List<FieldDescriptor> multidim_array_fields, Map<MethodDescriptor, List<MethodDescriptor>> map) {
		super(api, next);
		this.desc = desc;
		this.multidim_array_fields = multidim_array_fields;
		this.map = map;
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {
		if(name.equals("getResourceAsStream") && owner.equals("java/lang/Class")) {
			System.out.println("[PTI] adding " + this.desc);
			//AdditionalMethodsToTaint.methodsWithGetAsSrtream.add(this.desc);
			SelectiveInstrumentationManager.methodsToInstrument.add(this.desc);
		}
		MethodDescriptor caller = this.desc;
		MethodDescriptor callee = new MethodDescriptor(name, owner, desc);
		
		Type calleeType = Type.getReturnType(desc);
		Type[] argTypes = Type.getArgumentTypes(desc);
		
		if(SelectiveInstrumentationManager.methodsToInstrument.contains(caller) 
				&& !SelectiveInstrumentationManager.methodsToInstrument.contains(callee)) {
			for(Type t : argTypes) {
				if(t.getSort() == Type.ARRAY && t.getDimensions() > 1) {
					SelectiveInstrumentationManager.methodsToInstrument.add(callee);
				}
			}
		}
		
		if(SelectiveInstrumentationManager.methodsToInstrument.contains(caller) 
				&& !callee.getOwner().startsWith("java/") && !callee.getOwner().startsWith("sun/") 
				&& !callee.getOwner().startsWith("javax/")) {
			if(calleeType.getSort() == Type.ARRAY && calleeType.getDimensions() > 1 
					&& !SelectiveInstrumentationManager.methodsToInstrument.contains(callee)) {
				System.out.println("[PTI] adding " + callee);
	//			if(PartialInstrumentationInferencerCV.classesSeenTillNow.contains(callee.getOwner()))
	//				System.out.println("Noooooo " + callee);
				SelectiveInstrumentationManager.methodsToInstrument.add(callee);
			}
			
		}
		map.get(this.desc).add(new MethodDescriptor(name, owner, desc));
		super.visitMethodInsn(opcode, owner, name, desc, itf);
	}
	
	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
		FieldDescriptor fdesc = new FieldDescriptor(name, owner, desc);
		// TODO test if the second case works
		/*
		 * putfield 
		 * putstatic
		 * getfield
		 * getstatic
		 */
		if(multidim_array_fields.contains(fdesc) || PartialInstrumentationInferencerCV.multidim_array_fields_non_private.contains(fdesc))  {
			System.out.println("[PTI] adding " + this.desc);
			//AdditionalMethodsToTaint.methodsAccessingMultiDimensionalArrays.add(this.desc);
			SelectiveInstrumentationManager.methodsToInstrument.add(this.desc);
		}
		super.visitFieldInsn(opcode, owner, name, desc);
	}
}
