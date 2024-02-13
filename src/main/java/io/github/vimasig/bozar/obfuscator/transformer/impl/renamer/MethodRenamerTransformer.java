package io.github.vimasig.bozar.obfuscator.transformer.impl.renamer;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.RenamerTransformer;
import io.github.vimasig.bozar.obfuscator.utils.ASMUtils;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarCategory;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.stream.Collectors;

public class MethodRenamerTransformer extends RenamerTransformer {

    private record ClassMethodWrapper(ClassNode classNode, MethodNode methodNode) {
        @Override
        public String toString() {
            return this.classNode.name + "." + this.methodNode.name + methodNode.desc;
        }
    }

    private final List<String> whitelistedMethods = new ArrayList<>();

    public MethodRenamerTransformer(Bozar bozar) {
        super(bozar, "Rename", BozarCategory.STABLE);
        whitelistedMethods.addAll(List.of(
                "main([Ljava/lang/String;)V",
                "premain(Ljava/lang/String;Ljava/lang/instrument/Instrumentation;)V",
                "agentmain(Ljava/lang/String;Ljava/lang/instrument/Instrumentation;)V",

                // java/lang/Object
                "toString()Ljava/lang/String;",
                "clone()Ljava/lang/Object;",
                "equals(Ljava/lang/Object;)Z",
                "hashCode()I"

                // TODO: Hardcode other classes like java/lang/Enum or read all libraries as ClassNode objects
        ));
    }

    @Override
    public void transformMethod(ClassNode classNode, MethodNode methodNode) {
        // Exclusions
        if ((classNode.access & ACC_ANNOTATION) != 0) return;
        if (methodNode.name.contains("<")) return;
        if (whitelistedMethods.contains(methodNode.name + methodNode.desc)) return;

        final String mapName = ASMUtils.getName(classNode, methodNode);

        if ((methodNode.access & ACC_STATIC) != 0 || (methodNode.access & ACC_PRIVATE) != 0) {
            // Directly map private/static methods
            this.registerMap(mapName);
        } else {
            final Set<ClassMethodWrapper> sameMethods = new HashSet<>();
            ClassNode superClass = classNode;

            // Base interface methods
            if (!this.canAccessAllInterfaces(classNode)) return;
            var superInterfaces = new ArrayList<ClassNode>();

            // Loop through super classes
            while (true) {
                // If getSuper() returns null but super is present, mark it as library override and don't rename it
                boolean isSuperPresent = this.isSuperPresent(superClass);

                if ((superClass = this.getSuper(superClass)) == null) {
                    if (isSuperPresent) return;
                    break;
                }

                // Overridden super method
                MethodNode overriddenMethod = findOverriddenMethod(superClass, methodNode);
                if (overriddenMethod != null) {
                    getSuperHierarchy(classNode, superClass).forEach(c -> sameMethods.add(new ClassMethodWrapper(c, overriddenMethod)));
                }

                // Super interfaces
                superInterfaces.addAll(this.getInterfaces(superClass));
                if (!this.canAccessAllInterfaces(superClass)) return;
            }

            // Look for overridden interface methods
            superInterfaces.forEach(cn -> cn.methods.stream()
                    .filter(method -> (methodNode.access & ACC_STATIC) == 0 && (methodNode.access & ACC_PRIVATE) == 0)
                    .filter(method -> method.name.equals(methodNode.name))
                    .filter(method -> method.desc.equals(methodNode.desc))
                    .findFirst()
                    .ifPresentOrElse(method -> this.getInterfaceHierarchyFromSuper(classNode, cn).forEach(c -> sameMethods.add(new ClassMethodWrapper(c, method))), () -> {
                    })
            );

            boolean methodOverrideFound = sameMethods.size() > 0;
            if (methodOverrideFound) {
                // Use the old map if it's already mapped, create a new one if it's not
                final String targetMap = sameMethods.stream()
                        .filter(cmw -> this.isMapRegistered(cmw.toString()))
                        .findFirst()
                        .map(cmw -> this.map.get(cmw.toString()))
                        .orElse(this.registerMap(mapName));

                // Map all same methods
                sameMethods.stream()
                        .map(ClassMethodWrapper::toString)
                        .forEach(s -> this.registerMap(s, targetMap));
            } else {
                var map = this.isMapRegistered(mapName) ? this.map.get(mapName) : this.registerMap(mapName);
                this.getUpperSuperHierarchy(classNode).forEach(cn -> this.registerMap(ASMUtils.getName(cn, methodNode), map));

                // Can't explain this cuz I can't understand either
                this.getUpperInterfaceHierarchy(classNode).forEach(cn -> {
                    this.registerMap(ASMUtils.getName(cn, methodNode), map);
                    this.getUpperSuperHierarchy(cn).forEach(cn2 -> this.registerMap(ASMUtils.getName(cn2, methodNode), map));
                });
            }
        }
    }

