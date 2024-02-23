package io.github.vimasig.bozar.obfuscator.transformer;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.impl.*;
import io.github.vimasig.bozar.obfuscator.transformer.impl.renamer.ClassRenamerTransformer;
import io.github.vimasig.bozar.obfuscator.transformer.impl.renamer.FieldRenamerTransformer;
import io.github.vimasig.bozar.obfuscator.transformer.impl.renamer.MethodRenamerTransformer;
import io.github.vimasig.bozar.obfuscator.transformer.impl.watermark.DummyClassTransformer;
import io.github.vimasig.bozar.obfuscator.transformer.impl.watermark.TextInsideClassTransformer;
import io.github.vimasig.bozar.obfuscator.transformer.impl.watermark.UnusedStringTransformer;
import io.github.vimasig.bozar.obfuscator.transformer.impl.watermark.ZipCommentTransformer;
import io.github.vimasig.bozar.obfuscator.utils.ASMUtils;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TransformManager {

    private final Bozar bozar;
    private final List<ClassTransformer> classTransformers = new ArrayList<>();

    public TransformManager(Bozar bozar) {
        this.bozar = bozar;
        this.classTransformers.addAll(getTransformers().stream()
            .map(clazz -> {
                try {
                    return clazz.getConstructor(Bozar.class).newInstance(this.bozar);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList()));
    }

    public static List<Class<? extends ClassTransformer>> getTransformers() {
        final var transformers = new ArrayList<Class<? extends ClassTransformer>>();

        transformers.add(ClassRenamerTransformer.class);
        transformers.add(FieldRenamerTransformer.class);
        transformers.add(MethodRenamerTransformer.class);

        // TODO: AntiDebugTransformer
        transformers.add(LightControlFlowTransformer.class);
        transformers.add(HeavyControlFlowTransformer.class);
        transformers.add(ConstantTransformer.class);
        transformers.add(LocalVariableTransformer.class);
        transformers.add(LineNumberTransformer.class);
        transformers.add(SourceFileTransformer.class);
        transformers.add(JunkCodeTransformer.class);
        transformers.add(StringEncryptionTransformer.class);
        transformers.add(InvalidSignatureTransformer.class);
        transformers.add(BadAnnotationTransformer.class);
        transformers.add(HideCodeTransformer.class);
        transformers.add(DummyClassTransformer.class);
        transformers.add(TextInsideClassTransformer.class);
        transformers.add(UnusedStringTransformer.class);
        transformers.add(ZipCommentTransformer.class);
        transformers.add(CrasherTransformer.class);
        transformers.add(ShuffleTransformer.class);
        transformers.add(InnerClassTransformer.class);

        return transformers;
    }

    public static ClassTransformer createTransformerInstance(Class<? extends ClassTransformer> transformerClass) {
        try {
            return transformerClass.getConstructor(Bozar.class).newInstance((Object)null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void transformAll() {
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
        if(this.bozar.getConfig().getOptions().getRename() != BozarConfig.BozarOptions.RenameOption.OFF) {
            this.bozar.log("Applying renamer...");
            var reMapper = new SimpleRemapper(map);
            for (int i = 0; i < this.bozar.getClasses().size(); i++) {
                ClassNode classNode = this.bozar.getClasses().get(i);
                ClassNode remappedClassNode = new ClassNode();
                ClassRemapper adapter = new ClassRemapper(remappedClassNode, reMapper);
                classNode.accept(adapter);
                this.bozar.getClasses().set(i, remappedClassNode);
            }
        }

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
