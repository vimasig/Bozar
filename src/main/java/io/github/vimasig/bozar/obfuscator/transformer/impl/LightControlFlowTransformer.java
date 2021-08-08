package io.github.vimasig.bozar.obfuscator.transformer.impl;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ControlFlowTransformer;
import io.github.vimasig.bozar.obfuscator.utils.ASMUtils;
import io.github.vimasig.bozar.obfuscator.utils.InsnBuilder;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class LightControlFlowTransformer extends ControlFlowTransformer {

    public LightControlFlowTransformer(Bozar bozar) {
        super(bozar, bozar.getConfig().getOptions().getControlFlowObfuscation() == BozarConfig.BozarOptions.ControlFlowObfuscationOption.LIGHT);
    }

    private static final String FLOW_FIELD_NAME = String.valueOf((char)5096);
    private static final int[] accessArr = new int[] { 0, ACC_PUBLIC, ACC_PRIVATE, ACC_PROTECTED };

    private long flowFieldValue = 0;
    @Override
    public void transformClass(ClassNode classNode) {
        // Skip interfaces because we cannot declare mutable fields in that
        if(!ASMUtils.isClassEligibleToModify(classNode)) return;

        this.flowFieldValue = ThreadLocalRandom.current().nextLong();
        classNode.fields.add(new FieldNode(accessArr[ThreadLocalRandom.current().nextInt(accessArr.length)] | ACC_STATIC, FLOW_FIELD_NAME, "J", null, this.flowFieldValue));
    }

    @Override
    public void transformMethod(ClassNode classNode, MethodNode methodNode) {
        if(!ASMUtils.isMethodEligibleToModify(classNode, methodNode)) return;

        // Main obfuscation
        Arrays.stream(methodNode.instructions.toArray())
                .filter(insn -> ASMUtils.isInvokeMethod(insn, true) || insn.getOpcode() == NEW || ASMUtils.isFieldInsn(insn))
                .forEach(insn -> {
                    final LabelNode label0 = new LabelNode();
                    final LabelNode label1 = new LabelNode();
                    final LabelNode label2 = new LabelNode();
                    final LabelNode label3 = new LabelNode();
                    final LabelNode label4 = new LabelNode();
                    final LabelNode label5 = new LabelNode();
                    final LabelNode label6 = new LabelNode();

                    final InsnList before = new InsnList();
                    final InsnList after = new InsnList();

                    switch (ThreadLocalRandom.current().nextInt(2)) {
                        case 0 -> {
                            before.add(new JumpInsnNode(GOTO, label3));
                            before.add(label2);
                            before.add(new InsnNode(POP));
                            before.add(label3);
                            before.add(new FieldInsnNode(GETSTATIC, classNode.name, FLOW_FIELD_NAME, "J"));
                            long l;
                            do {
                                l = ThreadLocalRandom.current().nextLong();
                            } while (l == this.flowFieldValue);
                            before.add(ASMUtils.pushLong(l));
                            before.add(new InsnNode(LCMP));
                            before.add(new InsnNode(DUP));
                            before.add(new JumpInsnNode(IFEQ, label2));

                            before.add(ASMUtils.pushInt((this.flowFieldValue > l) ? 1 : -1));
                            before.add(new JumpInsnNode(IF_ICMPNE, label5));

                            after.add(new JumpInsnNode(GOTO, label6));
                            after.add(label5);
                            after.add(ASMUtils.pushInt(ThreadLocalRandom.current().nextInt()));
                            after.add(new JumpInsnNode(GOTO, label2));
                            after.add(label6);
                        }
                        case 1 -> {
                            before.add(new FieldInsnNode(GETSTATIC, classNode.name, FLOW_FIELD_NAME, "J"));
                            before.add(new JumpInsnNode(GOTO, label1));
                            before.add(label0);
                            before.add(ASMUtils.pushLong(ThreadLocalRandom.current().nextLong()));
                            before.add(new InsnNode(LDIV));
                            before.add(label1);
                            before.add(new InsnNode(L2I));
                            before.add(getRandomLookupSwitch(2 + ThreadLocalRandom.current().nextInt(3),
                                    (int)this.flowFieldValue,
                                    new SwitchBlock(InsnBuilder.createEmpty().insn(new JumpInsnNode(GOTO, label4)).getInsnList()),
                                    () -> new SwitchBlock(InsnBuilder.createEmpty().insn(ASMUtils.pushLong(ThreadLocalRandom.current().nextLong()), new JumpInsnNode(GOTO, label0)).getInsnList()),
                                    InsnBuilder.createEmpty().getInsnList()));
                            before.add(label4);
                        }
                    }

                    methodNode.instructions.insertBefore(insn, before);
                    methodNode.instructions.insert(insn, after);
                });
    }
}
