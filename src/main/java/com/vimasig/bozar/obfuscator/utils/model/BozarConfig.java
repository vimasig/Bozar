package com.vimasig.bozar.obfuscator.utils.model;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class BozarConfig {

    private transient final File input;
    private transient final Path output;
    private final String exclude;
    private final List<String> libraries;
    private final BozarOptions bozarOptions;
    private final int version;

    public BozarConfig(String input, String output, String exclude, List<String> libraries, BozarOptions bozarOptions, int version) {
        this.input = new File(input);
        this.output = Path.of(output);
        this.exclude = exclude;
        this.libraries = libraries;
        this.bozarOptions = bozarOptions;
        this.version = version;
    }

    public File getInput() {
        return input;
    }

    public Path getOutput() {
        return output;
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

    public int getVersion() {
        return version;
    }

    public static int getLatestVersion() {
        return 2;
    }

    public static class BozarOptions {
        public static class WatermarkOptions {
            private final String dummyClassText;
            private final String textInsideClassText;
            private final String ldcPopText;
            private final String zipCommentText;

            public WatermarkOptions(String dummyClassText, String textInsideClassText, String ldcPopText, String zipCommentText) {
                this.dummyClassText = dummyClassText;
                this.textInsideClassText = textInsideClassText;
                this.ldcPopText = ldcPopText;
                this.zipCommentText = zipCommentText;
            }

            public String getDummyClassText() {
                return dummyClassText;
            }

            public String getTextInsideClassText() {
                return textInsideClassText;
            }

            public String getLdcPopText() {
                return ldcPopText;
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

            public boolean isLdcPop() {
                return this.ldcPopText != null && !this.ldcPopText.isEmpty();
            }

            public boolean isZipComment() {
                return this.zipCommentText != null && !this.zipCommentText.isEmpty();
            }
        }

        public enum RenameOption {
            @SerializedName("Off") OFF,
            @SerializedName("Alphabet") ALPHABET,
            @SerializedName("Invisible") INVISIBLE
        }

        public enum LineNumberOption {
            @SerializedName("Keep") KEEP,
            @SerializedName("Delete") DELETE,
            @SerializedName("Randomize") RANDOMIZE
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
        private final RenameOption rename;
        private final LineNumberOption lineNumbers;
        private final LocalVariableOption localVariables;
        private final boolean removeSourceFile;
        private final boolean shuffle;
        private final boolean controlFlowObfuscation;
        private final boolean crasher;
        private final ConstantObfuscationOption constantObfuscation;
        private final WatermarkOptions watermarkOptions;

        public BozarOptions(RenameOption rename, LineNumberOption lineNumbers, LocalVariableOption localVariables, boolean removeSourceFile, boolean shuffle, boolean controlFlowObfuscation, boolean crasher, ConstantObfuscationOption constantObfuscation, WatermarkOptions watermarkOptions) {
            this.rename = rename;
            this.lineNumbers = lineNumbers;
            this.localVariables = localVariables;
            this.removeSourceFile = removeSourceFile;
            this.shuffle = shuffle;
            this.crasher = crasher;
            this.controlFlowObfuscation = controlFlowObfuscation;
            this.constantObfuscation = constantObfuscation;
            this.watermarkOptions = watermarkOptions;
        }

        public RenameOption getRename() {
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

        public boolean isShuffle() {
            return shuffle;
        }

        public boolean isCrasher() {
            return crasher;
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
