package io.github.vimasig.bozar.obfuscator.utils;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Arrays;

public class InsnBuilder {

    private final InsnList insnList;

    private InsnBuilder() {
        this.insnList = new InsnList();
    }

    private InsnBuilder(InsnList insnList) {
        this.insnList = insnList;
    }

    public InsnBuilder insn(AbstractInsnNode... insnNodes) {
        Arrays.stream(insnNodes).forEach(this.insnList::add);
        return this;
    }

    public InsnBuilder insnList(InsnList... insnLists) {
        Arrays.stream(insnLists).forEach(this.insnList::add);
        return this;
    }

    public InsnList getInsnList() {
        return insnList;
    }

    public static InsnBuilder create(InsnList insnList) {
        return new InsnBuilder(insnList);
    }

    public static InsnBuilder createEmpty() {
        return new InsnBuilder();
    }
}
