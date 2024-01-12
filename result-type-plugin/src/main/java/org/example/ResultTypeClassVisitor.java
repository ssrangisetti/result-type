package org.example;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;

import java.net.URLClassLoader;

import static org.objectweb.asm.Opcodes.ASM9;

public class ResultTypeClassVisitor extends ClassVisitor {
    private String className;
    private URLClassLoader urlClassLoader;

    protected ResultTypeClassVisitor(int api, ClassVisitor classVisitor, URLClassLoader urlClassLoader) {
        super(api, classVisitor);
        this.urlClassLoader = urlClassLoader;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String methodName, String methodDescriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.cv.visitMethod(access, methodName, methodDescriptor, signature, exceptions);
        ResultTypeMethodVisitor visitor = new ResultTypeMethodVisitor(ASM9, methodVisitor, methodName, methodDescriptor, signature, className, urlClassLoader);
        visitor.aa = new AnalyzerAdapter(className, access, methodName, methodDescriptor, visitor);
        visitor.lvs = new LocalVariablesSorter(access, methodDescriptor, visitor.aa);
        return visitor.lvs;
    }
}
