package com.vimasig.bozar.obfuscator.transformer.impl.renamer;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.transformer.RenamerTransformer;
import com.vimasig.bozar.obfuscator.utils.model.ResourceWrapper;
import org.objectweb.asm.tree.ClassNode;

public class ClassRenamerTransformer extends RenamerTransformer {

    public ClassRenamerTransformer(Bozar bozar) {
        super(bozar);
    }

    private char obfName = '\u2000';

    @Override
    public void transformClass(ClassNode classNode) {
        if(!this.getBozar().getConfig().getOptions().isRename()) return;
        map.put(classNode.name, String.valueOf(obfName++));
    }

    @Override
    public void transformResource(ResourceWrapper resource) {
        if(resource.getZipEntry().isDirectory()) return;

        String str = new String(resource.getBytes());
        for (var set : map.entrySet()) {
            String s1 = set.getKey().replace("/", ".");
            String s2 = set.getValue().replace("/", ".");
            str = str.replace(s1, s2);
        } resource.setBytes(str.getBytes());
    }
}
