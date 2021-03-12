package com.vimasig.bozar.ui;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.vimasig.bozar.obfuscator.utils.BozarUtils;
import com.vimasig.bozar.obfuscator.utils.Reflection;
import com.vimasig.bozar.obfuscator.utils.model.BozarConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    private final Controller controller;
    public ConfigManager(Controller controller) {
        this.controller = controller;
    }

    public void loadConfig(File file) throws IOException {
        String str = Files.readString(file.toPath());
        try {
            // Deserializer for input/output file
            JsonDeserializer<BozarConfig> deserializer = (jsonElement, type, jsonDeserializationContext) -> {
                BozarConfig bozarConfig = new Gson().fromJson(jsonElement, BozarConfig.class);
                var reflect = new Reflection<>(bozarConfig);
                reflect.setDeclaredField("input", new File(((JsonObject)jsonElement).get("input").getAsString()));
                reflect.setDeclaredField("output", Path.of(((JsonObject)jsonElement).get("output").getAsString()));
                return bozarConfig;
            };

            // Load config
            BozarConfig bozarConfig = new GsonBuilder()
                    .registerTypeAdapter(BozarConfig.class, deserializer)
                    .setPrettyPrinting()
                    .create()
                    .fromJson(str, BozarConfig.class);
            if(bozarConfig != null)
                if(bozarConfig.getVersion() != BozarConfig.getLatestVersion())
                    this.controller.log("Skipping loading unsupported config version: " + bozarConfig.getVersion());
                else this.loadConfig(bozarConfig);
        } catch (JsonSyntaxException | NullPointerException e) {
            e.printStackTrace();
            this.controller.log("Cannot parse config: " + file.getName());
        }
    }

    public void loadConfig(BozarConfig bozarConfig) {
        var c = this.controller;
        c.input.setText(bozarConfig.getInput().getAbsolutePath());
        c.output.setText(bozarConfig.getOutput().toFile().getAbsolutePath());
        c.exclude.setText(bozarConfig.getExclude());
        c.libraries.getItems().addAll(bozarConfig.getLibraries());

        // Obfuscation options
        c.optionLineNumbers.getSelectionModel().select(BozarUtils.getSerializedName(bozarConfig.getOptions().getLineNumbers()));
        c.optionLocalVariables.getSelectionModel().select(BozarUtils.getSerializedName(bozarConfig.getOptions().getLocalVariables()));
        c.optionRename.getSelectionModel().select(BozarUtils.getSerializedName(bozarConfig.getOptions().getRename()));
        c.optionRemoveSourceFile.setSelected(bozarConfig.getOptions().isRemoveSourceFile());
        c.optionShuffle.setSelected(bozarConfig.getOptions().isShuffle());
        c.optionCrasher.setSelected(bozarConfig.getOptions().isCrasher());
        c.optionControlFlowObf.setSelected(bozarConfig.getOptions().isControlFlowObfuscation());
        c.optionConstantObf.getSelectionModel().select(BozarUtils.getSerializedName(bozarConfig.getOptions().getConstantObfuscation()));

        // Watermark options
        c.optionWatermarkDummyText.setText(bozarConfig.getOptions().getWatermarkOptions().getDummyClassText());
        c.optionWatermarkTextClassText.setText(bozarConfig.getOptions().getWatermarkOptions().getTextInsideClassText());
        c.optionWatermarkLdcPopText.setText(bozarConfig.getOptions().getWatermarkOptions().getLdcPopText());
        c.optionWatermarkZipCommentText.setText(bozarConfig.getOptions().getWatermarkOptions().getZipCommentText());
    }

    public void saveConfig(BozarConfig bozarConfig) throws IOException {
        try (FileWriter fw = new FileWriter("bozarConfig.json")) {
            // Serializer for input/output file
            JsonSerializer<BozarConfig> serializer = (cfg, type, jsonSerializationContext) -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("input", new JsonPrimitive(cfg.getInput().getAbsolutePath()));
                jsonObject.add("output", new JsonPrimitive(cfg.getOutput().toFile().getAbsolutePath()));
                ((JsonObject) new Gson().toJsonTree(cfg))
                        .entrySet().forEach(stringJsonElementEntry -> jsonObject.add(stringJsonElementEntry.getKey(), stringJsonElementEntry.getValue()));
                return jsonObject;
            };

            // Write config
            fw.write(new GsonBuilder()
                    .registerTypeAdapter(BozarConfig.class, serializer)
                    .create()
                    .toJson(bozarConfig)
            );
            fw.flush();
        }
    }

    public BozarConfig generateConfig() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        var c = this.controller;

        BozarConfig.BozarOptions.WatermarkOptions watermarkOptions = new BozarConfig.BozarOptions.WatermarkOptions(
                c.optionWatermarkDummyText.getText(),
                c.optionWatermarkTextClassText.getText(),
                c.optionWatermarkLdcPopText.getText(),
                c.optionWatermarkZipCommentText.getText()
        );
        BozarConfig.BozarOptions bozarOptions = new BozarConfig.BozarOptions(
                gson.fromJson(c.optionRename.getSelectionModel().getSelectedItem(), BozarConfig.BozarOptions.RenameOption.class),
                gson.fromJson(c.optionLineNumbers.getSelectionModel().getSelectedItem(), BozarConfig.BozarOptions.LineNumberOption.class),
                gson.fromJson(c.optionLocalVariables.getSelectionModel().getSelectedItem(), BozarConfig.BozarOptions.LocalVariableOption.class),
                c.optionRemoveSourceFile.isSelected(),
                c.optionShuffle.isSelected(),
                c.optionCrasher.isSelected(),
                c.optionControlFlowObf.isSelected(),
                gson.fromJson(c.optionConstantObf.getSelectionModel().getSelectedItem(), BozarConfig.BozarOptions.ConstantObfuscationOption.class),
                watermarkOptions
        );
        BozarConfig bozarConfig = new BozarConfig(c.input.getText(), c.output.getText(), c.exclude.getText(), this.controller.libraries.getItems(), bozarOptions, BozarConfig.getLatestVersion());
        try {
            this.saveConfig(bozarConfig);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Cannot save config.");
        }
        return bozarConfig;
    }

    public void loadDefaultConfig() throws IOException {
        File f = new File("bozarConfig.json");
        if(f.exists() && f.isFile())
            this.loadConfig(f);
    }
}
