package com.vimasig.bozar.obfuscator.utils.model;

import org.objectweb.asm.ClassWriter;

public class CustomClassWriter extends ClassWriter {

    private final ClassLoader classLoader;
    public CustomClassWriter(int flags, ClassLoader classLoader) {
        super(flags);
        this.classLoader = classLoader;
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }
}
