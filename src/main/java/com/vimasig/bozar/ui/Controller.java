package com.vimasig.bozar.ui;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
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
    public CheckBox optionRemoveSourceFile;

    public CheckBox optionConstantObf;

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
        optionLineNumbers.getItems().add("Keep");
        optionLineNumbers.getItems().add("Delete");
        optionLineNumbers.getItems().add("Scramble");
        optionLineNumbers.getSelectionModel().select(0);

        optionLocalVariables.getItems().add("Keep");
        optionLocalVariables.getItems().add("Delete");
        optionLocalVariables.getItems().add("Obfuscate");
        optionLocalVariables.getSelectionModel().select(0);

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
            log("Generating config...");
            BozarConfig config = this.configManager.generateConfig();
            log("Initializing Bozar...");
            Bozar bozar = new Bozar(new File(this.input.getText()), Path.of(this.output.getText()), config);
            log("Running bozar...");
            bozar.run();
        });
        buttonAddLib.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(jarFilter);
            List<File> files = fileChooser.showOpenMultipleDialog(((Button)actionEvent.getSource()).getScene().getWindow());
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

        // TODO: Add exclude feature & remove this
        exclude.setPromptText("Not implemented yet.");
        exclude.setStyle("-fx-prompt-text-fill: #000000");
        exclude.setDisable(true);

        // Done
        log("Loaded.");
    }

    private void log(String s) {
        s = "[BozarGUI] " + s;
        System.out.println(s);
    }
}
