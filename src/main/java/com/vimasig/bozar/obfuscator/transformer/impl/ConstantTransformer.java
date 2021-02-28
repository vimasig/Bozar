package com.vimasig.bozar.obfuscator.transformer.impl;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import com.vimasig.bozar.obfuscator.utils.ASMUtils;
import com.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ConstantTransformer extends ClassTransformer {

    public ConstantTransformer(Bozar bozar) {
        super(bozar, bozar.getConfig().getOptions().getConstantObfuscation() != BozarConfig.ObfuscationOptions.ConstantObfuscationOption.OFF);
    }

    private void obfuscateNumbers(ClassNode classNode, MethodNode methodNode) {
        // TODO: Obfuscate longs to enchance ControlFlowTransformer
        Arrays.stream(methodNode.instructions.toArray())
                .filter(ASMUtils::isPushInt)
                .forEach(insn -> {
                    final InsnList insnList = new InsnList();
                    int value = ASMUtils.getPushedInt(insn);

                    int type = random.nextInt(2);

                    // Bounds check for number obfuscation
                    byte shift = 2;
                    if(type == 1) {
                        long l = (long)value << (long)shift;
                        if(l > Integer.MAX_VALUE || l < Integer.MIN_VALUE)
                            type--;
                    }

                    // Number obfuscation types
                    switch (type) {
                        case 0 -> {
                            int xor1 = random.nextInt(Short.MAX_VALUE);
                            int xor2 = value ^ xor1;
                            insnList.add(ASMUtils.pushInt(xor1));
                            insnList.add(ASMUtils.pushInt(xor2));
                            insnList.add(new InsnNode(IXOR));
                        }
                        case 1 -> {
                            insnList.add(ASMUtils.pushInt(value << shift));
                            insnList.add(ASMUtils.pushInt(shift));
                            insnList.add(new InsnNode(ISHR));
                        }
                    }

                    // Combined obfuscation with Control Flow
                    // But it generated +750% file bloat with my test file (no libraries), so I don't recommend it
                    // TODO: Remove this and implement built-in flow obfuscation
                    if (this.getBozar().getConfig().getOptions().getConstantObfuscation() == BozarConfig.ObfuscationOptions.ConstantObfuscationOption.FLOW) {
                        int index = methodNode.maxLocals + 2;
                        insnList.add(new VarInsnNode(ISTORE, index));
                        insnList.add(new VarInsnNode(ILOAD, index));
                        insnList.insert((value == 0) ? new InsnNode(ICONST_1) : new InsnNode(ICONST_0));
                        var label0 = new LabelNode();
                        var label1 = new LabelNode();
                        insnList.add(new JumpInsnNode(GOTO, label1));
                        insnList.add(label0);
                        insnList.add(new IincInsnNode(index, random.nextInt(Integer.MAX_VALUE)));
                        insnList.add(new VarInsnNode(ILOAD, index));
                        insnList.add(ASMUtils.pushInt(random.nextInt()));
                        insnList.add(label1);
                        insnList.add(new JumpInsnNode(IF_ICMPEQ, label0));
                        insnList.add(new VarInsnNode(ILOAD, index));
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
                    return val > 0 && val <= Byte.MAX_VALUE;
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
}
