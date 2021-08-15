package io.github.vimasig.bozar.ui;

import com.google.gson.*;
import io.github.vimasig.bozar.obfuscator.transformer.impl.*;
import io.github.vimasig.bozar.obfuscator.transformer.impl.renamer.ClassRenamerTransformer;
import io.github.vimasig.bozar.obfuscator.transformer.impl.watermark.DummyClassTransformer;
import io.github.vimasig.bozar.obfuscator.transformer.impl.watermark.TextInsideClassTransformer;
import io.github.vimasig.bozar.obfuscator.transformer.impl.watermark.UnusedStringTransformer;
import io.github.vimasig.bozar.obfuscator.transformer.impl.watermark.ZipCommentTransformer;
import io.github.vimasig.bozar.obfuscator.utils.BozarUtils;
import io.github.vimasig.bozar.obfuscator.utils.Reflection;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
                try {
                    BozarConfig bozarConfig = new Gson().fromJson(jsonElement, BozarConfig.class);
                    var reflect = new Reflection<>(bozarConfig);
                    reflect.setDeclaredField("input", new File(((JsonObject)jsonElement).get("input").getAsString()));
                    reflect.setDeclaredField("output", Path.of(((JsonObject)jsonElement).get("output").getAsString()));
                    return bozarConfig;
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    return null;
                }
            };

            // Load config
            BozarConfig bozarConfig = new GsonBuilder()
                    .registerTypeAdapter(BozarConfig.class, deserializer)
                    .create()
                    .fromJson(str, BozarConfig.class);
            if(bozarConfig != null)
                this.loadConfig(bozarConfig);
            else throw new NullPointerException("bozarConfig");
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
        c.getComboBox(LineNumberTransformer.class).getSelectionModel().select(BozarUtils.getSerializedName(bozarConfig.getOptions().getLineNumbers()));
        c.getComboBox(LocalVariableTransformer.class).getSelectionModel().select(BozarUtils.getSerializedName(bozarConfig.getOptions().getLocalVariables()));
        c.getComboBox(ClassRenamerTransformer.class).getSelectionModel().select(BozarUtils.getSerializedName(bozarConfig.getOptions().getRename()));
        c.getCheckBox(SourceFileTransformer.class).setSelected(bozarConfig.getOptions().isRemoveSourceFile());
        c.getCheckBox(ShuffleTransformer.class).setSelected(bozarConfig.getOptions().isShuffle());
        c.getCheckBox(InnerClassTransformer.class).setSelected(bozarConfig.getOptions().isRemoveInnerClasses());
        c.getComboBox(LightControlFlowTransformer.class).getSelectionModel().select(BozarUtils.getSerializedName(bozarConfig.getOptions().getControlFlowObfuscation()));
        c.getCheckBox(CrasherTransformer.class).setSelected(bozarConfig.getOptions().isCrasher());
        c.getComboBox(ConstantTransformer.class).getSelectionModel().select(BozarUtils.getSerializedName(bozarConfig.getOptions().getConstantObfuscation()));

        // Watermark options
        c.getTextInputControl(DummyClassTransformer.class).setText(bozarConfig.getOptions().getWatermarkOptions().getDummyClassText());
        c.getTextInputControl(TextInsideClassTransformer.class).setText(bozarConfig.getOptions().getWatermarkOptions().getTextInsideClassText());
        c.getTextInputControl(UnusedStringTransformer.class).setText(bozarConfig.getOptions().getWatermarkOptions().getLdcPopText());
        c.getTextInputControl(ZipCommentTransformer.class).setText(bozarConfig.getOptions().getWatermarkOptions().getZipCommentText());
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
                    .setPrettyPrinting()
                    .create()
                    .toJson(bozarConfig)
            );
            fw.flush();
        }
    }

    public BozarConfig generateConfig() {
        var c = this.controller;

        BozarConfig.BozarOptions.WatermarkOptions watermarkOptions = new BozarConfig.BozarOptions.WatermarkOptions(
                c.getTextInputControl(DummyClassTransformer.class).getText(),
                c.getTextInputControl(TextInsideClassTransformer.class).getText(),
                c.getTextInputControl(UnusedStringTransformer.class).getText(),
                c.getTextInputControl(ZipCommentTransformer.class).getText()
        );
        BozarConfig.BozarOptions bozarOptions = new BozarConfig.BozarOptions(
                (BozarConfig.BozarOptions.RenameOption) c.getEnum(ClassRenamerTransformer.class),
                (BozarConfig.BozarOptions.LineNumberOption) c.getEnum(LineNumberTransformer.class),
                (BozarConfig.BozarOptions.LocalVariableOption) c.getEnum(LocalVariableTransformer.class),
                c.getCheckBox(SourceFileTransformer.class).isSelected(),
                c.getCheckBox(ShuffleTransformer.class).isSelected(),
                c.getCheckBox(InnerClassTransformer.class).isSelected(),
                (BozarConfig.BozarOptions.ControlFlowObfuscationOption) c.getEnum(LightControlFlowTransformer.class),
                c.getCheckBox(CrasherTransformer.class).isSelected(),
                (BozarConfig.BozarOptions.ConstantObfuscationOption) c.getEnum(ConstantTransformer.class),
                watermarkOptions
        );
        BozarConfig bozarConfig = new BozarConfig(c.input.getText(), c.output.getText(), c.exclude.getText(), this.controller.libraries.getItems(), bozarOptions);

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
