package com.vimasig.bozar.ui;

import com.google.gson.*;
import com.vimasig.bozar.obfuscator.utils.BozarUtils;
import com.vimasig.bozar.obfuscator.utils.model.BozarConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class ConfigManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Controller controller;
    public ConfigManager(Controller controller) {
        this.controller = controller;
    }

    public void loadConfig(File file) throws IOException {
        String str = Files.readString(file.toPath());
        try {
            BozarConfig bozarConfig = this.gson.fromJson(str, BozarConfig.class);
            this.loadConfig(bozarConfig);
        } catch (JsonSyntaxException | NullPointerException e) {
            e.printStackTrace();
            this.controller.log("Cannot parse config: " + file.getName());
        }
    }

    public void loadConfig(BozarConfig bozarConfig) {
        var c = this.controller;
        c.exclude.setText(bozarConfig.getExclude());
        c.libraries.getItems().addAll(bozarConfig.getLibraries());

        // Obfuscation options
        c.optionLineNumbers.getSelectionModel().select(BozarUtils.getSerializedName(bozarConfig.getOptions().getLineNumbers()));
        c.optionLocalVariables.getSelectionModel().select(BozarUtils.getSerializedName(bozarConfig.getOptions().getLocalVariables()));
        c.optionRename.getSelectionModel().select(BozarUtils.getSerializedName(bozarConfig.getOptions().getRename()));
        c.optionRemoveSourceFile.setSelected(bozarConfig.getOptions().isRemoveSourceFile());
        c.optionCrasher.setSelected(bozarConfig.getOptions().isCrasher());
        c.optionControlFlowObf.setSelected(bozarConfig.getOptions().isControlFlowObfuscation());
        c.optionConstantObf.getSelectionModel().select(BozarUtils.getSerializedName(bozarConfig.getOptions().getConstantObfuscation()));

        // Watermark options
        c.optionWatermarkDummyText.setText(bozarConfig.getOptions().getWatermarkOptions().getDummyClassText());
        c.optionWatermarkTextClassText.setText(bozarConfig.getOptions().getWatermarkOptions().getTextInsideClassText());
        c.optionWatermarkZipCommentText.setText(bozarConfig.getOptions().getWatermarkOptions().getZipCommentText());
    }

    public void saveConfig(BozarConfig bozarConfig) throws IOException {
        try (FileWriter fw = new FileWriter("bozarConfig.json")) {
            fw.write(this.gson.toJson(bozarConfig));
            fw.flush();
        }
    }

    public BozarConfig generateConfig() {
        var c = this.controller;
        BozarConfig.BozarOptions.WatermarkOptions watermarkOptions = new BozarConfig.BozarOptions.WatermarkOptions(
                c.optionWatermarkDummyText.getText(),
                c.optionWatermarkTextClassText.getText(),
                c.optionWatermarkLdcPopText.getText(),
                c.optionWatermarkZipCommentText.getText()
        );
        BozarConfig.BozarOptions bozarOptions = new BozarConfig.BozarOptions(
                this.gson.fromJson(c.optionRename.getSelectionModel().getSelectedItem(), BozarConfig.BozarOptions.RenameOption.class),
                this.gson.fromJson(c.optionLineNumbers.getSelectionModel().getSelectedItem(), BozarConfig.BozarOptions.LineNumberOption.class),
                this.gson.fromJson(c.optionLocalVariables.getSelectionModel().getSelectedItem(), BozarConfig.BozarOptions.LocalVariableOption.class),
                c.optionRemoveSourceFile.isSelected(),
                c.optionCrasher.isSelected(),
                c.optionControlFlowObf.isSelected(),
                this.gson.fromJson(c.optionConstantObf.getSelectionModel().getSelectedItem(), BozarConfig.BozarOptions.ConstantObfuscationOption.class),
                watermarkOptions
        );
        BozarConfig bozarConfig = new BozarConfig(c.exclude.getText(), this.controller.libraries.getItems(), bozarOptions);
        try {
            this.saveConfig(bozarConfig);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Cannot save config.");
        }
        return bozarConfig;
    }
}
