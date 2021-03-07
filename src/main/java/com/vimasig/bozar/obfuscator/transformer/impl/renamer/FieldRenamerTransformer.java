package com.vimasig.bozar.obfuscator.transformer.impl.renamer;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.transformer.RenamerTransformer;
import com.vimasig.bozar.obfuscator.utils.ASMUtils;
import com.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class FieldRenamerTransformer extends RenamerTransformer {

    public FieldRenamerTransformer(Bozar bozar) {
        super(bozar, bozar.getConfig().getOptions().getRename() != BozarConfig.BozarOptions.RenameOption.OFF);
    }

    @Override
    public void transformClass(ClassNode classNode) {
        this.index = 0;
    }

    @Override
    public void transformField(ClassNode classNode, FieldNode fieldNode) {
        this.registerMap(ASMUtils.getName(classNode, fieldNode));
    }
}
