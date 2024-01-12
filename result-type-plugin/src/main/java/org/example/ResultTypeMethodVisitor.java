package org.example;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.util.TraceSignatureVisitor;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

public class ResultTypeMethodVisitor extends MethodVisitor {

    LocalVariablesSorter lvs;
    AnalyzerAdapter aa;

    private String methodDescriptor;
    private String signature;
    private String className;
    private URLClassLoader classLoader;
    private String printableSignature;

    protected ResultTypeMethodVisitor(int api, MethodVisitor mv, String methodName, String methodDescriptor, String signature, String className, URLClassLoader classLoader) {
        super(api, mv);
        this.methodDescriptor = methodDescriptor;
        this.signature = signature;
        this.className = className.replace("/", ".");
        this.classLoader = classLoader;
        this.printableSignature = getPrintableSignature(methodName, methodDescriptor, signature);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (opcode == INVOKEINTERFACE && owner.equals("org/example/Result") && name.equals("tryGet")) {
            JavaType errType = validateAndGetErrType();

            int id = lvs.newLocal(Type.getType("Lorg/example/Result;"));
            aa.visitVarInsn(ASTORE, id);
            aa.visitVarInsn(ALOAD, id);
            List<Object> locals = new ArrayList<>(aa.locals);
            aa.visitTypeInsn(INSTANCEOF, "org/example/Result$Ok");
            Label label = new Label();
            aa.visitJumpInsn(IFNE, label);
            List<Object> stack = new ArrayList<>(aa.stack);
            aa.visitVarInsn(ALOAD, id);
            aa.visitTypeInsn(CHECKCAST, "org/example/Result$Err");
            aa.visitMethodInsn(INVOKEVIRTUAL, "org/example/Result$Err", "error", "()Lorg/example/IError;", false);
            aa.visitMethodInsn(INVOKESTATIC, errType.name(), "from", "(Lorg/example/IError;)L" + errType.name() + ";", false);
            aa.visitMethodInsn(INVOKESTATIC, "org/example/Result$Err", "fromError", "(Lorg/example/IError;)Lorg/example/Result;", false);
            aa.visitInsn(ARETURN);
            aa.visitLabel(label);
            aa.visitFrame(F_NEW, locals.size(), locals.toArray(), stack.size(), stack.toArray());
            aa.visitVarInsn(ALOAD, id);
            aa.visitTypeInsn(CHECKCAST, "org/example/Result$Ok");
            aa.visitMethodInsn(INVOKEVIRTUAL, "org/example/Result$Ok", "data", "()Ljava/lang/Object;", false);
        } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }

    private JavaType validateAndGetErrType() {
        if (!Type.getReturnType(methodDescriptor).toString().equals("Lorg/example/Result;")) {
            throw new RuntimeException("tryGet() is used in a method for which the return type is not org.example.Result. signature: %s in class: %s. use unwrap() instead of tryGet() or return org.example.Result".formatted(printableSignature, className));
        }
        SignatureReader signatureReader = new SignatureReader(Type.getReturnType(signature == null ? methodDescriptor : signature).getDescriptor());
        ResultSignatureVisitor resultSignatureVisitor = new ResultSignatureVisitor(ASM9);
        signatureReader.acceptType(resultSignatureVisitor);

        JavaType errType = getErrType(resultSignatureVisitor.toJavaType());
        checkFromMethodExists(errType);

        return errType;
    }

    private void checkFromMethodExists(JavaType errType) {
        try {
            Class<?> cls = Class.forName(errType.name().replace("/", "."), false, classLoader);
            Method method = cls.getMethod("from", Class.forName("org.example.IError", false, classLoader));
            Set<AccessFlag> accessFlags = method.accessFlags();
            if (!accessFlags.contains(AccessFlag.PUBLIC) || !accessFlags.contains(AccessFlag.STATIC)) {
                throw new RuntimeException("from() method must be public and static for the error type: %s".formatted(errType.name()));
            }
            if (method.getReturnType() != cls) {
                throw new RuntimeException("from() method must return the same type as the class for the error type: %s".formatted(errType.name()));
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not find Error class type %s for the method with signature: %s in class: %s".formatted(errType.name(), printableSignature, className), e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not find the static from method in the error type %s for the method with signature: %s in class: %s".formatted(errType.name(), printableSignature, className), e);
        }
    }

    private JavaType getErrType(JavaType javaType) {
        JavaType errType;
        if (javaType.typeArguments().isEmpty()) {
            errType = new JavaType("org/example/StdError", false, 0, JavaType.BoundsType.EQUALS, new ArrayList<>());
        } else {
            errType = javaType.typeArguments().get(1);
        }
        if (errType.boundType() != JavaType.BoundsType.EQUALS || errType.parameterized()) {
            throw new RuntimeException("Error type is either parameterized or unbounded for the method with signature: %s in class: %s. use unwrap() instead of tryGet() or do not use parameters like T,U or ? in the error type".formatted(printableSignature, className));
        }
        if (errType.arrayDimensions() != 0) {
            throw new RuntimeException("Error type is an array for the method with signature: %s in class: %s. use unwrap() instead of tryGet() or do not use arrays in the error type".formatted(printableSignature, className));
        }
        return errType;
    }

    private String getPrintableSignature(String methodName, String methodDescriptor, String signature) {
        SignatureReader signatureReader = new SignatureReader(signature == null ? methodDescriptor : signature);
        TraceSignatureVisitor signatureVisitor = new TraceSignatureVisitor(0);
        signatureReader.accept(signatureVisitor);
        String signatureNoName = signatureVisitor.getDeclaration();
        int index = signatureNoName.indexOf('(');
        return signatureVisitor.getReturnType() + " " + signatureNoName.substring(0, index) + methodName + signatureNoName.substring(index);
    }

}
