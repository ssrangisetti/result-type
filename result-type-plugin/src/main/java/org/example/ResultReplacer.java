package org.example;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static org.objectweb.asm.Opcodes.ASM9;

public class ResultReplacer {

    private final File classes;
    private final URLClassLoader urlClassLoader;

    public ResultReplacer(File classes, URLClassLoader urlClassLoader) {
        this.classes = classes;
        this.urlClassLoader = urlClassLoader;
    }

    public void process() throws IOException {
        final Path root = classes.toPath();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith(".class")) {
                    replace(file.toFile());
                }
                return super.visitFile(file, attrs);
            }
        });
    }

    public void replace(File file) throws IOException {
        ClassWriter cw = new ClassWriter(0);
        ClassVisitor cv = new ResultTypeClassVisitor(ASM9, cw, urlClassLoader);

        try (InputStream inputStream = new FileInputStream(file)) {
            ClassReader cr = new ClassReader(inputStream);
            cr.accept(cv, ClassReader.EXPAND_FRAMES);
        }
        byte[] byteArray = cw.toByteArray();
        Files.write(file.toPath(), byteArray);

    }
}
