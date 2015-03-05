package edu.ucla;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.MethodVisitor;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;

public class PartialInstrumentationInferencerMV extends MethodVisitor {

	MethodDescriptor desc;
	Map<MethodDescriptor, List<MethodDescriptor>> map = new HashMap<MethodDescriptor, List<MethodDescriptor>>();
	List<String> superClass;
	
	public PartialInstrumentationInferencerMV(int api, MethodDescriptor desc, MethodVisitor next, Map<MethodDescriptor, List<MethodDescriptor>> map, List<String> superClass) {
		super(api, next);
		this.desc = desc;
		this.map = map;
		this.superClass = superClass;
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {
		if(name.equals("getResourceAsStream") && owner.equals("java/lang/Class")) 
			SelectiveInstrumentationManager.methodsToInstrument.add(this.desc);
		
		MethodDescriptor caller = this.desc;
		MethodDescriptor callee = new MethodDescriptor(name, owner, desc);
		
		Type calleeType = Type.getReturnType(desc);
		Type[] argTypes = Type.getArgumentTypes(desc);
		
		if(SelectiveInstrumentationManager.methodsToInstrument.contains(caller) 
				&& !SelectiveInstrumentationManager.methodsToInstrument.contains(callee)) {
			for(Type t : argTypes) {
				if((t.getSort() == Type.ARRAY && t.getDimensions() > 1) || t.getDescriptor().equals("Ljava/lang/Object;")) {
					System.out.println("[PTI-visitMethodInsn] Adding additional method");
					SelectiveInstrumentationManager.methodsToInstrument.add(callee);
					break;
				}
			}
		}
		
		if(SelectiveInstrumentationManager.methodsToInstrument.contains(caller) 
				&& !callee.getOwner().startsWith("java/") && !callee.getOwner().startsWith("sun/") 
				&& !callee.getOwner().startsWith("javax/")) {
			if(calleeType.getSort() == Type.ARRAY && calleeType.getDimensions() > 1 
					&& !SelectiveInstrumentationManager.methodsToInstrument.contains(callee)) {
				System.out.println("[PTI-visitMethodInsn] Adding additional method");
				SelectiveInstrumentationManager.methodsToInstrument.add(callee);
			}
			
		}
		map.get(this.desc).add(new MethodDescriptor(name, owner, desc));
		super.visitMethodInsn(opcode, owner, name, desc, itf);
	}
	
	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
		Type fieldType = Type.getType(desc);
		
		FieldDescriptor fdesc = new FieldDescriptor(name, owner, desc);
		if((opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC) 
				&& fieldType.getSort() == Type.ARRAY && fieldType.getDimensions() == 1) {
				System.out.println("[PTI-visitFieldInsn] Adding additional method");
				SelectiveInstrumentationManager.methodsToInstrument.add(this.desc);
			
		}

		// TODO test if the second case works
		/*
		 * putfield 
		 * putstatic
		 * getfield
		 * getstatic
		 */
		if(fieldType.getSort() == Type.ARRAY && fieldType.getDimensions() > 1)  {
			System.out.println("[PTI-visitFieldInsn] Adding additional method");
			SelectiveInstrumentationManager.methodsToInstrument.add(this.desc);
		}
		super.visitFieldInsn(opcode, owner, name, desc);
	}
}
