package io.github.vimasig.bozar.obfuscator.transformer;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.utils.model.ResourceWrapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;

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

    protected boolean isSuperPresent(ClassNode classNode) {
        return classNode.superName != null && !classNode.superName.equals("java/lang/Object");
    }

    protected ClassNode getSuper(ClassNode classNode) {
        return this.findClass(classNode.superName);
    }

    protected ClassNode findClass(String className) {
        return this.getBozar().getClasses().stream().filter(cn -> cn.name.equals(className)).findFirst().orElse(null);
    }

    protected List<ClassNode> findClasses(List<String> classNames) {
        return this.getBozar().getClasses().stream()
                .filter(cn -> classNames.contains(cn.name))
                .collect(Collectors.toList());
    }

    protected List<ClassNode> getSuperHierarchy(ClassNode base) {
        return getSuperHierarchy(base, null);
    }

    protected List<ClassNode> getSuperHierarchy(ClassNode base, ClassNode to) {
        var superList = new ArrayList<ClassNode>();
        while (base != null) {
            superList.add(base);
            if(base.equals(to)) break;
            base = this.getSuper(base);
        } return superList;
    }
}
