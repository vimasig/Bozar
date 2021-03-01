package com.vimasig.bozar.obfuscator.transformer.impl;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import com.vimasig.bozar.obfuscator.utils.ASMUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class CrasherTransformer extends ClassTransformer {

    public CrasherTransformer(Bozar bozar) {
        super(bozar, bozar.getConfig().getOptions().isCrasher());
    }

    @Override
    public void transformOutput(JarOutputStream jarOutputStream) {
        String className = "BOZAR";
        String repeatBase = "\u0001/";
        ClassNode invalid = new ClassNode();
        invalid.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, className + repeatBase.repeat((Character.MAX_VALUE / repeatBase.length()) - className.length()), null, "java/lang/Object", null);
        try {
            jarOutputStream.putNextEntry(new JarEntry("\u0020".repeat(4) + ".class"));
            jarOutputStream.write(ASMUtils.toByteArrayDefault(invalid));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
