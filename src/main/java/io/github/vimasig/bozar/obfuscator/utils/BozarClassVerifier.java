package io.github.vimasig.bozar.obfuscator.utils;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.impl.CrasherTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BozarClassVerifier {

    public static boolean verify(Bozar bozar, Path path, ClassLoader parent) throws IOException {
        var classLoader = new URLClassLoader(new URL[] { path.toFile().toURI().toURL() }, parent);
        var classes = new ArrayList<byte[]>();

        // Load JAR
        try (var jarInputStream = new ZipInputStream(Files.newInputStream(path))) {
            ZipEntry zipEntry;
            while ((zipEntry = jarInputStream.getNextEntry()) != null) {
                if (zipEntry.getName().endsWith(".class")) {
                    classes.add(StreamUtils.readAll(jarInputStream));
                }
            }
        }

        // Loop classes of loaded JAR
        boolean allOK = true;
        for (byte[] classBytes : classes) {
            ClassReader reader = new ClassReader(classBytes);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            // Skip watermark class because we know it's already invalid
            if(classNode.methods.stream().anyMatch(methodNode -> methodNode.name.equals("\u0001") && methodNode.desc.equals("(\u0001/)L\u0001/;")))
                continue;
            // Skip crasher class
            if(classNode.name.startsWith(CrasherTransformer.CLASS_NAME + CrasherTransformer.REPEAT_BASE.repeat(10)))
                continue;

            try {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                CheckClassAdapter.verify(reader, classLoader, false, printWriter);
                if (!stringWriter.toString().isEmpty()) {
                    allOK = false;
                    bozar.err("Cannot verify class");
                    bozar.err(stringWriter.toString());
                }
            } catch (Throwable t) {
                allOK = false;
                bozar.err("Cannot verify class: %s", classNode.name);
                t.printStackTrace();
            }
        }
        return allOK;
    }
}
