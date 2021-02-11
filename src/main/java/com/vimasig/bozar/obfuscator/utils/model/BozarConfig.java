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
        public enum LineNumberOption {
            @SerializedName("Keep") KEEP,
            @SerializedName("Delete") DELETE,
            @SerializedName("Scramble") SCRAMBLE;
        }

        public enum LocalVariableOption {
            @SerializedName("Keep") KEEP,
            @SerializedName("Delete") DELETE,
            @SerializedName("Obfuscate") OBFUSCATE;
        }

        private final LineNumberOption lineNumbers;
        private final LocalVariableOption localVariables;
        private final boolean removeSourceFile;
        private final boolean constantObfuscation;

        public Options(LineNumberOption lineNumbers, LocalVariableOption localVariables, boolean removeSourceFile, boolean constantObfuscation) {
            this.lineNumbers = lineNumbers;
            this.localVariables = localVariables;
            this.removeSourceFile = removeSourceFile;
            this.constantObfuscation = constantObfuscation;
        }

        public LineNumberOption getLineNumbers() {
            return lineNumbers;
        }

        public LocalVariableOption getLocalVariables() {
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
