package com.vimasig.bozar.ui;

import com.google.gson.*;
import com.vimasig.bozar.obfuscator.utils.BozarUtils;
import com.vimasig.bozar.obfuscator.utils.model.BozarConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Controller controller;
    public ConfigManager(Controller controller) {
        this.controller = controller;
    }

    public void loadConfig(File file) throws IOException {
        String str = Files.readString(file.toPath());
        BozarConfig bozarConfig = this.gson.fromJson(str, BozarConfig.class);
        this.loadConfig(bozarConfig);
    }

    public void loadConfig(BozarConfig bozarConfig) {
        var c = this.controller;
        c.exclude.setText(bozarConfig.getExclude());
        c.libraries.getItems().addAll(bozarConfig.getLibraries());
        c.optionLineNumbers.getSelectionModel().select(BozarUtils.getSerializedName(bozarConfig.getOptions().getLineNumbers()));
        c.optionLocalVariables.getSelectionModel().select(BozarUtils.getSerializedName(bozarConfig.getOptions().getLocalVariables()));
        c.optionRemoveSourceFile.setSelected(bozarConfig.getOptions().isRemoveSourceFile());
        c.optionControlFlowObf.setSelected(bozarConfig.getOptions().isControlFlowObfuscation());
        c.optionConstantObf.setSelected(bozarConfig.getOptions().isConstantObfuscation());
    }

    public void saveConfig(BozarConfig bozarConfig) throws IOException {
        try (FileWriter fw = new FileWriter("bozarConfig.json")) {
            fw.write(this.gson.toJson(bozarConfig));
            fw.flush();
        }
    }

    public BozarConfig generateConfig() {
        JsonObject json = new JsonObject();
        var c = this.controller;
        json.add("input", new JsonPrimitive(c.input.getText()));
        json.add("output", new JsonPrimitive(c.output.getText()));
        json.add("exclude", new JsonPrimitive(c.exclude.getText()));
        json.add("libraries", this.getLibraries());

        JsonObject options = new JsonObject();
        options.add("lineNumbers", new JsonPrimitive(c.optionLineNumbers.getSelectionModel().getSelectedItem()));
        options.add("localVariables", new JsonPrimitive(c.optionLocalVariables.getSelectionModel().getSelectedItem()));
        options.add("removeSourceFile", new JsonPrimitive(c.optionRemoveSourceFile.isSelected()));
        options.add("controlFlowObfuscation", new JsonPrimitive(c.optionControlFlowObf.isSelected()));
        options.add("constantObfuscation", new JsonPrimitive(c.optionConstantObf.isSelected()));
        json.add("options", options);

        BozarConfig bozarConfig = this.gson.fromJson(json, BozarConfig.class);
        try {
            this.saveConfig(bozarConfig);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Cannot save config.");
        }
        return bozarConfig;
    }

    private JsonArray getLibraries() {
        List<String> i = new ArrayList<>(this.controller.libraries.getItems());
        JsonArray arr = new JsonArray(i.size());
        i.forEach(s -> arr.add(new JsonPrimitive(s)));
        return arr;
    }
}
