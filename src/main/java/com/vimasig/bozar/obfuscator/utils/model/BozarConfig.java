package com.vimasig.bozar.obfuscator.utils.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BozarConfig {

    private final String exclude;
    private final List<String> libraries;
    private final BozarOptions bozarOptions;

    public BozarConfig(String exclude, List<String> libraries, BozarOptions bozarOptions) {
        this.exclude = exclude;
        this.libraries = libraries;
        this.bozarOptions = bozarOptions;
    }

    public String getExclude() {
        return exclude;
    }

    public List<String> getLibraries() {
        return libraries;
    }

    public BozarOptions getOptions() {
        return bozarOptions;
    }

    public static class BozarOptions {
        public static class WatermarkOptions {
            private final String dummyClassText;
            private final String textInsideClassText;
            private final String zipCommentText;

            public WatermarkOptions(String dummyClassText, String textInsideClassText, String zipCommentText) {
                this.dummyClassText = dummyClassText;
                this.textInsideClassText = textInsideClassText;
                this.zipCommentText = zipCommentText;
            }

            public String getDummyClassText() {
                return dummyClassText;
            }

            public String getTextInsideClassText() {
                return textInsideClassText;
            }

            public String getZipCommentText() {
                return zipCommentText;
            }

            public boolean isDummyClass() {
                return this.dummyClassText != null && !this.dummyClassText.isEmpty();
            }

            public boolean isTextInsideClass() {
                return this.textInsideClassText != null && !this.textInsideClassText.isEmpty();
            }

            public boolean isZipComment() {
                return this.zipCommentText != null && !this.zipCommentText.isEmpty();
            }
        }

        public enum LineNumberOption {
            @SerializedName("Keep") KEEP,
            @SerializedName("Delete") DELETE,
            @SerializedName("Scramble") SCRAMBLE
        }

        public enum LocalVariableOption {
            @SerializedName("Keep") KEEP,
            @SerializedName("Delete") DELETE,
            @SerializedName("Obfuscate") OBFUSCATE
        }

        public enum ConstantObfuscationOption {
            @SerializedName("Off") OFF,
            @SerializedName("Light") LIGHT,
            @SerializedName("Flow") FLOW
        }

        // Obfuscation options
        private final boolean rename;
        private final LineNumberOption lineNumbers;
        private final LocalVariableOption localVariables;
        private final boolean removeSourceFile;
        private final boolean controlFlowObfuscation;
        private final ConstantObfuscationOption constantObfuscation;
        private final WatermarkOptions watermarkOptions;

        public BozarOptions(boolean rename, LineNumberOption lineNumbers, LocalVariableOption localVariables, boolean removeSourceFile, boolean controlFlowObfuscation, ConstantObfuscationOption constantObfuscation, WatermarkOptions watermarkOptions) {
            this.rename = rename;
            this.lineNumbers = lineNumbers;
            this.localVariables = localVariables;
            this.removeSourceFile = removeSourceFile;
            this.controlFlowObfuscation = controlFlowObfuscation;
            this.constantObfuscation = constantObfuscation;
            this.watermarkOptions = watermarkOptions;
        }

        public boolean isRename() {
            return rename;
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

        public ConstantObfuscationOption getConstantObfuscation() {
            return constantObfuscation;
        }

        public boolean isControlFlowObfuscation() {
            return controlFlowObfuscation;
        }

        public WatermarkOptions getWatermarkOptions() {
            return watermarkOptions;
        }
    }
}
