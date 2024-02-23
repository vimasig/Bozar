package io.github.vimasig.bozar.obfuscator.transformer.impl;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarCategory;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class HideCodeTransformer extends ClassTransformer {
    public HideCodeTransformer(Bozar bozar) {
        super(bozar, "Hide Code", BozarCategory.ADVANCED);
    }

    @Override
    public void transformClass(ClassNode node) {

        // https://github.com/KgDW/GOTOObfuscator/blob/main/src/main/java/org/gotoobfuscator/transformer/transformers/HideCode.java

        if (((node.visibleAnnotations == null || node.visibleAnnotations.isEmpty()) && (node.invisibleAnnotations == null || node.invisibleAnnotations.isEmpty())) && notSynthetic(node.access)) {
            node.access |= ACC_SYNTHETIC;
        }

        for (FieldNode fieldNode : node.fields) {
            if ((fieldNode.visibleAnnotations != null && fieldNode.visibleAnnotations.isEmpty()) || (fieldNode.invisibleAnnotations != null && fieldNode.invisibleAnnotations.isEmpty())) {
                continue;
            }

            if (notSynthetic(fieldNode.access)) {
                fieldNode.access |= ACC_SYNTHETIC;
            }
        }

        for (MethodNode methodNode : node.methods) {
            if ((methodNode.visibleAnnotations != null && methodNode.visibleAnnotations.isEmpty()) || (methodNode.invisibleAnnotations != null && methodNode.invisibleAnnotations.isEmpty())) {
                continue;
            }

            if (notSynthetic(methodNode.access)) {
                methodNode.access |= ACC_SYNTHETIC;
            }

            if ((methodNode.access & ACC_BRIDGE) == 0 && !methodNode.name.startsWith("<")) {
                methodNode.access |= ACC_BRIDGE;
            }
        }

    }

    @Override
    public BozarConfig.EnableType getEnableType() {
        return new BozarConfig.EnableType(() -> this.getBozar().getConfig().getOptions().isInvalidSignature(), boolean.class);
    }

    private boolean notSynthetic(int access) {
        return (access & ACC_SYNTHETIC) == 0;
    }

}
