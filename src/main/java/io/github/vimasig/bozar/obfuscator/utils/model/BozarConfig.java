package io.github.vimasig.bozar.obfuscator.utils.model;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public class BozarConfig {

    private transient final File input;
    private transient final Path output;
    private final String exclude;
    private final List<String> libraries;
    private final BozarOptions bozarOptions;

    public BozarConfig(String input, String output, String exclude, List<String> libraries, BozarOptions bozarOptions) {
        this.input = new File(input);
        this.output = Path.of(output);
        this.exclude = exclude;
        this.libraries = libraries;
        this.bozarOptions = bozarOptions;
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

    public static class BozarOptions {
        public enum RenameOption {
            @SerializedName("Off") OFF,
            @SerializedName("Alphabet") ALPHABET,
            @SerializedName("Invisible") INVISIBLE,
            @SerializedName("IlIlIlIlIl") IlIlIlIlIl,
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

        public enum ControlFlowObfuscationOption {
            @SerializedName("Off") OFF,
            @SerializedName("Light") LIGHT,
            @SerializedName("Heavy") HEAVY
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
        private final boolean removeInnerClasses;
        private final ControlFlowObfuscationOption controlFlowObfuscation;
        private final boolean crasher;
        private final ConstantObfuscationOption constantObfuscation;
        private final WatermarkOptions watermarkOptions;
        private final boolean junkcode;

        public BozarOptions(RenameOption rename, LineNumberOption lineNumbers, LocalVariableOption localVariables, boolean removeSourceFile, boolean shuffle, boolean removeInnerClasses, ControlFlowObfuscationOption controlFlowObfuscation, boolean crasher, ConstantObfuscationOption constantObfuscation, WatermarkOptions watermarkOptions,boolean junkcode) {
            this.rename = rename;
            this.lineNumbers = lineNumbers;
            this.localVariables = localVariables;
            this.removeSourceFile = removeSourceFile;
            this.shuffle = shuffle;
            this.removeInnerClasses = removeInnerClasses;
            this.crasher = crasher;
            this.controlFlowObfuscation = controlFlowObfuscation;
            this.constantObfuscation = constantObfuscation;
            this.watermarkOptions = watermarkOptions;
            this.junkcode = junkcode;
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
        public boolean isJunkCode() {
            return junkcode;
        }

        public boolean isRemoveInnerClasses() {
            return removeInnerClasses;
        }

        public boolean isCrasher() {
            return crasher;
        }

        public ConstantObfuscationOption getConstantObfuscation() {
            return constantObfuscation;
        }

        public ControlFlowObfuscationOption getControlFlowObfuscation() {
            return controlFlowObfuscation;
        }

        public WatermarkOptions getWatermarkOptions() {
            return watermarkOptions;
        }

        public static class WatermarkOptions {
            private final boolean dummyClass, textInsideClass, ldcPop, zipComment;
            private final String dummyClassText, textInsideClassText, ldcPopText, zipCommentText;

            public WatermarkOptions(boolean dummyClass, boolean textInsideClass, boolean ldcPop, boolean zipComment, String dummyClassText, String textInsideClassText, String ldcPopText, String zipCommentText) {
                this.dummyClass = dummyClass;
                this.textInsideClass = textInsideClass;
                this.ldcPop = ldcPop;
                this.zipComment = zipComment;
                this.dummyClassText = dummyClassText;
                this.textInsideClassText = textInsideClassText;
                this.ldcPopText = ldcPopText;
                this.zipCommentText = zipCommentText;
            }

            public boolean isDummyClass() {
                return dummyClass;
            }

            public boolean isTextInsideClass() {
                return textInsideClass;
            }

            public boolean isLdcPop() {
                return ldcPop;
            }

            public boolean isZipComment() {
                return zipComment;
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
        }
    }

    public record EnableType(Supplier<Boolean> isEnabled, Object type) { }
}
