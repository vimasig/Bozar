package io.github.vimasig.bozar.obfuscator.utils.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

@Getter
public class BozarConfig {

    private transient final File input;
    private transient final Path output;
    private final String exclude;
    private final List<String> libraries;
    private final BozarOptions options;

    public BozarConfig(String input, String output, String exclude, List<String> libraries, BozarOptions bozarOptions) {
        this.input = new File(input);
        this.output = Path.of(output);
        this.exclude = exclude;
        this.libraries = libraries;
        this.options = bozarOptions;
    }

    @Getter
    @RequiredArgsConstructor
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

        @Getter
        @AllArgsConstructor
        public static class WatermarkOptions {
            private final boolean dummyClass;
            private final boolean textInsideClass;
            private final boolean ldcPop;
            private final boolean zipComment;
            private final String dummyClassText;
            private final String textInsideClassText;
            private final String ldcPopText;
            private final String zipCommentText;
        }
    }

    public static record EnableType(Supplier<Boolean> isEnabled, Object type) { }
}
