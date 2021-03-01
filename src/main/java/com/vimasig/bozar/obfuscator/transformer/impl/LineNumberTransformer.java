package com.vimasig.bozar.obfuscator.transformer.impl;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import com.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;

public class LineNumberTransformer extends ClassTransformer {

    public LineNumberTransformer(Bozar bozar) {
        super(bozar, bozar.getConfig().getOptions().getLineNumbers() != BozarConfig.BozarOptions.LineNumberOption.KEEP);
    }

    @Override
    public void transformMethod(ClassNode classNode, MethodNode methodNode) {
        switch (this.getBozar().getConfig().getOptions().getLineNumbers()) {
            case DELETE -> Arrays.stream(methodNode.instructions.toArray())
                    .filter(insn -> insn instanceof LineNumberNode)
                    .map(insn -> (LineNumberNode)insn)
                    .forEach(lineNumberNode -> methodNode.instructions.remove(lineNumberNode));
            case SCRAMBLE -> Arrays.stream(methodNode.instructions.toArray())
                    .filter(insn -> insn instanceof LineNumberNode)
                    .map(insn -> (LineNumberNode)insn)
                    // Character.MAX_VALUE is not a special requirement
                    .forEach(lineNumberNode -> lineNumberNode.line = this.random.nextInt(Character.MAX_VALUE));
        }
    }
}
