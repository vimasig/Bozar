package com.vimasig.bozar.obfuscator.transformer;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.transformer.impl.ConstantTransformer;
import com.vimasig.bozar.obfuscator.transformer.impl.LineNumberTransformer;
import com.vimasig.bozar.obfuscator.transformer.impl.LocalVariableTransformer;
import com.vimasig.bozar.obfuscator.transformer.impl.SourceFileTransformer;
import com.vimasig.bozar.obfuscator.utils.ASMUtils;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.ArrayList;
import java.util.List;

public class TransformManager {

    private final Bozar bozar;
    private final List<ClassTransformer> classTransformers = new ArrayList<>();

    public TransformManager(Bozar bozar) {
        this.bozar = bozar;
        this.classTransformers.add(new LocalVariableTransformer(bozar));
        this.classTransformers.add(new LineNumberTransformer(bozar));
        this.classTransformers.add(new SourceFileTransformer(bozar));
        this.classTransformers.add(new ConstantTransformer(bozar));
    }

    public void transform() {
        // TODO: Class version check to skip processing old versions
        this.classTransformers.forEach(classTransformer -> {
            this.bozar.log("Applying %s", classTransformer.getName());
            this.bozar.getClasses().forEach(classNode -> {
                // TODO: Exclude feature
                classTransformer.transformClass(classNode);
                classNode.fields.forEach(fieldNode -> classTransformer.transformField(classNode, fieldNode));
                classNode.methods.forEach(methodNode -> {
                    AbstractInsnNode[] insns = methodNode.instructions.toArray().clone();
                    classTransformer.transformMethod(classNode, methodNode);
                    if (!ASMUtils.isMethodSizeValid(methodNode)) {
                        this.bozar.log("Cannot apply \"%s\" on \"%s\" due to low method capacity", classTransformer.getName(), classNode.name + "." + methodNode.name + methodNode.desc);
                        methodNode.instructions = ASMUtils.arrayToList(insns);
                    }
                });
            });
        });
    }
}
