package io.github.vimasig.bozar.obfuscator.transformer.impl;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarCategory;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.List;

public class LineNumberTransformer extends ClassTransformer {

    public LineNumberTransformer(Bozar bozar) {
        super(bozar, "Line numbers", BozarCategory.STABLE);
    }

    @Override
    public void transformMethod(ClassNode classNode, MethodNode methodNode) {
        switch (this.getBozar().getConfig().getOptions().getLineNumbers()) {
            case DELETE -> Arrays.stream(methodNode.instructions.toArray())
                    .filter(insn -> insn instanceof LineNumberNode)
                    .map(insn -> (LineNumberNode)insn)
                    .forEach(lineNumberNode -> methodNode.instructions.remove(lineNumberNode));
            case RANDOMIZE -> Arrays.stream(methodNode.instructions.toArray())
                    .filter(insn -> insn instanceof LineNumberNode)
                    .map(insn -> (LineNumberNode)insn)
                    // Character.MAX_VALUE is not a special requirement
                    .forEach(lineNumberNode -> lineNumberNode.line = this.random.nextInt(Character.MAX_VALUE));
        }
    }

    @Override
    public BozarConfig.EnableType getEnableType() {
        return new BozarConfig.EnableType(() -> ((List<?>)this.getEnableType().type()).contains(this.getBozar().getConfig().getOptions().getLineNumbers()),
                List.of(BozarConfig.BozarOptions.LineNumberOption.DELETE, BozarConfig.BozarOptions.LineNumberOption.RANDOMIZE));
    }
}
