package com.vimasig.bozar.obfuscator;

import com.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import com.vimasig.bozar.obfuscator.transformer.TransformManager;
import com.vimasig.bozar.obfuscator.utils.StreamUtils;
import com.vimasig.bozar.obfuscator.utils.StringUtils;
import com.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import com.vimasig.bozar.obfuscator.utils.model.CustomClassWriter;
import com.vimasig.bozar.obfuscator.utils.model.ResourceWrapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Bozar implements Runnable {

    private final File input;
    private final Path output;
    private final BozarConfig config;

    public Bozar(File input, Path output, BozarConfig config) {
        this.input = input;
        this.output = output;
        this.config = config;
    }

    private final List<ClassNode> classes = new ArrayList<>();
    private final List<ResourceWrapper> resources = new ArrayList<>();

    @Override
    public void run() {
        try {
            // Used to calculate time elapsed
            final long startTime = System.currentTimeMillis();

            // Input file checks
            if(!this.input.exists())
                throw new FileNotFoundException("Cannot find input");
            if(!this.input.isFile())
                throw new IllegalArgumentException("Received input is not a file");
            String inputExtension = this.input.getName().substring(this.input.getName().lastIndexOf(".") + 1).toLowerCase();
            switch (inputExtension) {
                case "jar" -> {
                    // Read JAR input
                    log("Processing JAR input...");
                    try (var jarInputStream = new ZipInputStream(Files.newInputStream(input.toPath()))) {
                        ZipEntry zipEntry;
                        while ((zipEntry = jarInputStream.getNextEntry()) != null) {
                            if (zipEntry.getName().endsWith(".class")) {
                                if(classes.size() == Integer.MAX_VALUE)
                                    throw new IllegalArgumentException("Maximum class count exceeded");
                                ClassReader reader = new ClassReader(jarInputStream);
                                ClassNode classNode = new ClassNode();
                                reader.accept(classNode, 0);
                                classes.add(classNode);
                            } else {
                                if(resources.size() == Integer.MAX_VALUE)
                                    throw new IllegalArgumentException("Maximum resource count exceeded");
                                resources.add(new ResourceWrapper(zipEntry, StreamUtils.readAll(jarInputStream)));
                            }
                        }
                    }
                }
                default -> throw new IllegalArgumentException("Unsupported file extension: " + inputExtension);
            }
            
            // Empty/corrupted file check
            if(classes.size() == 0)
                throw new IllegalArgumentException("Received input does not look like a proper JAR file");

            // Transform
            log("Transforming...");
            final var transformHandler = new TransformManager(this);
            transformHandler.transformAll();

            // Write output
            log("Writing...");
            try (var out = new JarOutputStream(Files.newOutputStream(this.output))) {
                // Write resources
                resources.stream()
                        .filter(resourceWrapper -> !resourceWrapper.getZipEntry().isDirectory())
                        .filter(resourceWrapper -> resourceWrapper.getBytes() != null)
                        .forEach(resourceWrapper -> {
                    try {
                        out.putNextEntry(new JarEntry(resourceWrapper.getZipEntry().getName()));
                        StreamUtils.copy(new ByteArrayInputStream(resourceWrapper.getBytes()), out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                // Convert string library paths to URL array
                final var libs = this.getConfig().getLibraries();
                URL[] urls = new URL[libs.size() + 1];
                urls[libs.size()] = this.input.toURI().toURL();
                for (int i = 0; i < libs.size(); i++)
                    urls[i] = new File(libs.get(i)).toURI().toURL();
                URLClassLoader classLoader = new URLClassLoader(urls);

                // Write classes
                for(ClassNode classNode : this.classes) {
                    var classWriter = new CustomClassWriter(ClassWriter.COMPUTE_FRAMES, classLoader);

                    // Text inside class watermark
                    if(this.getConfig().getOptions().getWatermarkOptions().isTextInsideClass())
                        classWriter.newUTF8(this.getConfig().getOptions().getWatermarkOptions().getTextInsideClassText());

                    classNode.accept(classWriter);
                    byte[] bytes = classWriter.toByteArray();
                    out.putNextEntry(new JarEntry(classNode.name + ".class"));
                    out.write(bytes);
                }

                // Zip comment
                if(this.getConfig().getOptions().getWatermarkOptions().isZipComment())
                    out.setComment(this.getConfig().getOptions().getWatermarkOptions().getZipCommentText());

                // Post transform
                transformHandler.getClassTransformers().stream()
                        .filter(ClassTransformer::isEnabled)
                        .forEach(classTransformer -> classTransformer.transformOutput(out));
            }

            // Elapsed time information
            final String timeElapsed = new DecimalFormat("##.###").format(((double)System.currentTimeMillis() - (double)startTime) / 1000D);
            log("Done. Took %ss", timeElapsed);

            // File size information
            final String oldSize = StringUtils.getConvertedSize(input.length());
            final String newSize = StringUtils.getConvertedSize(output.toFile().length());
            log("File size changed from %s to %s", oldSize, newSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isExcluded(ClassTransformer classTransformer, final String s) {
        return this.getConfig().getExclude().lines().anyMatch(line -> {
            // Detect target transformer
            String targetTransformer = null;
            if(line.contains(":")) {
                targetTransformer = line.split(":")[0];
                line = line.substring((targetTransformer + ":").length());
            }

            if(targetTransformer != null && classTransformer == null) return false;
            if(targetTransformer != null && !classTransformer.getName().equals(targetTransformer)) return false;

            if(line.endsWith("**"))
                return s.startsWith(line.replace("**", ""));
            else if(line.endsWith("*"))
                return s.startsWith(line.replace("**", ""))
                    && s.chars().filter(ch -> ch == '.').count() == line.chars().filter(ch -> ch == '.').count();
            else return line.equals(s);
        });
    }

    public List<ClassNode> getClasses() {
        return classes;
    }

    public List<ResourceWrapper> getResources() {
        return resources;
    }

    public BozarConfig getConfig() {
        return config;
    }

    public void log(String format, Object... args) {
        System.out.println("[Bozar] " + String.format(format, args));
    }

    public void err(String format, Object... args) {
        System.out.println("[Bozar] [ERROR] " + String.format(format, args));
    }
}
