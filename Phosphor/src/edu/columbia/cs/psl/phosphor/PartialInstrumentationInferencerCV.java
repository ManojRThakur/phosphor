package edu.columbia.cs.psl.phosphor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.ClassVisitor;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.FieldVisitor;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.MethodVisitor;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;

/**
 * Infers additional methods to instrument apart from the list fed to phosphor
 * 
 * @author manojthakur
 *
 */
public class PartialInstrumentationInferencerCV extends ClassVisitor{
	
	public static List<FieldDescriptor> multidim_array_fields_non_private = new ArrayList<FieldDescriptor>();
	public static List<String> classesSeenTillNow = new ArrayList<String>(); 
	
	List<FieldDescriptor> multidim_array_fields = new ArrayList<FieldDescriptor>();
	List<MethodDescriptor> methodCallingAsStream = new ArrayList<MethodDescriptor>();
	String className;
	boolean isInterface = false;
	Map<MethodDescriptor, List<MethodDescriptor>> map = new HashMap<MethodDescriptor, List<MethodDescriptor>>();
	String[] interfaces = null;
	String superClass;
	
	public PartialInstrumentationInferencerCV()  {
		super(Opcodes.ASM5);
	}
	
	public PartialInstrumentationInferencerCV(final ClassVisitor cv) {
		super(Opcodes.ASM5, cv);
	}
		
	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		this.className = name;
		this.interfaces = interfaces;
		this.superClass = superName;
		
		if((access & Opcodes.ACC_INTERFACE) != 0 || (access & Opcodes.ACC_ABSTRACT) != 0)
			isInterface = true;
		super.visit(version, access, name, signature, superName, interfaces);
	}
	
	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		Type fieldType = Type.getType(desc);
		
		// is this field an array with dim > 1
		if(fieldType.getSort() == Type.ARRAY && fieldType.getDimensions() > 1) {
			FieldDescriptor fdesc = new FieldDescriptor(name, className, desc);

			multidim_array_fields.add(fdesc);
			if(((access & Opcodes.ACC_PUBLIC) != 0) || ((access & Opcodes.ACC_PROTECTED) != 0))
				multidim_array_fields_non_private.add(fdesc);
		}
		
		return super.visitField(access, name, desc, signature, value);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		
		MethodDescriptor mdesc = new MethodDescriptor(name, className, desc);
		for(String inter : interfaces) {
			MethodDescriptor supr = new MethodDescriptor(name, inter, desc);
			if(SelectiveInstrumentationManager.methodsToInstrument.contains(supr)) {
				SelectiveInstrumentationManager.methodsToInstrument.add(mdesc);
				break;
			}
		}
		MethodDescriptor supr = new MethodDescriptor(name, superClass, desc);
		
		if(SelectiveInstrumentationManager.methodsToInstrument.contains(supr))
			SelectiveInstrumentationManager.methodsToInstrument.add(mdesc);
		
		MethodVisitor next = super.visitMethod(access, name, desc, signature, exceptions);
		map.put(mdesc, new ArrayList<MethodDescriptor>());
		
		return new PartialInstrumentationInferencerMV(Opcodes.ASM5, mdesc, next, this.multidim_array_fields, map);
	}
	
	@Override
	public void visitEnd() {
		classesSeenTillNow.add(className);
		super.visitEnd();
	}
}
