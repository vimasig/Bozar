package io.github.vimasig.bozar.obfuscator.transformer.impl;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarCategory;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.concurrent.ThreadLocalRandom;

// By LangYa
public class JunkCodeTransformer extends ClassTransformer {

    public JunkCodeTransformer(Bozar bozar) {
        super(bozar, "Junk Code", BozarCategory.STABLE);
    }

    private final String repeat = getRandomUniqueIl(400).repeat(ThreadLocalRandom.current().nextInt(1000,2000));


    @Override
    public void transformClass(ClassNode classNode) {

        // https://github.com/KgDW/GOTOObfuscator/blob/main/src/main/java/org/gotoobfuscator/transformer/transformers/JunkCode.kt

        for (MethodNode method : classNode.methods) {
            InsnList list = new InsnList();
            LabelNode label = new LabelNode();

            list.add(label);
            list.add(new LdcInsnNode(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE)));
            list.add(new JumpInsnNode(Opcodes.IFGE, label));

            list.add(new InvokeDynamicInsnNode(" ", "()V", new Handle(Opcodes.H_INVOKESTATIC, " ", " ", "(IJIJIJIJIJIJIJIJIJIJIJIJIJ)L;", false)));

            list.add(new InsnNode(Opcodes.ACONST_NULL));
            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, repeat, repeat, "([[[[[[[[[[[[[[[[[[[[[L;)V", false));

            method.instructions.insert(list);
        }

    }


    @Override
    public BozarConfig.EnableType getEnableType() {
        return new BozarConfig.EnableType(() -> this.getBozar().getConfig().getOptions().isJunkCode(), boolean.class);
    }

}
