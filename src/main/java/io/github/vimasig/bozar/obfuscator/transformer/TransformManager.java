package io.github.vimasig.bozar.obfuscator.transformer;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.impl.*;
import io.github.vimasig.bozar.obfuscator.transformer.impl.renamer.ClassRenamerTransformer;
import io.github.vimasig.bozar.obfuscator.transformer.impl.renamer.FieldRenamerTransformer;
import io.github.vimasig.bozar.obfuscator.transformer.impl.renamer.MethodRenamerTransformer;
import io.github.vimasig.bozar.obfuscator.utils.ASMUtils;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TransformManager {

    private final Bozar bozar;
    private final List<ClassTransformer> classTransformers = new ArrayList<>();

    public TransformManager(Bozar bozar) {
        this.bozar = bozar;
        this.classTransformers.add(new ClassRenamerTransformer(bozar));
        this.classTransformers.add(new FieldRenamerTransformer(bozar));
        this.classTransformers.add(new MethodRenamerTransformer(bozar));

        // TODO: AntiDebugTransformer
        this.classTransformers.add(new ControlFlowTransformer(bozar));
        this.classTransformers.add(new ConstantTransformer(bozar));
        this.classTransformers.add(new LocalVariableTransformer(bozar));
        this.classTransformers.add(new LineNumberTransformer(bozar));
        this.classTransformers.add(new SourceFileTransformer(bozar));
        this.classTransformers.add(new WatermarkTransformer(bozar));
        this.classTransformers.add(new CrasherTransformer(bozar));
        this.classTransformers.add(new ShuffleTransformer(bozar));
        this.classTransformers.add(new InnerClassTransformer(bozar));
    }

    public void transformAll() {
        // Pre
        this.classTransformers.stream()
                .filter(ClassTransformer::isEnabled)
                .forEach(ClassTransformer::pre);

        // Transform all classes
        this.classTransformers.stream()
            .filter(ClassTransformer::isEnabled)
            .filter(ct -> !(ct instanceof RenamerTransformer))
            .forEach(ct -> {
                this.bozar.log("Applying %s", ct.getName());
                this.bozar.getClasses().forEach(classNode -> this.transform(classNode, ct.getClass()));
                this.bozar.getResources().forEach(ct::transformResource);
        });

        // Post
        this.classTransformers.stream()
                .filter(ClassTransformer::isEnabled)
                .forEach(ClassTransformer::post);

        // Apply renamer transformers
        var map = new HashMap<String, String>();
        this.classTransformers.stream()
            .filter(ClassTransformer::isEnabled)
            .filter(ct -> ct instanceof RenamerTransformer)
            .map(ct -> (RenamerTransformer)ct)
            .forEach(crt -> {
                this.bozar.log("Applying renamer %s", crt.getName());
                this.bozar.getClasses().forEach(classNode -> this.transform(classNode, crt.getClass()));
                this.bozar.getResources().forEach(crt::transformResource);
                map.putAll(crt.map);
            });

        // Remap classes
        var reMapper = new SimpleRemapper(map);
        for (int i = 0; i < this.bozar.getClasses().size(); i++) {
            ClassNode classNode = this.bozar.getClasses().get(i);
            ClassNode remappedClassNode = new ClassNode();
            ClassRemapper adapter = new ClassRemapper(remappedClassNode, reMapper);
            classNode.accept(adapter);
            this.bozar.getClasses().set(i, remappedClassNode);
        }
    }

    public void transform(ClassNode classNode, Class<? extends ClassTransformer> transformerClass) {
        ClassTransformer classTransformer = this.getClassTransformer(transformerClass);
        if(this.bozar.isExcluded(classTransformer, ASMUtils.getName(classNode))) return;

        classTransformer.transformClass(classNode);
        classNode.fields.stream()
                .filter(fieldNode -> !this.bozar.isExcluded(classTransformer, ASMUtils.getName(classNode, fieldNode)))
                .forEach(fieldNode -> classTransformer.transformField(classNode, fieldNode));
        classNode.methods.stream()
                .filter(methodNode -> !this.bozar.isExcluded(classTransformer, ASMUtils.getName(classNode) + "." + methodNode.name + "()"))
                .forEach(methodNode -> {
            AbstractInsnNode[] insns = methodNode.instructions.toArray().clone();
            classTransformer.transformMethod(classNode, methodNode);

            // Revert changes if method size is invalid
            if (!ASMUtils.isMethodSizeValid(methodNode)) {
                this.bozar.log("Cannot apply \"%s\" on \"%s\" due to low method capacity", classTransformer.getName(), classNode.name + "." + methodNode.name + methodNode.desc);
                methodNode.instructions = ASMUtils.arrayToList(insns);
            }
        });
    }

    @SuppressWarnings("unchecked") // Checked using stream
    public <T extends ClassTransformer> T getClassTransformer(Class<T> transformerClass) {
        if(transformerClass == null)
            throw new NullPointerException("transformerClass cannot be null");
        return (T) this.classTransformers.stream()
                .filter(ct -> ct.getClass().equals(transformerClass))
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Cannot find transformerClass: " + transformerClass.getName()));
    }

    public List<ClassTransformer> getClassTransformers() {
        return classTransformers;
    }
}
