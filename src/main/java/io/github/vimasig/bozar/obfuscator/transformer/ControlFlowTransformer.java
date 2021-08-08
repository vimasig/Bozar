package io.github.vimasig.bozar.obfuscator.transformer;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.utils.ASMUtils;
import io.github.vimasig.bozar.obfuscator.utils.InsnBuilder;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControlFlowTransformer extends ClassTransformer {

    public ControlFlowTransformer(Bozar bozar, boolean enabled) {
        super(bozar, enabled);
    }

    protected static final record SwitchBlock(LabelNode labelNode, InsnList insnList) {
        public SwitchBlock() {
            this(new LabelNode(), new InsnList());
            this.insnList.add(getRandomLongDiv());
        }

        public SwitchBlock(InsnList insnList) {
            this(new LabelNode(), insnList);
        }
    }

    protected static InsnList getRandomLookupSwitch(final int switchSize, final int targetKey, final SwitchBlock targetBlock, final InsnList defInstructions) {
        return getRandomLookupSwitch(switchSize, targetKey, targetBlock, SwitchBlock::new, defInstructions);
    }

    protected static InsnList getRandomLookupSwitch(final int switchSize, final int targetKey, final SwitchBlock targetBlock, final Supplier<SwitchBlock> dummyBlock, final InsnList defInstructions) {
        final InsnList il = new InsnList();
        var switchDefaultLabel = new LabelNode();
        var switchEndLabel = new LabelNode();
        var switchBlocks = IntStream.range(0, switchSize).mapToObj(v -> dummyBlock.get()).collect(Collectors.toList());
        var keyList = getUniqueRandomIntArray(switchSize - 1);

        {
            keyList.add(targetKey);
            Collections.sort(keyList);
            switchBlocks.set(keyList.indexOf(targetKey), targetBlock);
        }

        il.add(new LookupSwitchInsnNode(switchDefaultLabel, keyList.stream().mapToInt(j -> j).toArray(), switchBlocks.stream().map(SwitchBlock::labelNode).toArray(LabelNode[]::new)));
        switchBlocks.forEach(switchBlock -> {
            il.add(switchBlock.labelNode());
            il.add(switchBlock.insnList());
            il.add(new JumpInsnNode(GOTO, switchEndLabel));
        });
        il.add(switchDefaultLabel);
        il.add(defInstructions);
        il.add(switchEndLabel);
        return il;
    }

    protected static List<Integer> getUniqueRandomIntArray(int size) {
        var baseList = new ArrayList<Integer>();
        for (int i = 0; i < size; i++) {
            int j;
            do {
                j = ThreadLocalRandom.current().nextInt();
            } while (baseList.contains(j));
            baseList.add(j);
        } return baseList;
    }

    protected static InsnList getRandomLongDiv() {
        return InsnBuilder.createEmpty().insn(ASMUtils.pushLong(new Random().nextLong()), new InsnNode(LDIV)).getInsnList();
    }
}
