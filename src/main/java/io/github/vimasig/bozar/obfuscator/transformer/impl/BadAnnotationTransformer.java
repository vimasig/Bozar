package io.github.vimasig.bozar.obfuscator.transformer.impl;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarCategory;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;

public class BadAnnotationTransformer extends ClassTransformer {

    public BadAnnotationTransformer(Bozar bozar) {
        super(bozar, "Bad Annotation", BozarCategory.STABLE);
    }

    @Override
    public void transformClass(ClassNode node) {

        // https://github.com/KgDW/GOTOObfuscator/blob/main/src/main/java/org/gotoobfuscator/transformer/transformers/BadAnnotation.kt

        if (node.visibleAnnotations == null) {
            node.visibleAnnotations = new ArrayList<>();
        }

        node.visibleAnnotations.add(new AnnotationNode(""));

        for (MethodNode method : node.methods) {
            if (method.visibleAnnotations == null) {
                method.visibleAnnotations = new ArrayList<>();
            }
            method.visibleAnnotations.add(new AnnotationNode(""));
        }

        for (FieldNode field : node.fields) {
            if (field.visibleAnnotations == null) {
                field.visibleAnnotations = new ArrayList<>();
            }
            field.visibleAnnotations.add(new AnnotationNode(""));
        }

    }

    @Override
    public BozarConfig.EnableType getEnableType() {
        return new BozarConfig.EnableType(() -> this.getBozar().getConfig().getOptions().isInvalidSignature(), boolean.class);
    }

}
