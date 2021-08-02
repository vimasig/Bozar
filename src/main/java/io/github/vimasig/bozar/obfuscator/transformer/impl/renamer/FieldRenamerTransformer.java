package io.github.vimasig.bozar.obfuscator.transformer.impl.renamer;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.RenamerTransformer;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class FieldRenamerTransformer extends RenamerTransformer {

    public FieldRenamerTransformer(Bozar bozar) {
        super(bozar, bozar.getConfig().getOptions().getRename() != BozarConfig.BozarOptions.RenameOption.OFF);
    }

    @Override
    public void transformClass(ClassNode classNode) {
        // Map all fields in this class node and its super classes (if not mapped)
        getSuperHierarchy(classNode)
                .forEach(cn -> cn.fields.stream()
                        .filter(fieldNode -> !this.isMapRegistered(getFieldMapFormat(cn, fieldNode)))
                        .forEach(fieldNode -> this.registerMap(getFieldMapFormat(cn, fieldNode)))
                );

        // Apply map to upper classes if a mapping is applied to a subclass
        // So our mapper can rename references that access subfields from upper classes
        getSuperHierarchy(classNode)
                .forEach(cn -> cn.fields.stream()
                        .filter(fieldNode -> this.isMapRegistered(getFieldMapFormat(cn, fieldNode)))
                        .filter(fieldNode -> !this.isMapRegistered(getFieldMapFormat(classNode, fieldNode)))
                        .forEach(fieldNode -> this.registerMap(getFieldMapFormat(classNode, fieldNode), this.map.get(getFieldMapFormat(cn, fieldNode))))
                );
    }

    private static String getFieldMapFormat(ClassNode classNode, FieldNode fieldNode) {
        return classNode.name + "." + fieldNode.name;
    }
}
