package io.github.vimasig.bozar.obfuscator.transformer;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.utils.model.ResourceWrapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Random;
import java.util.jar.JarOutputStream;

public class ClassTransformer implements Opcodes {

    private final Bozar bozar;
    private final boolean enabled;
    protected final Random random = new Random();

    public ClassTransformer(Bozar bozar, boolean enabled) {
        this.bozar = bozar;
        this.enabled = enabled;
    }

    public void pre() {}
    public void post() {}
    public void transformClass(ClassNode classNode) {}
    public void transformMethod(ClassNode classNode, MethodNode methodNode) {}
    public void transformField(ClassNode classNode, FieldNode fieldNode) {}
    public void transformResource(ResourceWrapper resource) {}
    public boolean transformOutput(ClassNode classNode) { return true; }
    public void transformOutput(JarOutputStream jarOutputStream) {}

    public final Bozar getBozar() {
        return bozar;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public final String getName() {
        return this.getClass().getSimpleName();
    }
}
