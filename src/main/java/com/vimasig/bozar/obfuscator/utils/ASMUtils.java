package com.vimasig.bozar.obfuscator.utils;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.CodeSizeEvaluator;
import org.objectweb.asm.tree.*;

import java.util.Arrays;

public class ASMUtils implements Opcodes {

    public static byte[] toByteArrayDefault(ClassNode classNode) {
        var classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    public static String getName(ClassNode classNode) {
        return classNode.name.replace("/", ".");
    }

    public static String getName(ClassNode classNode, FieldNode fieldNode) {
        return getName(classNode) + "." + fieldNode.name;
    }

    public static String getName(ClassNode classNode, MethodNode methodNode) {
        return getName(classNode) + "." + methodNode.name + methodNode.desc;
    }

    public static InsnList getThrowNull() {
        final InsnList insnList = new InsnList();
        insnList.add(new InsnNode(ACONST_NULL));
        insnList.add(new InsnNode(ATHROW));
        return insnList;
    }

    public static InsnList arrayToList(AbstractInsnNode[] insns) {
        final InsnList insnList = new InsnList();
        Arrays.stream(insns).forEach(insnList::add);
        return insnList;
    }

    public static boolean isMethodSizeValid(MethodNode methodNode) {
        return getCodeSize(methodNode) <= 65536;
    }

    public static int getCodeSize(MethodNode methodNode) {
        CodeSizeEvaluator cse = new CodeSizeEvaluator(null);
        methodNode.accept(cse);
        return cse.getMaxSize();
    }

    public static MethodNode findOrCreateInit(ClassNode classNode) {
        MethodNode clinit = findMethod(classNode, "<init>", "()V");
        if (clinit == null) {
            clinit = new MethodNode(ACC_PUBLIC, "<init>", "()V", null, null);
            clinit.instructions.add(new InsnNode(RETURN));
            classNode.methods.add(clinit);
        } return clinit;
    }

    public static MethodNode findOrCreateClinit(ClassNode classNode) {
        MethodNode clinit = findMethod(classNode, "<clinit>", "()V");
        if (clinit == null) {
            clinit = new MethodNode(ACC_STATIC, "<clinit>", "()V", null, null);
            clinit.instructions.add(new InsnNode(RETURN));
            classNode.methods.add(clinit);
        } return clinit;
    }

    public static MethodNode findMethod(ClassNode classNode, String name, String desc) {
        return classNode.methods
                .stream()
                .filter(methodNode -> name.equals(methodNode.name) && desc.equals(methodNode.desc))
                .findAny()
                .orElse(null);
    }

    public static boolean isInvokeMethod(AbstractInsnNode insn) {
        return insn.getOpcode() >= INVOKEVIRTUAL && insn.getOpcode() <= INVOKEDYNAMIC;
    }

    public static boolean isIf(AbstractInsnNode insn) {
        int op = insn.getOpcode();
        return (op >= IFEQ && op <= IF_ACMPNE) || op == IFNULL || op == IFNONNULL;
    }

    public static AbstractInsnNode pushLong(long value) {
        if (value == 0) return new InsnNode(LCONST_0);
        else if (value == 1) return new InsnNode(LCONST_1);
        else return new LdcInsnNode(value);
    }

    public static AbstractInsnNode pushInt(int value) {
        if (value >= -1 && value <= 5) {
            return new InsnNode(ICONST_0 + value);
        }
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            return new IntInsnNode(BIPUSH, value);
        }
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            return new IntInsnNode(SIPUSH, value);
        }
        return new LdcInsnNode(value);
    }

    public static boolean isPushInt(AbstractInsnNode insn) {
        int op = insn.getOpcode();
        return (op >= ICONST_M1 && op <= ICONST_5)
                || op == BIPUSH
                || op == SIPUSH
                || (op == LDC && ((LdcInsnNode) insn).cst instanceof Integer);
    }

    public static int getPushedInt(AbstractInsnNode insn) {
        int op = insn.getOpcode();
        if (op >= ICONST_M1 && op <= ICONST_5) {
            return op - ICONST_0;
        }
        if (op == BIPUSH || op == SIPUSH) {
            return ((IntInsnNode)insn).operand;
        }
        if (op == LDC) {
            Object cst = ((LdcInsnNode)insn).cst;
            if (cst instanceof Integer) {
                return (int) cst;
            }
        } throw new IllegalArgumentException("Insn is not a push int instruction");
    }
}
