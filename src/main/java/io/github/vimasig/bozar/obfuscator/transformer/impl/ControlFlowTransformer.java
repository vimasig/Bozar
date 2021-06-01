package io.github.vimasig.bozar.obfuscator.transformer.impl;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import io.github.vimasig.bozar.obfuscator.utils.ASMUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Arrays;

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
                    before.add(ASMUtils.pushLong(Math.abs((jVar = random.nextLong()) == 0 ? ++jVar : jVar)));
                    before.add(new JumpInsnNode(GOTO, label2));
                    before.add(label1);
                    long v1 = random.nextLong();
                    long v2 = random.nextLong();
                    before.add(ASMUtils.pushLong(v1));
                    before.add(ASMUtils.pushLong(v2));
                    before.add(label2);
                    switch (random.nextInt(3)) {
                        case 0 -> {
                            before.add(new InsnNode(LXOR));
                            before.add(ASMUtils.pushLong(v1 ^ v2));
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
                            before.add(new InsnNode(LCMP));
                            int index = methodNode.maxLocals + 3;
                            before.add(new VarInsnNode(ISTORE, index));
                            before.add(new VarInsnNode(ILOAD, index));
                            before.add(new JumpInsnNode(IFEQ, label0));
                            before.add(new VarInsnNode(ILOAD, index));
                            before.add(ASMUtils.pushInt(-1));
                            before.add(new JumpInsnNode(IF_ICMPNE, label1));
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

    private void injectInstructions(MethodNode methodNode, AbstractInsnNode insn, InsnList start, InsnList before, InsnList after, InsnList end) {
        methodNode.instructions.insert(start);
        methodNode.instructions.insertBefore(insn, before);
        methodNode.instructions.insert(insn, after);
        methodNode.instructions.add(end);
    }
}
