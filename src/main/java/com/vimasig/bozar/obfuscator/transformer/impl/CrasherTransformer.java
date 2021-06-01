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

    public static final String CLASS_NAME = "BOZAR";
    public static final String REPEAT_BASE = "\u0001/";

    public CrasherTransformer(Bozar bozar) {
        super(bozar, bozar.getConfig().getOptions().isCrasher());
    }

    @Override
    public void transformOutput(JarOutputStream jarOutputStream) {
        ClassNode invalid = new ClassNode();
        invalid.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, CLASS_NAME + REPEAT_BASE.repeat((Character.MAX_VALUE / REPEAT_BASE.length()) - CLASS_NAME.length()), null, "java/lang/Object", null);
        try {
            jarOutputStream.putNextEntry(new JarEntry("\u0020".repeat(4) + ".class"));
            jarOutputStream.write(ASMUtils.toByteArrayDefault(invalid));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
