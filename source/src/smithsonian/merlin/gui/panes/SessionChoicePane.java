package smithsonian.merlin.gui.panes;

import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import smithsonian.merlin.gui.MainView;
import smithsonian.merlin.gui.components.OptionBox;
import smithsonian.merlin.gui.components.Popup;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Callback;
import smithsonian.merlin.util.FileManager;
import smithsonian.merlin.util.Options;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by albesmn on 8/3/2016.
 */
public class SessionChoicePane extends VBox {

    private MainView mainView;
    private final OptionBox CMD_CHECKIN;
    private final OptionBox CMD_CHECKOUT;
    private final ComboBox museumComboBox;

    private String command;
    private String stream;

    public SessionChoicePane(MainView mainView) {
        this.mainView = mainView;
        CMD_CHECKIN = new OptionBox("###START_CHECKIN###", false, 150);
        CMD_CHECKOUT = new OptionBox("###START_CHECKOUT###", false, 150);
        museumComboBox = new ComboBox();

        CMD_CHECKOUT.setOnMousePressed(me -> {
            if (!CMD_CHECKOUT.getDisabled()) {
                command = CMD_CHECKOUT.getName();
                executeCommands();
            } else {
                museumComboBox.setStyle("-fx-font-size: 15pt; -fx-background-color: white; -fx-border-color: red;");
                TextFlow flow = new TextFlow(new Text("Please enter a museum."));
                flow.setTextAlignment(TextAlignment.CENTER);
                new Popup(MainView.stage, flow, 2, Color.RED);
            }
        });
        CMD_CHECKIN.setOnMousePressed(me -> {
            if (!CMD_CHECKIN.getDisabled()) {
                // check if shared drive is not set
                command = CMD_CHECKIN.getName();
                executeCommands();
            } else {
                museumComboBox.setStyle("-fx-font-size: 15pt; -fx-background-color: white; -fx-border-color: red;");
                TextFlow flow = new TextFlow(new Text("Please enter a museum."));
                flow.setTextAlignment(TextAlignment.CENTER);
                new Popup(MainView.stage, flow, 2, Color.RED);
            }
        });

        init();
    }

    private void init() {
        command = "";
        stream = "";

        VBox chooseMuseum = new VBox();
        Label caption = new Label("Please choose a museum");
        caption.setFont(Font.font(null, FontWeight.BOLD, 24));
        caption.setPadding(new Insets(20));
        museumComboBox.getItems().addAll(FileManager.loadMuseums());
        museumComboBox.setPromptText("Museum...");
        museumComboBox.setStyle("-fx-font-size: 15pt; -fx-background-color: white;");
        museumComboBox.setOnAction(e -> {
            if (museumComboBox.getValue() != null && !museumComboBox.getValue().toString().isEmpty()) {
                CMD_CHECKOUT.enable();
                CMD_CHECKIN.enable();
                museumComboBox.setStyle("-fx-font-size: 15pt; -fx-background-color: white;");
            } else {
                CMD_CHECKIN.disable();
                CMD_CHECKOUT.disable();
            }
        });

        // add padding to cells
        museumComboBox.setCellFactory(
                new Callback<ListView<String>, ListCell<String>>() {
                    public ListCell<String> call(ListView<String> param) {
                        final ListCell<String> cell = new ListCell<String>() {
                            {
                                super.setPrefWidth(300);
                            }

                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null) {
                                    setText(item);
                                    setPadding(new Insets(10));
                                } else {
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }
                });

        if (Options.museum != null && !Options.museum.isEmpty()) {
            museumComboBox.getSelectionModel().select(Options.museum);
            CMD_CHECKOUT.enable();
            CMD_CHECKIN.enable();
        } else {
            CMD_CHECKOUT.disable();
            CMD_CHECKIN.disable();
        }
        chooseMuseum.setAlignment(Pos.CENTER);
        chooseMuseum.getChildren().addAll(caption, museumComboBox);

        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(50));
        this.getChildren().addAll(chooseMuseum, showSessionOptions());
    }

    private VBox showSessionOptions() {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);

        Label caption = new Label("Do you want to Check-Out or Check-In items?");
        caption.setFont(Font.font(null, FontWeight.BOLD, 30));
        caption.setAlignment(Pos.CENTER);
        caption.setPadding(new Insets(20));

        HBox hbox = new HBox();
        hbox.getChildren().addAll(CMD_CHECKOUT, CMD_CHECKIN);
        hbox.setSpacing(300);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(10));

        vbox.setMinHeight(this.getHeight());
        vbox.setPadding(new Insets(100, 20, 20, 20));
        vbox.getChildren().addAll(caption, hbox);

        //topMenu.setDisable(true);

        return vbox;
    }

    public void handleKeyReleased(KeyEvent ke) {
        if (ke.getCode() == KeyCode.ENTER) {
            checkForCommands();
            executeCommands();
        }
    }

    public void handleKeyTyped(KeyEvent ke) {
        stream += ke.getCharacter();
        stream = stream.replace("\n", "").replace("\r", "");
        // debug
        // System.out.println(stream);
    }

    private void executeCommands() {
        if (command.equals(CMD_CHECKOUT.getName())) {
            if (Options.shared_folder_path != null && !Options.shared_folder_path.isEmpty()) {
                Options.museum = museumComboBox.getValue().toString();
                mainView.setState(MainView.State.CHECKOUT);
                command = "";
            } else {
                setSharedDriveDirectory();
            }
        } else if (command.equals(CMD_CHECKIN.getName())) {
            if (Options.shared_folder_path != null && !Options.shared_folder_path.isEmpty()) {
                Options.museum = museumComboBox.getValue().toString();
                mainView.setState(MainView.State.CHECKIN);
                command = "";
            } else {
                setSharedDriveDirectory();
            }
        }
    }

    public void checkForCommands() {
        // check if checkout or checkin
        if (stream.contains(CMD_CHECKIN.getName())) {
            command = CMD_CHECKIN.getName();
        } else if (stream.contains(CMD_CHECKOUT.getName())) {
            command = CMD_CHECKOUT.getName();
        }
    }

    private void setSharedDriveDirectory() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Please provide a path");
        alert.setHeaderText("Path for shared folder not set!");
        alert.setContentText("Please provide the path to the shared folder.\n" +
                "It is located on a shared drive and called 'dpo-merlin'.");
        alert.getDialogPane().setStyle("-fx-font-size: 16");
        alert.showAndWait();

        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose the shared folder and hit enter");
        File file = dc.showDialog(MainView.stage);


        String path = file.getAbsolutePath();

        if (path.endsWith("dpo-merlin")) {
            Options.shared_folder_path = path;

            Text text = new Text("Path set successfully.");
            TextFlow flow = new TextFlow(text);
            flow.setLineSpacing(10);
            flow.setTextAlignment(TextAlignment.CENTER);
            FileManager.loadMuseumsFromSharedDrive();
            FileManager.loadDefaultLayoutFromSharedDrive();
            new Popup(MainView.stage, flow, 2, Color.web("#99CC00"));
        } else {
            Text text = new Text("Path doesn't end with 'dpo-merlin'.");
            TextFlow flow = new TextFlow(text);
            flow.setLineSpacing(10);
            flow.setTextAlignment(TextAlignment.CENTER);
            new Popup(MainView.stage, flow, 2, Color.RED);
        }
    }
}
