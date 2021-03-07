package com.vimasig.bozar.obfuscator.transformer.impl.renamer;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.transformer.RenamerTransformer;
import com.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import com.vimasig.bozar.obfuscator.utils.model.ResourceWrapper;
import org.objectweb.asm.tree.ClassNode;

public class ClassRenamerTransformer extends RenamerTransformer {

    public ClassRenamerTransformer(Bozar bozar) {
        super(bozar, bozar.getConfig().getOptions().getRename() != BozarConfig.BozarOptions.RenameOption.OFF);
    }

    @Override
    public void transformClass(ClassNode classNode) {
        this.registerMap(classNode.name);
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
