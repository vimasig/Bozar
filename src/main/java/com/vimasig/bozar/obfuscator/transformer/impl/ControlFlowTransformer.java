package com.vimasig.bozar.obfuscator.transformer.impl;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import com.vimasig.bozar.obfuscator.utils.ASMUtils;
import org.objectweb.asm.tree.*;

import java.util.Arrays;

public class ControlFlowTransformer extends ClassTransformer {

    public ControlFlowTransformer(Bozar bozar) {
        super(bozar);
    }

    private final String FLOW_FIELD_NAME = String.valueOf((char)5097);

    @Override
    public void transformClass(ClassNode classNode) {
        if(!this.getBozar().getConfig().getOptions().isControlFlowObfuscation()) return;
        // Skip interfaces because we cannot declare mutable fields in that
        if((classNode.access & ACC_INTERFACE) != 0) return;
        classNode.fields.add(new FieldNode(ACC_PRIVATE | ACC_STATIC, this.FLOW_FIELD_NAME, "J", null, 0L));
    }

    @Override
    public void transformMethod(ClassNode classNode, MethodNode methodNode) {
        if(!this.getBozar().getConfig().getOptions().isControlFlowObfuscation()) return;
        if((classNode.access & ACC_INTERFACE) != 0) return;

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
                    for(int i = 0; i < 2; i++)
                        before.add(ASMUtils.pushLong(random.nextLong()));
                    before.add(label2);
                    switch (random.nextInt(2)) {
                        case 0 -> {
                            before.add(new InsnNode(LCMP));
                            int index = methodNode.maxLocals + 3;
                            before.add(new VarInsnNode(ISTORE, index));
                            before.add(new VarInsnNode(ILOAD, index));
                            before.add(new JumpInsnNode(IFEQ, label0));
                            before.add(new VarInsnNode(ILOAD, index));
                            before.add(ASMUtils.pushInt(-1));
                            before.add(new JumpInsnNode(IF_ICMPNE, label1));
                        }
                        case 1 -> {
                            before.add(new InsnNode(LAND));
                            before.add(ASMUtils.pushLong(0));
                            before.add(new InsnNode(LCMP));
                            before.add(new JumpInsnNode(IFNE, label1));

                            after.add(new FieldInsnNode(GETSTATIC, classNode.name, this.FLOW_FIELD_NAME, "J"));
                            after.add(ASMUtils.pushLong(0));
                            after.add(new InsnNode(LCMP));
                            after.add(ASMUtils.pushInt(-1));
                            after.add(new JumpInsnNode(IF_ICMPNE, label3));
                            after.add(ASMUtils.getThrowNull());
                            after.add(label3);
                        }
                    }

                    this.injectInstructions(methodNode, jump, start, before, after, end);
                });
    }

    private void injectInstructions(MethodNode methodNode, AbstractInsnNode insn, InsnList start, InsnList before, InsnList after, InsnList end) {
        methodNode.instructions.insert(start);
        methodNode.instructions.insertBefore(insn, before);
        methodNode.instructions.insert(insn, after);
        methodNode.instructions.add(end);
    }
}