    @Override
    public BozarConfig.EnableType getEnableType() {
        return new BozarConfig.EnableType(() -> this.getBozar().getConfig().getOptions().getRename() != this.getEnableType().type(), BozarConfig.BozarOptions.RenameOption.OFF);
    }

    /**
     * @return Looks for non-static & non-private methods in classNode and returns the first method with the same method description and name
     */
    private static MethodNode findOverriddenMethod(ClassNode classNode, MethodNode targetMethod) {
        return classNode.methods.stream()
                .filter(methodNode -> (methodNode.access & ACC_STATIC) == 0 && (methodNode.access & ACC_PRIVATE) == 0)
                .filter(methodNode -> methodNode.name.equals(targetMethod.name))
                .filter(methodNode -> methodNode.desc.equals(targetMethod.desc))
                .findFirst()
                .orElse(null);
    }

    // TODO: Get rid of duplicates when renamer is stable

    /**
     * @return all available interfaces and sub interfaces in the classNode
     */
    private List<ClassNode> getInterfaces(ClassNode classNode) {
        var interfaces = this.findClasses(classNode.interfaces);
        var tmpArr = interfaces.stream()
                .map(this::getInterfaces)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        interfaces.addAll(tmpArr);
        return interfaces;
    }

    /**
     * @return all available class nodes that extends given class node
     */
    private List<ClassNode> getUpperSuperHierarchy(ClassNode classNode) {
        var upperClasses = this.getBozar().getClasses().stream()
                .filter(cn -> cn.superName != null && classNode.name != null && cn.superName.equals(classNode.name))
                .collect(Collectors.toList());
        var tmpArr = upperClasses.stream()
                .map(this::getUpperSuperHierarchy)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        upperClasses.addAll(tmpArr);
        return upperClasses;
    }

    /**
     * @return all available class nodes that implements given class node
     */
    private List<ClassNode> getUpperInterfaceHierarchy(ClassNode classNode) {
        var upperClasses = this.getBozar().getClasses().stream()
                .filter(cn -> cn.interfaces.contains(classNode.name))
                .collect(Collectors.toList());
        var tmpArr = upperClasses.stream()
                .map(this::getUpperInterfaceHierarchy)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        upperClasses.addAll(tmpArr);
        return upperClasses;
    }

    /**
     * Used to check if there is an inaccessible library exists
     *
     * @return the state of whether all classNode interfaces are loaded as ClassNode objects
     */
    private boolean canAccessAllInterfaces(ClassNode classNode) {
        var interfaces = this.findClasses(classNode.interfaces);
        boolean b = interfaces.size() == classNode.interfaces.size();
        if (!b) return false;
        return interfaces.size() == 0 || interfaces.stream().allMatch(this::canAccessAllInterfaces);
    }

    /**
     * @return all available interfaces and sub interfaces in the classNode and its super classes
     */
    private List<ClassNode> getInterfaceHierarchyFromSuper(ClassNode base, ClassNode target) {
        var list = new ArrayList<ClassNode>();
        do {
            var l = this.getInterfaces(base);
            if (!l.isEmpty()) {
                List<ClassNode> tmp = new ArrayList<>();
                for (ClassNode iface : l) {
                    tmp.add(iface);
                    if (iface.equals(target)) {
                        list.addAll(tmp);
                        return list;
                    }
                }
            }

            list.add(base);
            base = this.getSuper(base);
        } while (base != null);
        return new ArrayList<>();
    }
}
