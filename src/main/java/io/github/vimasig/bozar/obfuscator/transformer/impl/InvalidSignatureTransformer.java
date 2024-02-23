package io.github.vimasig.bozar.obfuscator.transformer.impl;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarCategory;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Random;

// By LangYa
public class InvalidSignatureTransformer extends ClassTransformer {
    public InvalidSignatureTransformer(Bozar bozar) {
        super(bozar, "Invalid Signature", BozarCategory.STABLE);
    }

    @Override
    public void transformClass(ClassNode node) {
        // https://github.com/KgDW/GOTOObfuscator/blob/main/src/main/java/org/gotoobfuscator/transformer/transformers/InvalidSignature.kt

        if (node.signature == null) {
            node.signature = randomSignature();
        }

        for (MethodNode methodNode : node.methods) {
            if (methodNode.signature == null) {
                methodNode.signature = randomSignature();
            }
        }

        for (FieldNode fieldNode : node.fields) {
            if (fieldNode.signature == null) {
                fieldNode.signature = randomSignature();
            }
        }

    }

    private String randomSignature() {
        Random random = new Random();
        int rand = random.nextInt(4);
        return switch (rand) {
            case 1 -> "[I";
            case 2 -> "[Z";
            case 3 -> "[J";
            default -> "[B";
        };
    }

    @Override
    public BozarConfig.EnableType getEnableType() {
        return new BozarConfig.EnableType(() -> this.getBozar().getConfig().getOptions().isInvalidSignature(), boolean.class);
    }

}
