package io.github.vimasig.bozar.obfuscator.transformer.impl;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarCategory;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class LocalVariableTransformer extends ClassTransformer {

    public LocalVariableTransformer(Bozar bozar) {
        super(bozar, "Local variables", BozarCategory.STABLE);
    }

    @Override
    public void transformMethod(ClassNode classNode, MethodNode methodNode) {
        switch (this.getBozar().getConfig().getOptions().getLocalVariables()) {
            case DELETE -> {
                methodNode.localVariables = null;
                methodNode.parameters = null;
            }
            case OBFUSCATE -> {
                final String s = "\u6000";
                if(methodNode.localVariables != null) methodNode.localVariables.forEach(localVar -> localVar.name = s);
                if(methodNode.parameters != null) methodNode.parameters.forEach(parameterNode -> parameterNode.name = s);
            }
        }
    }

    @Override
    public BozarConfig.EnableType getEnableType() {
        return new BozarConfig.EnableType(() -> ((List<?>)this.getEnableType().type()).contains(this.getBozar().getConfig().getOptions().getLocalVariables()),
                List.of(BozarConfig.BozarOptions.LocalVariableOption.DELETE, BozarConfig.BozarOptions.LocalVariableOption.OBFUSCATE));
    }
}
