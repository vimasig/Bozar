package com.vimasig.bozar.obfuscator.transformer.impl.renamer;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.transformer.RenamerTransformer;
import com.vimasig.bozar.obfuscator.utils.ASMUtils;
import com.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class MethodRenamerTransformer extends RenamerTransformer {

    private final List<String> whitelistedMethods = new ArrayList<>();

    public MethodRenamerTransformer(Bozar bozar) {
        super(bozar, bozar.getConfig().getOptions().getRename() != BozarConfig.BozarOptions.RenameOption.OFF);
        whitelistedMethods.addAll(List.of(
                "main([Ljava/lang/String;)V",
                "premain(Ljava/lang/String;Ljava/lang/instrument/Instrumentation;)V",
                "agentmain(Ljava/lang/String;Ljava/lang/instrument/Instrumentation;)V"
        ));
    }

    @Override
    public void transformClass(ClassNode classNode) {
        this.index = 0;
    }

    @Override
    public void transformMethod(ClassNode classNode, MethodNode methodNode) {
        // TODO: Find a way to detect overridden methods and obfuscate non-static & non-overridden methods too
        if((methodNode.access & ACC_STATIC) == 0) return;
        if(methodNode.name.contains("<")) return;
        if(whitelistedMethods.contains(methodNode.name + methodNode.desc)) return;

        // Register map
        this.registerMap(ASMUtils.getName(classNode, methodNode));

        // Use the same method name if possible
        long methodSigCount = classNode.methods.stream()
                .filter(m -> m.desc.equals(methodNode.desc))
                .count();
        if(methodSigCount == 1) this.index -= 1;
    }
}
