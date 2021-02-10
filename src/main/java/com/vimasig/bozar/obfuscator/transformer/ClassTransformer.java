package com.vimasig.bozar.obfuscator.transformer;

import com.vimasig.bozar.obfuscator.Bozar;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Random;

public class ClassTransformer implements Opcodes {

    private final Bozar bozar;
    protected final Random random = new Random();

    public ClassTransformer(Bozar bozar) {
        this.bozar = bozar;
    }

    public void transformClass(ClassNode classNode) {}
    public void transformMethod(ClassNode classNode, MethodNode methodNode) {}
    public void transformField(ClassNode classNode, FieldNode fieldNode) {}

    public final Bozar getBozar() {
        return bozar;
    }

    public final String getName() {
        return this.getClass().getSimpleName();
    }
}
