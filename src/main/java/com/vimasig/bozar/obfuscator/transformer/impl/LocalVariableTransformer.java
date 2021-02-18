package com.vimasig.bozar.obfuscator.transformer.impl;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import com.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class LocalVariableTransformer extends ClassTransformer {

    public LocalVariableTransformer(Bozar bozar) {
        super(bozar, bozar.getConfig().getOptions().getLocalVariables() != BozarConfig.ObfuscationOptions.LocalVariableOption.KEEP);
    }

    @Override
    public void transformMethod(ClassNode classNode, MethodNode methodNode) {
        switch (this.getBozar().getConfig().getOptions().getLocalVariables()) {
            case DELETE -> {
                methodNode.localVariables = null;
                methodNode.parameters = null;
            }
            case OBFUSCATE -> {
                if(methodNode.localVariables != null) methodNode.localVariables.forEach(localVar -> localVar.name = "\u00A0");
                if(methodNode.parameters != null) methodNode.parameters.forEach(parameterNode -> parameterNode.name = "\u00A0");
            }
        }
    }
}
