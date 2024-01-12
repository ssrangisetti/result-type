package org.example;

import org.objectweb.asm.signature.SignatureVisitor;

import java.util.*;

public class ResultSignatureVisitor extends SignatureVisitor {
    private static final String TYPE_ONLY_ERROR = "This visitor is valid only for types and not methods. Use acceptType() instead.";
    /*
    interface no
    abstract class no
    concrete class yes
    inner class no
    inner static class yes
    generic class yes but cannot use typeArguments
    array type no
    enum type yes static factory method
     */

    private String name;
    private JavaType.BoundsType boundType;
    private boolean parameterized;
    private List<ResultSignatureVisitor> typeArgumentVisitors = new ArrayList<>();
    private ResultSignatureVisitor arrayVisitor = null;
    private List<JavaType> unboundTypeArgs = new ArrayList<>();
    private static final Map<Character, String> BASE_TYPES;

    static {
        HashMap<Character, String> baseTypes = new HashMap<>();
        baseTypes.put('Z', "boolean");
        baseTypes.put('B', "byte");
        baseTypes.put('C', "char");
        baseTypes.put('S', "short");
        baseTypes.put('I', "int");
        baseTypes.put('J', "long");
        baseTypes.put('F', "float");
        baseTypes.put('D', "double");
        baseTypes.put('V', "void");
        BASE_TYPES = Collections.unmodifiableMap(baseTypes);
    }

    public ResultSignatureVisitor(int api, char wildcard) {
        super(api);
        switch (wildcard) {
            case '+' -> boundType = JavaType.BoundsType.EXTENDS;
            case '-' -> boundType = JavaType.BoundsType.SUPER;
            case '=' -> boundType = JavaType.BoundsType.EQUALS;
            default -> throw new IllegalStateException("Unexpected bound type wildcard: " + wildcard);
        }
    }

    public ResultSignatureVisitor(int api) {
        super(api);
        this.boundType = JavaType.BoundsType.UNBOUNDED;
    }

    @Override
    public void visitFormalTypeParameter(String name) {
        throw new IllegalStateException(TYPE_ONLY_ERROR);
    }

    @Override
    public SignatureVisitor visitClassBound() {
        throw new IllegalStateException(TYPE_ONLY_ERROR);
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        throw new IllegalStateException(TYPE_ONLY_ERROR);
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        throw new IllegalStateException(TYPE_ONLY_ERROR);
    }

    @Override
    public SignatureVisitor visitInterface() {
        throw new IllegalStateException(TYPE_ONLY_ERROR);
    }

    @Override
    public SignatureVisitor visitParameterType() {
        throw new IllegalStateException(TYPE_ONLY_ERROR);
    }

    @Override
    public SignatureVisitor visitReturnType() {
        throw new IllegalStateException(TYPE_ONLY_ERROR);
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        throw new IllegalStateException(TYPE_ONLY_ERROR);
    }

    @Override
    public void visitBaseType(char descriptor) {
        name = BASE_TYPES.get(descriptor);
    }

    @Override
    public void visitTypeVariable(String name) {
        this.name = name;
        this.parameterized = true;
    }

    @Override
    public SignatureVisitor visitArrayType() {
        arrayVisitor = new ResultSignatureVisitor(api);
        return arrayVisitor;
    }

    @Override
    public void visitClassType(String name) {
        this.name = name;
    }

    @Override
    public void visitInnerClassType(String name) {
        this.name += "$" + name;
    }

    @Override
    public void visitTypeArgument() {
        unboundTypeArgs.add(new JavaType(null, false, 0, JavaType.BoundsType.UNBOUNDED, null));
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        ResultSignatureVisitor signatureVisitor = new ResultSignatureVisitor(api, wildcard);
        typeArgumentVisitors.add(signatureVisitor);
        return signatureVisitor;
    }

    public JavaType toJavaType() {
        JavaType jType;
        if (arrayVisitor != null) {
            JavaType arrayType = arrayVisitor.toJavaType();
            jType = new JavaType(arrayType.name(), arrayType.parameterized(), arrayType.arrayDimensions() + 1, arrayType.boundType(), arrayType.typeArguments());
        } else {
            List<JavaType> typeArguments = new ArrayList<>(unboundTypeArgs);
            for (ResultSignatureVisitor visitor : typeArgumentVisitors) {
                typeArguments.add(visitor.toJavaType());
            }
            jType = new JavaType(name, parameterized, 0, boundType, typeArguments);
        }
        return jType;
    }

}
