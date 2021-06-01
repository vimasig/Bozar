package io.github.vimasig.bozar.obfuscator.utils.model;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.impl.renamer.ClassRenamerTransformer;
import org.objectweb.asm.ClassWriter;

import java.util.Map;

public class CustomClassWriter extends ClassWriter {

    private final Bozar bozar;
    private final ClassLoader classLoader;
    public CustomClassWriter(Bozar bozar, int flags, ClassLoader classLoader) {
        super(flags);
        this.bozar = bozar;
        this.classLoader = classLoader;
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        try {
            return super.getCommonSuperClass(type1, type2);
        } catch (TypeNotPresentException e) {
            return super.getCommonSuperClass(this.findTypeOrDefault(type1), this.findTypeOrDefault(type2));
        }
    }

    private String findTypeOrDefault(String type) {
        var crt = this.bozar.getTransformHandler().getClassTransformer(ClassRenamerTransformer.class);
        return crt.getMap().entrySet().stream()
                .filter(entry -> entry.getValue().equals(type))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(type);
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }
}
