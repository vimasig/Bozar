package io.github.vimasig.bozar.obfuscator.transformer.impl;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarCategory;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;

public class InnerClassTransformer extends ClassTransformer {

    public InnerClassTransformer(Bozar bozar) {
        super(bozar, "Remove inner classes", BozarCategory.STABLE);
    }

    public void transformClass(ClassNode classNode) {
        classNode.innerClasses = new ArrayList<>();
    }

    @Override
    public BozarConfig.EnableType getEnableType() {
        return new BozarConfig.EnableType(() -> this.getBozar().getConfig().getOptions().isRemoveInnerClasses(), boolean.class);
    }
}
