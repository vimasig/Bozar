package com.vimasig.bozar.obfuscator.transformer.impl.renamer;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.transformer.RenamerTransformer;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class FieldRenamerTransformer extends RenamerTransformer {

    public FieldRenamerTransformer(Bozar bozar) {
        super(bozar);
    }

    private char obfName = '\u2000';

    @Override
    public void transformField(ClassNode classNode, FieldNode fieldNode) {
        if(!this.getBozar().getConfig().getOptions().isRename()) return;
        map.put(classNode.name + "." + fieldNode.name, String.valueOf(obfName++));
    }
}
