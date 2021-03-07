package com.vimasig.bozar.obfuscator.transformer.impl;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collections;
import java.util.List;

public class ShuffleTransformer extends ClassTransformer {

    public ShuffleTransformer(Bozar bozar) {
        super(bozar, bozar.getConfig().getOptions().isShuffle());
    }

    @Override
    public void pre() {
        var classes = this.getBozar().getClasses();
        Collections.shuffle(classes);
        classes.forEach(this::shuffle);
    }

    private void shuffle(ClassNode classNode) {
        shuffleIfNonnull(classNode.fields);
        shuffleIfNonnull(classNode.methods);
        shuffleIfNonnull(classNode.innerClasses);
        shuffleIfNonnull(classNode.interfaces);
        shuffleIfNonnull(classNode.attrs);
        shuffleIfNonnull(classNode.invisibleAnnotations);
        shuffleIfNonnull(classNode.visibleAnnotations);
        shuffleIfNonnull(classNode.invisibleTypeAnnotations);
        shuffleIfNonnull(classNode.visibleTypeAnnotations);
        classNode.fields.forEach(f -> {
            shuffleIfNonnull(f.attrs);
            shuffleIfNonnull(f.invisibleAnnotations);
            shuffleIfNonnull(f.visibleAnnotations);
            shuffleIfNonnull(f.invisibleTypeAnnotations);
            shuffleIfNonnull(f.visibleTypeAnnotations);
        });
        classNode.methods.forEach(m -> {
            shuffleIfNonnull(m.attrs);
            shuffleIfNonnull(m.invisibleAnnotations);
            shuffleIfNonnull(m.visibleAnnotations);
            shuffleIfNonnull(m.invisibleTypeAnnotations);
            shuffleIfNonnull(m.visibleTypeAnnotations);
            shuffleIfNonnull(m.exceptions);
            shuffleIfNonnull(m.invisibleLocalVariableAnnotations);
            shuffleIfNonnull(m.visibleLocalVariableAnnotations);
            shuffleIfNonnull(m.localVariables);
            shuffleIfNonnull(m.parameters);
        });
    }

    private void shuffleIfNonnull(List<?> list) {
        if(list != null) Collections.shuffle(list);
    }
}
