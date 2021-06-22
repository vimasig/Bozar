package io.github.vimasig.bozar.ui;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.utils.BozarUtils;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Controller {

    public final ConfigManager configManager = new ConfigManager(this);

    @FXML private Button browseInput;
    @FXML private Button browseOutput;
    @FXML private ListView<String> console;
    @FXML private Button buttonObf;
    @FXML private Button buttonAddLib;
    @FXML private Button buttonRemoveLib;

    // Configurations
    public TextField input;
    public TextField output;
    public TextArea exclude;
    public ListView<String> libraries;

    // Obfuscation options
    public ComboBox<String> optionLineNumbers;
    public ComboBox<String> optionLocalVariables;
    public ComboBox<String> optionRename;
    public CheckBox optionRemoveSourceFile;
    public CheckBox optionShuffle;
    public CheckBox optionInnerClass;

    public CheckBox optionControlFlowObf;
    public CheckBox optionCrasher;
    public ComboBox<String> optionConstantObf;

    // Watermark options
    public TextField optionWatermarkDummyText;
    public TextField optionWatermarkTextClassText;
    public TextField optionWatermarkLdcPopText;
    public TextArea optionWatermarkZipCommentText;

    private class RedirectedPrintStream extends PrintStream {
        private final String prefix;

        public RedirectedPrintStream(OutputStream out, String prefix) {
            super(out);
            this.prefix = prefix;
        }

        @Override
        public void write(byte[] buf, int off, int len) {
            var s = new AtomicReference<String>();
            s.set(new String(buf, off, len)
                    .replace("\r", "")
                    .replace("\n", ""));
            if (!s.get().isBlank()) {
                if(this.prefix != null)
                    s.set(this.prefix + s.get());
                Platform.runLater(() -> {
                    console.getItems().add(s.get());
                    console.scrollTo(console.getItems().size());
                });
            }
            super.write(buf, off, len);
        }
    }

    @FXML
    public void initialize() {
        // Redirect outputs to ListView
        System.setOut(new RedirectedPrintStream(System.out, null));
        System.setErr(new RedirectedPrintStream(System.err, "ERROR: "));
        log("Initializing controller...");

        // Configure GUI items
        this.mapComboBoxToEnum(this.optionLineNumbers, BozarConfig.BozarOptions.LineNumberOption.class);
        this.mapComboBoxToEnum(this.optionLocalVariables, BozarConfig.BozarOptions.LocalVariableOption.class);
        this.mapComboBoxToEnum(this.optionRename, BozarConfig.BozarOptions.RenameOption.class);
        this.mapComboBoxToEnum(this.optionConstantObf, BozarConfig.BozarOptions.ConstantObfuscationOption.class);

        // Example usage of exclude
        exclude.setPromptText("com.example.myapp.MyClass\r\ncom.example.myapp.MyClass.myField\r\ncom.example.myapp.MyClass.myMethod()\r\ncom.example.mypackage.**");

        var jarFilter = new FileChooser.ExtensionFilter("JAR files (*.jar)", "*.jar");
        browseInput.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(jarFilter);
            File file = fileChooser.showOpenDialog(((Button)actionEvent.getSource()).getScene().getWindow());
            if (file == null || !file.exists() || !file.isFile())
                return;
            input.setText(file.getAbsolutePath());
        });
        browseOutput.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(jarFilter);
            File file = fileChooser.showSaveDialog(((Button)actionEvent.getSource()).getScene().getWindow());
            if (file == null || !file.exists() || !file.isFile())
                return;
            output.setText(file.getAbsolutePath());
        });
        buttonObf.setOnAction(actionEvent -> {
            this.console.getItems().clear();
            log("Generating config...");
            BozarConfig config = this.configManager.generateConfig();
            log("Initializing Bozar...");
            Bozar bozar = new Bozar(config);
            log("Running bozar...");
            bozar.run();
        });
        buttonAddLib.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(jarFilter);
            List<File> files = fileChooser.showOpenMultipleDialog(((Button)actionEvent.getSource()).getScene().getWindow());
            if(files == null) return;
            files.forEach(file -> {
                if (file == null || !file.exists() || !file.isFile())
                    return;
                libraries.getItems().add(file.getAbsolutePath());
            });
        });
        buttonRemoveLib.setOnAction(actionEvent -> {
            int index = libraries.getSelectionModel().getSelectedIndex();
            if(index != -1)
                libraries.getItems().remove(index);
        });

        // Load default config
        try {
            this.configManager.loadDefaultConfig();
        } catch (IOException e) {
            e.printStackTrace();
            this.log("Cannot load default config");
        }

        // Done
        log("Loaded.");
    }

    private void mapComboBoxToEnum(ComboBox<String> comboBox, Class<? extends Enum<?>> enumClass) {
        comboBox.getItems().addAll(Arrays.stream(enumClass.getEnumConstants())
                .map(BozarUtils::getSerializedName)
                .toList());
        comboBox.getSelectionModel().select(0);
    }

    public void log(String s) {
        s = "[BozarGUI] " + s;
        System.out.println(s);
    }
}
