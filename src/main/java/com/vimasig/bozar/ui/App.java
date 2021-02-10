package com.vimasig.bozar.ui;

import com.vimasig.bozar.obfuscator.Bozar;
import com.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import com.vimasig.bozar.obfuscator.utils.model.BozarMessage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // TODO: Auto updater

        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("/menu.fxml").openStream());
        Controller controller = fxmlLoader.getController();

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(this.getOptions(), this.getParameters().getRaw().toArray(new String[0]));

            if(cmd.hasOption("input"))
                controller.input.setText(cmd.getOptionValue("input"));
            if(cmd.hasOption("output"))
                controller.output.setText(cmd.getOptionValue("output"));
            if(cmd.hasOption("config"))
                try {
                    controller.configManager.loadConfig(new File(cmd.getOptionValue("config")));
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Cannot load config.");
                }

            if(cmd.hasOption("console")) {
                if(!cmd.hasOption("input") || !cmd.hasOption("output") || !cmd.hasOption("config"))
                    throw new IllegalArgumentException("Missing arguments: input, output, config");
                BozarConfig config = controller.configManager.generateConfig();
                Bozar bozar = new Bozar(new File(controller.input.getText()), Path.of(controller.output.getText()), config);
                bozar.run();
                System.exit(0);
            }

            Scene scene = new Scene(root);
            stage.setTitle(BozarMessage.VERSION_TEXT.toString());
            stage.setScene(scene);
            stage.show();
        } catch (ParseException e) {
            e.printStackTrace();
            System.err.println("Cannot parse command line.");
            System.exit(0);
        }
    }

    private Options getOptions() {
        final Options options = new Options();
        options.addOption(new Option("input", true, "Input file."));
        options.addOption(new Option("output", true, "Output file."));
        options.addOption(new Option( "cfg", "config", true, "Config file."));
        options.addOption(new Option("c", "console", false, "Application will run without GUI and obfuscation task will be started immediately."));
        return options;
    }
}
