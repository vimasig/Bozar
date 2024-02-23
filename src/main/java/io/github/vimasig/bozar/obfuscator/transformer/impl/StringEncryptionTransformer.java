package io.github.vimasig.bozar.obfuscator.transformer.impl;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarCategory;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

// By LangYa
public class StringEncryptionTransformer extends ClassTransformer {
    public StringEncryptionTransformer(Bozar bozar) {
        super(bozar, "String Encryption", BozarCategory.STABLE);
    }

    @Override
    public void transformClass(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof LdcInsnNode) {
                    Object value = ((LdcInsnNode) instruction).cst;
                    if (value instanceof String string) {
                        ((LdcInsnNode) instruction).cst = stringToUnicode(string);
                    }
                }
            }
        }
    }

    public static String stringToUnicode(String string) {
        StringBuilder unicode = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            unicode.append(String.format("\\u%04x", (int) c));
        }

        return unicode.toString();
    }

    @Override
    public BozarConfig.EnableType getEnableType() {
        return new BozarConfig.EnableType(() -> this.getBozar().getConfig().getOptions().isStringEncryption(), boolean.class);
    }
}
