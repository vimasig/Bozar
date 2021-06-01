package com.vimasig.bozar.obfuscator.transformer.impl;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.transformer.ClassTransformer;
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
