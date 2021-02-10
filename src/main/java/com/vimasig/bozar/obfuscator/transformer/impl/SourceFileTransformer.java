package com.vimasig.bozar.obfuscator.transformer.impl;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import org.objectweb.asm.tree.ClassNode;

public class SourceFileTransformer extends ClassTransformer {

    public SourceFileTransformer(Bozar bozar) {
        super(bozar);
    }

    @Override
    public void transformClass(ClassNode classNode) {
        if(!this.getBozar().getConfig().getOptions().isRemoveSourceFile()) return;
        classNode.sourceFile = "";
        classNode.sourceDebug = "";
    }
}
