package io.github.vimasig.bozar.obfuscator.transformer.impl;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collections;
import java.util.List;

public class ShuffleTransformer extends ClassTransformer {

    public ShuffleTransformer(Bozar bozar) {
        super(bozar, bozar.getConfig().getOptions().isShuffle());
    }

    @Override
    public void pre() {
        this.shuffle();
    }

    @Override
    public void post() {
        this.shuffle();
    }

    private void shuffle() {
        var classes = this.getBozar().getClasses();
        Collections.shuffle(classes);
        classes.forEach(ShuffleTransformer::shuffle);
    }

    private static void shuffle(ClassNode classNode) {
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

    private static void shuffleIfNonnull(List<?> list) {
        if(list != null) Collections.shuffle(list);
    }
}
