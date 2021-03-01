package com.vimasig.bozar.obfuscator.transformer.impl;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import com.vimasig.bozar.obfuscator.utils.ASMUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class WatermarkTransformer extends ClassTransformer {

    public WatermarkTransformer(Bozar bozar) {
        super(bozar, bozar.getConfig().getOptions().getWatermarkOptions().isDummyClass());
    }

    @Override
    public void transformOutput(JarOutputStream jarOutputStream) {
        ClassNode dummy = new ClassNode();
        dummy.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, this.getBozar().getConfig().getOptions().getWatermarkOptions().getDummyClassText(), null, "java/lang/Object", null);
        dummy.visitMethod(random.nextInt(100), "\u0001", "(\u0001/)L\u0001/;", null, null);
        try {
            jarOutputStream.putNextEntry(new JarEntry(dummy.name + ".class"));
            jarOutputStream.write(ASMUtils.toByteArrayDefault(dummy));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
