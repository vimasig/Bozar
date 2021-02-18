package com.vimasig.bozar.obfuscator.transformer.impl;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import org.objectweb.asm.tree.ClassNode;

public class SourceFileTransformer extends ClassTransformer {

    public SourceFileTransformer(Bozar bozar) {
        super(bozar, bozar.getConfig().getOptions().isRemoveSourceFile());
    }

    @Override
    public void transformClass(ClassNode classNode) {
        classNode.sourceFile = "";
        classNode.sourceDebug = "";
    }
}
