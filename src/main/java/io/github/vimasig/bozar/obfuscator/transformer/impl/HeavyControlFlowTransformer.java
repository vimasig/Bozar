package io.github.vimasig.bozar.obfuscator.transformer.impl;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ControlFlowTransformer;
import io.github.vimasig.bozar.obfuscator.utils.ASMUtils;
import io.github.vimasig.bozar.obfuscator.utils.InsnBuilder;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarCategory;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class HeavyControlFlowTransformer extends ControlFlowTransformer {

    public HeavyControlFlowTransformer(Bozar bozar) {
        super(bozar, "Control Flow obfuscation", BozarCategory.ADVANCED);
    }

    private static final String FLOW_FIELD_NAME = String.valueOf((char)5097);
    private static final int[] accessArr = new int[] { 0, ACC_PUBLIC, ACC_PRIVATE, ACC_PROTECTED };

    @Override
    public void transformClass(ClassNode classNode) {
        // Skip interfaces because we cannot declare mutable fields in that
        if(!ASMUtils.isClassEligibleToModify(classNode)) return;
        classNode.fields.add(new FieldNode(accessArr[ThreadLocalRandom.current().nextInt(accessArr.length)] | ACC_STATIC, FLOW_FIELD_NAME, "J", null, 0L));
    }

    @Override
    public void transformMethod(ClassNode classNode, MethodNode methodNode) {
        if(!ASMUtils.isMethodEligibleToModify(classNode, methodNode)) return;
        // Add IF instruction if the method doesn't have any
        if(Arrays.stream(methodNode.instructions.toArray()).noneMatch(ASMUtils::isIf)) {
            final InsnList il = new InsnList();
            final LabelNode label0 = new LabelNode();
            final LabelNode label1 = new LabelNode();
            il.add(new InsnNode(ICONST_1));
            il.add(new JumpInsnNode(GOTO, label1));
            il.add(label0);
            il.add(new InsnNode(ICONST_5));
            il.add(label1);
            il.add(new InsnNode(ICONST_M1));
            il.add(new JumpInsnNode(IF_ICMPLE, label0));
            methodNode.instructions.insert(il);
        }

        // Main obfuscation
        Arrays.stream(methodNode.instructions.toArray())
                .filter(insn -> ASMUtils.isInvokeMethod(insn, true) || insn.getOpcode() == NEW || ASMUtils.isFieldInsn(insn))
                .forEach(insn -> {
                    final InsnList before = new InsnList();
                    final InsnList after = new InsnList();

                    switch (ThreadLocalRandom.current().nextInt(2)) {
                        case 0 -> {
                            final LabelNode label0 = new LabelNode();
                            final LabelNode label1 = new LabelNode();
                            final LabelNode label2 = new LabelNode();
                            final LabelNode label3 = new LabelNode();

                            before.add(new LdcInsnNode(""));
                            before.add(new InsnNode(ICONST_0));
                            before.add(label2);
                            before.add(new InsnNode(POP2));
                            before.add(new FieldInsnNode(GETSTATIC, classNode.name, FLOW_FIELD_NAME, "J"));
                            long l;
                            do {
                                l = ThreadLocalRandom.current().nextLong();
                            } while (l == 0);
                            before.add(ASMUtils.pushLong(l));
                            before.add(new InsnNode(LCMP));
                            before.add(new InsnNode(ICONST_0));
                            before.add(new InsnNode(SWAP));
                            before.add(new InsnNode(DUP));
                            before.add(new JumpInsnNode(IFEQ, label0));
                            before.add(new JumpInsnNode(IFEQ, label3));
                            before.add(new InsnNode(POP));

                            after.add(new JumpInsnNode(GOTO, label1));
                            after.add(label0);
                            after.add(new InsnNode(POP));
                            after.add(label3);
                            after.add(new InsnNode(ICONST_0));
                            after.add(new JumpInsnNode(GOTO, label2));
                            after.add(label1);
                        }
                        case 1 -> {
                            before.add(new FieldInsnNode(GETSTATIC, classNode.name, FLOW_FIELD_NAME, "J"));
                            before.add(new InsnNode(L2I));
                            before.add(getRandomLookupSwitch(2 + ThreadLocalRandom.current().nextInt(3),
                                    0,
                                    new SwitchBlock(InsnBuilder.createEmpty().getInsnList()),
                                    () -> new SwitchBlock(InsnBuilder.createEmpty().getInsnList()),
                                    InsnBuilder.createEmpty().insn(new InsnNode(ACONST_NULL), new InsnNode(ATHROW)).getInsnList()));
                        }
                    }

                    methodNode.instructions.insertBefore(insn, before);
                    methodNode.instructions.insert(insn, after);
                });
        Arrays.stream(methodNode.instructions.toArray())
                .filter(ASMUtils::isIf)
                .map(insn -> (JumpInsnNode)insn)
                .forEach(jump -> {
                    var label0 = new LabelNode();
                    var label1 = new LabelNode();
                    var label2 = new LabelNode();
                    var label3 = new LabelNode();
                    var label4 = new LabelNode();
                    long jVar;

                    final InsnList start = new InsnList();
                    final InsnList before = new InsnList();
                    final InsnList after = new InsnList();
                    final InsnList end = new InsnList();

                    before.add(label0);
                    before.add(new FieldInsnNode(GETSTATIC, classNode.name, FLOW_FIELD_NAME, "J"));
                    before.add(label4);
                    before.add(ASMUtils.pushLong(jVar = Math.abs((jVar = random.nextLong()) == 0 ? ++jVar : jVar)));
                    before.add(new JumpInsnNode(GOTO, label2));
                    before.add(label1);
                    long v1 = random.nextLong();
                    long v2 = random.nextLong();
                    before.add(ASMUtils.pushLong(v1));
                    before.add(ASMUtils.pushLong(v2));
                    before.add(label2);

                    { // Switch
                        long dividedBy = random.nextLong();
                        var targetBlock = new SwitchBlock(InsnBuilder
                                .createEmpty()
                                .insn(ASMUtils.pushLong(dividedBy), new InsnNode(LDIV))
                                .getInsnList()
                        );

                        final InsnList defInstructions = new InsnList();
                        defInstructions.add(getRandomLongDiv());
                        defInstructions.add(new InsnNode(POP2));
                        defInstructions.add(new InsnNode(POP2));
                        defInstructions.add(new JumpInsnNode(GOTO, label1));

                        before.add(new InsnNode(DUP2));
                        before.add(new InsnNode(L2I));
                        before.add(getRandomLookupSwitch(3 + random.nextInt(3), (int)jVar, targetBlock, defInstructions));
                        jVar /= dividedBy;
                    }

                    // Random operations
                    switch (random.nextInt(3)) {
                        case 0 -> {
                            before.add(new InsnNode(LXOR));
                            before.add(ASMUtils.pushLong(jVar));
                            before.add(new InsnNode(LCMP));
                            before.add(new JumpInsnNode(IFNE, label1));
                            before.add(new VarInsnNode(ALOAD, methodNode.maxLocals + 4));
                            before.add(new JumpInsnNode(IFNULL, label3));
                            before.add(getNullLDC());
                            before.add(new VarInsnNode(ASTORE, methodNode.maxLocals + 4));
                            before.add(ASMUtils.pushLong(-5));
                            before.add(new JumpInsnNode(GOTO, label4));
                            before.add(label3);
                        }
                        case 1 -> {
                            int lcmpResult = (jVar == 0) ? 0 : (jVar < 0) ? 1 : -1;
                            before.add(new InsnNode(LCMP));
                            int index = methodNode.maxLocals + 3;
                            before.add(new VarInsnNode(ISTORE, index));
                            before.add(new VarInsnNode(ILOAD, index));
                            before.add(lcmpResult == 0 ? new JumpInsnNode(IFNE, label0) : new JumpInsnNode(IFEQ, label0));
                            if(lcmpResult != 0)
                                before.add(switch (lcmpResult) {
                                    case 1 -> getRandomJumpOperation1(index, 1, label1);
                                    case -1 -> getRandomJumpOperation1(index, -1, label1);
                                    case 0 -> getRandomJumpOperation1(index, 0, label1);
                                    default -> throw new IllegalStateException("Unexpected value: " + lcmpResult);
                                });
                        }
                        case 2 -> {
                            before.add(new InsnNode(LAND));
                            before.add(ASMUtils.pushLong(0));
                            before.add(new InsnNode(LCMP));
                            before.add(new JumpInsnNode(IFNE, label1));
                            after.add(new FieldInsnNode(GETSTATIC, classNode.name, FLOW_FIELD_NAME, "J"));
                            after.add(ASMUtils.pushLong(0));
                            after.add(new InsnNode(LCMP));
                            after.add(ASMUtils.pushInt(-1));
                            after.add(new JumpInsnNode(IF_ICMPNE, label3));
                            after.add(ASMUtils.BuiltInstructions.getThrowNull());
                            after.add(label3);
                        }
                    }

                    this.injectInstructions(methodNode, jump, start, before, after, end);
                });

        methodNode.instructions.insert(new VarInsnNode(ASTORE, methodNode.maxLocals + 4));
        methodNode.instructions.insert(new InsnNode(ACONST_NULL));
    }

    private static AbstractInsnNode getNullLDC() {
        try {
            var typeConstructor = Type.class.getDeclaredConstructor(int.class, String.class, int.class, int.class);
            typeConstructor.setAccessible(true);
            return new LdcInsnNode(typeConstructor.newInstance(11, "()Z", 0, 3));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static InsnList getRandomJumpOperation1(int index, int value, LabelNode labelNode) {
        return InsnBuilder.createEmpty().insn(new VarInsnNode(ILOAD, index), ASMUtils.pushInt( value), new JumpInsnNode(IF_ICMPNE, labelNode)).getInsnList();
    }

    private void injectInstructions(MethodNode methodNode, AbstractInsnNode insn, InsnList start, InsnList before, InsnList after, InsnList end) {
        methodNode.instructions.insert(start);
        methodNode.instructions.insertBefore(insn, before);
        methodNode.instructions.insert(insn, after);
        methodNode.instructions.add(end);
    }

    @Override
    public BozarConfig.EnableType getEnableType() {
        return new BozarConfig.EnableType(() -> this.getBozar().getConfig().getOptions().getControlFlowObfuscation() == this.getEnableType().type(), BozarConfig.BozarOptions.ControlFlowObfuscationOption.HEAVY);
    }
}
