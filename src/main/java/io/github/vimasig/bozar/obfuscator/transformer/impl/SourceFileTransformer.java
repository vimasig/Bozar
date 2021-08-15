package io.github.vimasig.bozar.obfuscator.transformer.impl;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarCategory;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.ClassNode;

public class SourceFileTransformer extends ClassTransformer {

    public SourceFileTransformer(Bozar bozar) {
        super(bozar, "Remove SourceFile", BozarCategory.STABLE);
    }

    @Override
    public void transformClass(ClassNode classNode) {
        classNode.sourceFile = "";
        classNode.sourceDebug = "";
    }

    @Override
    public BozarConfig.EnableType getEnableType() {
        return new BozarConfig.EnableType(() -> this.getBozar().getConfig().getOptions().isRemoveSourceFile(), boolean.class);
    }
}
