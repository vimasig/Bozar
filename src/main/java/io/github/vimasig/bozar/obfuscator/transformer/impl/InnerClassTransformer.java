package io.github.vimasig.bozar.obfuscator.transformer.impl;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;

public class InnerClassTransformer extends ClassTransformer {

    public InnerClassTransformer(Bozar bozar) {
        super(bozar, bozar.getConfig().getOptions().isRemoveInnerClasses());
    }

    public void transformClass(ClassNode classNode) {
        classNode.innerClasses = new ArrayList<>();
    }
}
