package com.vimasig.bozar.obfuscator.utils.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BozarConfig {

    private final String exclude;
    private final List<String> libraries;
    private final Options options;

    public BozarConfig(String exclude, List<String> libraries, Options options) {
        this.exclude = exclude;
        this.libraries = libraries;
        this.options = options;
    }

    public String getExclude() {
        return exclude;
    }

    public List<String> getLibraries() {
        return libraries;
    }

    public Options getOptions() {
        return options;
    }

    @Override
    public String toString() {
        return "BozarConfig{" +
                "exclude='" + exclude + '\'' +
                ", libraries=" + libraries +
                ", options=" + options +
                '}';
    }

    public static class Options {
        public enum LineNumberOptions {
            @SerializedName("Keep") KEEP,
            @SerializedName("Delete") DELETE,
            @SerializedName("Scramble") SCRAMBLE;
        }

        public enum LocalVariableOptions {
            @SerializedName("Keep") KEEP,
            @SerializedName("Delete") DELETE,
            @SerializedName("Obfuscate") OBFUSCATE;
        }

        private final LineNumberOptions lineNumbers;
        private final LocalVariableOptions localVariables;
        private final boolean removeSourceFile;
        private final boolean constantObfuscation;

        public Options(LineNumberOptions lineNumbers, LocalVariableOptions localVariables, boolean removeSourceFile, boolean constantObfuscation) {
            this.lineNumbers = lineNumbers;
            this.localVariables = localVariables;
            this.removeSourceFile = removeSourceFile;
            this.constantObfuscation = constantObfuscation;
        }

        public LineNumberOptions getLineNumbers() {
            return lineNumbers;
        }

        public LocalVariableOptions getLocalVariables() {
            return localVariables;
        }

        public boolean isRemoveSourceFile() {
            return removeSourceFile;
        }

        public boolean isConstantObfuscation() {
            return constantObfuscation;
        }

        @Override
        public String toString() {
            return "Options{" +
                    "lineNumbers=" + lineNumbers +
                    ", localVariables=" + localVariables +
                    ", removeSourceFile=" + removeSourceFile +
                    ", constantObfuscation=" + constantObfuscation +
                    '}';
        }
    }
}
