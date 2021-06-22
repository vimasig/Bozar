package io.github.vimasig.bozar.obfuscator.transformer.impl;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import io.github.vimasig.bozar.obfuscator.utils.ASMUtils;
import io.github.vimasig.bozar.obfuscator.utils.InsnBuilder;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControlFlowTransformer extends ClassTransformer {

    public ControlFlowTransformer(Bozar bozar) {
        super(bozar, bozar.getConfig().getOptions().isControlFlowObfuscation());
    }

    private final String FLOW_FIELD_NAME = String.valueOf((char)5097);

    @Override
    public void transformClass(ClassNode classNode) {
        // Skip interfaces because we cannot declare mutable fields in that
        if(!ASMUtils.isClassEligibleToModify(classNode)) return;
        classNode.fields.add(new FieldNode(ACC_PRIVATE | ACC_STATIC, this.FLOW_FIELD_NAME, "J", null, 0L));
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
                .filter(ASMUtils::isIf)
                .map(insn -> (JumpInsnNode)insn)
                .forEach(jump -> {
                    var label0 = new LabelNode();
                    var label1 = new LabelNode();
                    var label2 = new LabelNode();
                    var label3 = new LabelNode();
                    long jVar;

                    final InsnList start = new InsnList();
                    final InsnList before = new InsnList();
                    final InsnList after = new InsnList();
                    final InsnList end = new InsnList();

                    before.add(label0);
                    before.add(new FieldInsnNode(GETSTATIC, classNode.name, this.FLOW_FIELD_NAME, "J"));
                    before.add(ASMUtils.pushLong(jVar = Math.abs((jVar = random.nextLong()) == 0 ? ++jVar : jVar)));
                    before.add(new JumpInsnNode(GOTO, label2));
                    before.add(label1);
                    long v1 = random.nextLong();
                    long v2 = random.nextLong();
                    before.add(ASMUtils.pushLong(v1));
                    before.add(ASMUtils.pushLong(v2));
                    before.add(label2);

                    // Switch
                    record SwitchBlock(LabelNode labelNode, InsnList insnList) {
                        public SwitchBlock() {
                            this(new LabelNode(), new InsnList());
                            this.insnList.add(getRandomLongDiv());
                        }

                        public SwitchBlock(InsnList insnList) {
                            this(new LabelNode(), insnList);
                        }
                    }

                    int switchSize = 5 + random.nextInt(5); // 5 to 9
                    before.add(new InsnNode(DUP2));
                    before.add(new InsnNode(L2I));

                    var switchDefaultLabel = new LabelNode();
                    var switchEndLabel = new LabelNode();
                    var switchBlocks = IntStream.range(0, switchSize).mapToObj(v -> new SwitchBlock()).collect(Collectors.toList());
                    var keyList = this.getUniqueRandomIntArray(switchSize - 1);

                    {
                        long dividedBy = random.nextLong();
                        var correctBlock = new SwitchBlock(InsnBuilder
                                .createEmpty()
                                .insn(ASMUtils.pushLong(dividedBy), new InsnNode(LDIV))
                                .getInsnList()
                        );
                        int i = (int)jVar;
                        keyList.add(i);
                        Collections.sort(keyList);
                        switchBlocks.set(keyList.indexOf(i), correctBlock);

                        jVar /= dividedBy;
                    }

                    var keys = keyList.stream().mapToInt(j -> j).toArray();

                    before.add(new LookupSwitchInsnNode(switchDefaultLabel, keys, switchBlocks.stream().map(switchBlock -> switchBlock.labelNode).toArray(LabelNode[]::new)));
                    switchBlocks.forEach(switchBlock -> {
                        before.add(switchBlock.labelNode);
                        before.add(switchBlock.insnList);
                        before.add(new JumpInsnNode(GOTO, switchEndLabel));
                    });
                    before.add(switchDefaultLabel);
                    before.add(getRandomLongDiv());
                    before.add(new InsnNode(POP2));
                    before.add(new InsnNode(POP2));
                    before.add(new JumpInsnNode(GOTO, label1));
                    before.add(switchEndLabel);

                    // Random operations
                    switch (random.nextInt(3)) {
                        case 0 -> {
                            before.add(new InsnNode(LXOR));
                            before.add(ASMUtils.pushLong(jVar));
                            before.add(new InsnNode(LCMP));
                            before.add(new JumpInsnNode(IFNE, label1));
                            before.add(new VarInsnNode(ALOAD, methodNode.maxLocals + 4));
                            before.add(new JumpInsnNode(IFNULL, label3));
                            before.add(new InsnNode(ACONST_NULL));
                            before.add(new VarInsnNode(ASTORE, methodNode.maxLocals + 4));
                            before.add(new JumpInsnNode(GOTO, label0));
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
                                    case 1 -> this.getRandomJumpOperation1(index, 1, label1);
                                    case -1 -> this.getRandomJumpOperation1(index, -1, label1);
                                    case 0 -> this.getRandomJumpOperation1(index, 0, label1);
                                    default -> throw new IllegalStateException("Unexpected value: " + lcmpResult);
                                });
                        }
                        case 2 -> {
                            before.add(new InsnNode(LAND));
                            before.add(ASMUtils.pushLong(0));
                            before.add(new InsnNode(LCMP));
                            before.add(new JumpInsnNode(IFNE, label1));
                            after.add(new FieldInsnNode(GETSTATIC, classNode.name, this.FLOW_FIELD_NAME, "J"));
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

        try {
            var typeConstructor = Type.class.getDeclaredConstructor(int.class, String.class, int.class, int.class);
            typeConstructor.setAccessible(true);
            methodNode.instructions.insert(new VarInsnNode(ASTORE, methodNode.maxLocals + 4));
            methodNode.instructions.insert(new LdcInsnNode(typeConstructor.newInstance(11, "()Z", 0, 3)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private InsnList getRandomJumpOperation1(int index, int value, LabelNode labelNode) {
        return InsnBuilder.createEmpty().insn(new VarInsnNode(ILOAD, index), ASMUtils.pushInt( value), new JumpInsnNode(IF_ICMPNE, labelNode)).getInsnList();
    }

    private static InsnList getRandomLongDiv() {
        return InsnBuilder.createEmpty().insn(ASMUtils.pushLong(new Random().nextLong()), new InsnNode(LDIV)).getInsnList();
    }

    private void injectInstructions(MethodNode methodNode, AbstractInsnNode insn, InsnList start, InsnList before, InsnList after, InsnList end) {
        methodNode.instructions.insert(start);
        methodNode.instructions.insertBefore(insn, before);
        methodNode.instructions.insert(insn, after);
        methodNode.instructions.add(end);
    }

    private List<Integer> getUniqueRandomIntArray(int size) {
        var baseList = new ArrayList<Integer>();
        for (int i = 0; i < size; i++) {
            int j;
            do {
                j = random.nextInt();
            } while (baseList.contains(j));
            baseList.add(j);
        } return baseList;
    }
}
