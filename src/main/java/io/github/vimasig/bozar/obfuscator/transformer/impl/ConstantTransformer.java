package io.github.vimasig.bozar.obfuscator.transformer.impl;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import io.github.vimasig.bozar.obfuscator.utils.ASMUtils;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarCategory;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class ConstantTransformer extends ClassTransformer {

    public ConstantTransformer(Bozar bozar) {
        super(bozar, "Constant obfuscation", BozarCategory.ADVANCED);
    }

    private void obfuscateNumbers(ClassNode classNode, MethodNode methodNode) {
        Arrays.stream(methodNode.instructions.toArray())
                .filter(insn -> ASMUtils.isPushInt(insn) || ASMUtils.isPushLong(insn))
                .forEach(insn -> {
                    final InsnList insnList = new InsnList();

                    final ValueType valueType = this.getValueType(insn);
                    final long value = switch (valueType) {
                        case INTEGER -> ASMUtils.getPushedInt(insn);
                        case LONG -> ASMUtils.getPushedLong(insn);
                    };

                    // Randomly selected number obfuscation type
                    int type = random.nextInt(2);

                    // Bounds check
                    final byte shift = 2;
                    final boolean canShift = switch (valueType) {
                        case INTEGER -> this.canShiftLeft(shift, value, Integer.MIN_VALUE);
                        case LONG -> this.canShiftLeft(shift, value, Long.MIN_VALUE);
                    };
                    if(!canShift && type == 1)
                        type--;

                    // Number obfuscation types
                    switch (type) {
                        case 0 -> { // XOR
                            int xor1 = random.nextInt(Short.MAX_VALUE);
                            long xor2 = value ^ xor1;
                            switch (valueType) {
                                case INTEGER -> {
                                    insnList.add(ASMUtils.pushInt(xor1));
                                    insnList.add(ASMUtils.pushInt((int) xor2));
                                    insnList.add(new InsnNode(IXOR));
                                }
                                case LONG -> {
                                    insnList.add(ASMUtils.pushLong(xor1));
                                    insnList.add(ASMUtils.pushLong(xor2));
                                    insnList.add(new InsnNode(LXOR));
                                }
                            }
                        }
                        case 1 -> { // Shift
                            switch (valueType) {
                                case INTEGER -> {
                                    insnList.add(ASMUtils.pushInt((int) (value << shift)));
                                    insnList.add(ASMUtils.pushInt(shift));
                                    insnList.add(new InsnNode(IUSHR));
                                }
                                case LONG -> {
                                    insnList.add(ASMUtils.pushLong(value << shift));
                                    insnList.add(ASMUtils.pushInt(shift));
                                    insnList.add(new InsnNode(LUSHR));
                                }
                            }
                        }
                    }

                    if (this.getBozar().getConfig().getOptions().getConstantObfuscation() == BozarConfig.BozarOptions.ConstantObfuscationOption.FLOW) {
                        final InsnList flow = new InsnList(), afterFlow = new InsnList();
                        final LabelNode label0 = new LabelNode(), label1 = new LabelNode(), label2 = new LabelNode(), label3 = new LabelNode();
                        int index = methodNode.maxLocals + 2;
                        long rand0 = random.nextLong(), rand1 = random.nextLong();
                        while (rand0 == rand1)
                            rand1 = random.nextLong();

                        flow.add(ASMUtils.pushLong(rand0));
                        flow.add(ASMUtils.pushLong(rand1));
                        flow.add(new InsnNode(LCMP));
                        flow.add(new VarInsnNode(ISTORE, index));
                        flow.add(new VarInsnNode(ILOAD, index));
                        flow.add(new JumpInsnNode(IFNE, label0));
                        flow.add(label3);
                        flow.add(switch (valueType) {
                            case INTEGER -> ASMUtils.pushInt(random.nextInt());
                            case LONG -> ASMUtils.pushLong(random.nextLong());
                        });
                        flow.add(new JumpInsnNode(GOTO, label1));
                        flow.add(label0);

                        int alwaysNegative = 0;
                        while (alwaysNegative >= 0) alwaysNegative = -random.nextInt(Integer.MAX_VALUE);

                        afterFlow.add(label1);
                        afterFlow.add(new VarInsnNode(ILOAD, index));
                        afterFlow.add(ASMUtils.pushInt(random.nextInt(Integer.MAX_VALUE)));
                        afterFlow.add(new InsnNode(IADD));
                        afterFlow.add(ASMUtils.pushInt(alwaysNegative));
                        afterFlow.add(new JumpInsnNode(IF_ICMPNE, label2));
                        afterFlow.add(switch (valueType) {
                            case INTEGER -> new InsnNode(POP);
                            case LONG -> new InsnNode(POP2);
                        });
                        afterFlow.add(new JumpInsnNode(GOTO, label3));
                        afterFlow.add(label2);

                        methodNode.instructions.insertBefore(insn, flow);
                        methodNode.instructions.insert(insn, afterFlow);
                    }

                    // Replace number instruction with our instructions
                    methodNode.instructions.insert(insn, insnList);
                    methodNode.instructions.remove(insn);
                });

        // Replace numbers between 0 - Byte.MAX_VALUE with
        // "".length()
        Arrays.stream(methodNode.instructions.toArray())
                .filter(ASMUtils::isPushInt)
                .filter(insn -> {
                    int val = ASMUtils.getPushedInt(insn);
                    return val >= 0 && val <= Byte.MAX_VALUE;
                })
                .forEach(insn -> {
                    final InsnList insnList = new InsnList();
                    int value = ASMUtils.getPushedInt(insn);

                    insnList.add(new LdcInsnNode("\0".repeat(value)));
                    insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false));
                    methodNode.instructions.insert(insn, insnList);
                    methodNode.instructions.remove(insn);
                });
    }

    @Override
    public void transformMethod(ClassNode classNode, MethodNode methodNode) {
        // Look for string literals
        Arrays.stream(methodNode.instructions.toArray())
                .filter(insn -> insn instanceof LdcInsnNode && ((LdcInsnNode)insn).cst instanceof String)
                .map(insn -> (LdcInsnNode)insn)
                .forEach(ldc -> {
                    // Replace string literal with our instructions
                    methodNode.instructions.insertBefore(ldc, this.convertString(methodNode, (String) ldc.cst));
                    methodNode.instructions.remove(ldc);
                });

        // Number obfuscation
        this.obfuscateNumbers(classNode, methodNode);
    }

    @Override
    public void transformField(ClassNode classNode, FieldNode fieldNode) {
        // Move field strings to initializer methods so we can obfuscate
        if(fieldNode.value instanceof String)
            if((fieldNode.access & ACC_STATIC) != 0)
                this.addDirectInstructions(classNode, ASMUtils.findOrCreateClinit(classNode), fieldNode);
            else
                this.addDirectInstructions(classNode, ASMUtils.findOrCreateInit(classNode), fieldNode);
    }

    private void addDirectInstructions(ClassNode classNode, MethodNode methodNode, FieldNode fieldNode) {
        final InsnList insnList = new InsnList();
        insnList.add(new LdcInsnNode(fieldNode.value));
        int opcode;
        if((fieldNode.access & ACC_STATIC) != 0)
            opcode = PUTSTATIC;
        else
            opcode = PUTFIELD;
        insnList.add(new FieldInsnNode(opcode, classNode.name, fieldNode.name, fieldNode.desc));
        methodNode.instructions.insert(insnList);

        fieldNode.value = null;
    }

    private InsnList convertString(MethodNode methodNode, String str) {
        final InsnList insnList = new InsnList();
        final int varIndex = methodNode.maxLocals + 1;

        insnList.add(ASMUtils.pushInt(str.length()));
        insnList.add(new IntInsnNode(NEWARRAY, T_BYTE));
        insnList.add(new VarInsnNode(ASTORE, varIndex));

        ArrayList<Integer> indexes = new ArrayList<>();
        for(int i = 0; i < str.length(); i++) indexes.add(i);
        Collections.shuffle(indexes);

        for(int i = 0; i < str.length(); i++) {
            int index = indexes.get(0);
            indexes.remove(0);
            char ch = str.toCharArray()[index];

            if(i == 0) {
                insnList.add(new VarInsnNode(ALOAD, varIndex));
                insnList.add(ASMUtils.pushInt(index));
                insnList.add(ASMUtils.pushInt((byte)random.nextInt(Character.MAX_VALUE)));
                insnList.add(new InsnNode(BASTORE));
            }

            insnList.add(new VarInsnNode(ALOAD, varIndex));
            insnList.add(ASMUtils.pushInt(index));
            insnList.add(ASMUtils.pushInt(ch));
            insnList.add(new InsnNode(BASTORE));
        }

        insnList.add(new TypeInsnNode(NEW, "java/lang/String"));
        insnList.add(new InsnNode(DUP));
        insnList.add(new VarInsnNode(ALOAD, varIndex));
        insnList.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V", false));
        return insnList;
    }

    private boolean canShiftLeft(byte shift, long value, final long minValue) {
        int power = (int) (Math.log(-(minValue >> 1)) / Math.log(2)) + 1;
        return IntStream.range(0, shift).allMatch(i -> (value >> power - i) == 0);
    }

    private enum ValueType {
        INTEGER, LONG
    }

    private ValueType getValueType(AbstractInsnNode insn) {
        if(ASMUtils.isPushInt(insn)) return ValueType.INTEGER;
        else if(ASMUtils.isPushLong(insn)) return ValueType.LONG;
        throw new IllegalArgumentException("Insn is not a push int/long instruction");
    }

    @Override
    public BozarConfig.EnableType getEnableType() {
        return new BozarConfig.EnableType(() -> ((List<?>)this.getEnableType().type()).contains(this.getBozar().getConfig().getOptions().getConstantObfuscation()),
                List.of(BozarConfig.BozarOptions.ConstantObfuscationOption.LIGHT, BozarConfig.BozarOptions.ConstantObfuscationOption.FLOW));
    }
}
