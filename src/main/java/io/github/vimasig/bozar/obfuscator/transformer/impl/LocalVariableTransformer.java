package io.github.vimasig.bozar.obfuscator.transformer.impl;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class LocalVariableTransformer extends ClassTransformer {

    public LocalVariableTransformer(Bozar bozar) {
        super(bozar, bozar.getConfig().getOptions().getLocalVariables() != BozarConfig.BozarOptions.LocalVariableOption.KEEP);
    }

    @Override
    public void transformMethod(ClassNode classNode, MethodNode methodNode) {
        switch (this.getBozar().getConfig().getOptions().getLocalVariables()) {
            case DELETE -> {
                methodNode.localVariables = null;
                methodNode.parameters = null;
            }
            case OBFUSCATE -> {
                if(methodNode.localVariables != null) methodNode.localVariables.forEach(localVar -> localVar.name = "\u2000");
                if(methodNode.parameters != null) methodNode.parameters.forEach(parameterNode -> parameterNode.name = "\u2000");
            }
        }
    }
}
