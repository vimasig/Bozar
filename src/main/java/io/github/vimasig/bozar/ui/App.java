package io.github.vimasig.bozar.ui;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.utils.BozarUtils;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarMessage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.cli.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // FX GUI
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("/menu.fxml").openStream());
        Controller controller = fxmlLoader.getController();

        // Handle command lines
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(this.getOptions(), this.getParameters().getRaw().toArray(new String[0]));

            if(cmd.hasOption("config"))
                try {
                    controller.configManager.loadConfig(new File(cmd.getOptionValue("config")));
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Cannot load config.");
                }
            if(cmd.hasOption("input"))
                controller.input.setText(cmd.getOptionValue("input"));
            if(cmd.hasOption("output"))
                controller.output.setText(cmd.getOptionValue("output"));

            // Update checker
            String latestVer = null;
            if(!cmd.hasOption("noupdate"))
                try {
                    latestVer = BozarUtils.getLatestVersion();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

            // Console mode
            if(cmd.hasOption("console")) {
                if(!cmd.hasOption("noupdate")) {
                    if(latestVer == null)
                        controller.log(BozarMessage.CANNOT_CHECK_UPDATE.toString());
                    else if(!BozarUtils.getVersion().equals(latestVer))
                        controller.log(BozarMessage.NEW_UPDATE_AVAILABLE.toString() + latestVer);
                }

                BozarConfig config = controller.configManager.generateConfig();
                Bozar bozar = new Bozar(config);
                bozar.run();
                System.exit(0);
            }

            if(latestVer == null)
                JOptionPane.showMessageDialog(null, BozarMessage.CANNOT_CHECK_UPDATE.toString(), BozarMessage.VERSION_TEXT.toString(), JOptionPane.ERROR_MESSAGE);
            else if(!BozarUtils.getVersion().equals(latestVer)){
                var message = BozarMessage.NEW_UPDATE_AVAILABLE.toString() + latestVer + System.lineSeparator() + "Do you want to go to the site?";
                if(JOptionPane.showConfirmDialog(null, message, BozarMessage.VERSION_TEXT.toString(), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == 0)
                    BozarUtils.openDownloadURL();
            }

            // GUI
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
        options.addOption(new Option( "noupdate", "Disable update warnings"));
        options.addOption(new Option("c", "console", false, "Application will run without GUI and obfuscation task will be started immediately."));
        return options;
    }
}
