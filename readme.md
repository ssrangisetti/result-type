# Rust's result type and question mark like operator for java

This repository contains a simple implementation of Rust's result type and question mark like operator for java.
Since `?` is already symbol for defining types in java and no other way of calling it is syntactically valid we will use
a method tryGet() to achieve the same effect. This plugin works by replacing the bytecode of `result.tryGet()` with
the bytecode equivalent to below code

```java
if (!result instanceof Ok) {
    return Err(ErrorType.from(result.error())
}
```
*Warning: This plugin is a project for fun and not tested in production and may have bugs. Please use it at your own risk.*
## How to use

The module `test-module` contains sample code that shows how to use the result type and the tryGet() method. The
module `result` contains the implementation of the result type and the tryGet() method.

* Clone this repository and run `mvn clean install`. This step is required because these artifacts are not available in
  any maven repository.
* Add the following dependency to your pom.xml

```xml

<dependency>
    <groupId>org.example</groupId>
    <artifactId>result-type-api</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

* Add the result-type-plugin to your pom.xml

```xml

<build>
    <plugins>
        <plugin>
            <groupId>org.example</groupId>
            <artifactId>result-type-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <id>return-result</id>
                    <phase>prepare-package</phase>
                    <goals>
                        <goal>process</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

* Start using the result type and the tryGet() method. ensure that any function that uses the tryGet() method returns a
  Result type.

```java
public Result<String, StdError> method1() {
    Result<String, StdError> res = getRes();
    System.out.println(res.errorContext("Failed to get hello").tryGet());
    return res;
}
```

## Caveats

* Any method that uses tryGet() must return a Result type.
* The Error part of the Returned Result type must not be a generic type like `T`, `U` or an unbounded type like `?`.
* You can define your own error type by implementing the `IError` interface. but ensure that the type implements a
  static method `from` that takes `IError` as input and returns the error type. look at the `StdError` class for an
  example.
    * The static method is chosen over constructor because
        1. You cannot call constructors in enums, interfaces or abstract classes
        2. This is much closer to Rust's api.
        3. Constructors take 4 instructions (new, dup, current error, <init>) and 2 new entries in the stack
* Sometimes you may get error that StackMapFrames are inconsistent. This is because I don't fully understand it and
  somehow made it work. Incase you face this issue the workaround is to comment
  the `aa.visitFrame(F_NEW, locals.size(), locals.toArray(), stack.size(), stack.toArray());` line
  in `ResultTypeMethodVisitor` class and run your java application with `-noverify` flag
    * This is not a good solution and if someone knows how to fix this please raise a PR.
