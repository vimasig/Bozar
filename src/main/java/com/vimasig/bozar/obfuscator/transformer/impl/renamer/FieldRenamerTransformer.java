package com.vimasig.bozar.obfuscator.transformer.impl.renamer;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.transformer.RenamerTransformer;
import com.vimasig.bozar.obfuscator.utils.ASMUtils;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class FieldRenamerTransformer extends RenamerTransformer {

    public FieldRenamerTransformer(Bozar bozar) {
        super(bozar, bozar.getConfig().getOptions().isRename());
    }

    // TODO: Ability to choose dictionary
    private char obfName = '\u2000';

    @Override
    public void transformField(ClassNode classNode, FieldNode fieldNode) {
        map.put(ASMUtils.getName(classNode, fieldNode), String.valueOf(obfName++));
    }
}
