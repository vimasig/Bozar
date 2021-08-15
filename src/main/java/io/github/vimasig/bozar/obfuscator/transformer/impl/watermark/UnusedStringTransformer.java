package io.github.vimasig.bozar.obfuscator.transformer.impl.watermark;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import io.github.vimasig.bozar.obfuscator.utils.ASMUtils;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarCategory;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class UnusedStringTransformer extends ClassTransformer {

    public UnusedStringTransformer(Bozar bozar) {
        super(bozar, "Unused string", BozarCategory.WATERMARK);
    }

    @Override
    public void transformMethod(ClassNode classNode, MethodNode methodNode) {
        if(!ASMUtils.isMethodEligibleToModify(classNode, methodNode)) return;
        methodNode.instructions.insert(new InsnNode(POP));
        methodNode.instructions.insert(new LdcInsnNode(this.getBozar().getConfig().getOptions().getWatermarkOptions().getLdcPopText()));
    }

    @Override
    public BozarConfig.EnableType getEnableType() {
        return new BozarConfig.EnableType(() -> this.getBozar().getConfig().getOptions().getWatermarkOptions().isLdcPop(), "");
    }
}
