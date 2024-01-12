package org.example;

import java.util.List;

public record JavaType(
        String name,
        boolean parameterized,
        int arrayDimensions,
        BoundsType boundType,
        List<JavaType> typeArguments
) {
    public enum BoundsType {
        EXTENDS,
        SUPER,
        EQUALS,
        UNBOUNDED;
    }
}
