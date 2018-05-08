package smithsonian.merlin.gui.panes;

import javafx.scene.text.*;
import smithsonian.merlin.gui.MainView;
import smithsonian.merlin.gui.components.ItemLocationList;
import smithsonian.merlin.gui.components.LocationTable;
import smithsonian.merlin.gui.components.OptionBox;
import smithsonian.merlin.gui.components.Popup;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import smithsonian.merlin.util.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by albesmn on 8/11/2016.
 */
public class CheckInPane extends HBox {

    private MainView mainView;
    private String stream;
    private String command;

    private ItemLocationList itemLocationList;
    private Label status;
    private Location[] locations;
    private LocationTable locationTable;

    private final OptionBox CMD_UNDO;
    private final OptionBox CMD_END;
    private String timeStampOut = null;
    private String timeStampIn = null;
    private String cartNumber;
    private boolean sure;


    public CheckInPane(MainView mainView) {
        this.mainView = mainView;
        CMD_UNDO = new OptionBox("###UNDO###", false, 150);
        CMD_END = new OptionBox("###END_CHECKIN###", false, 150);

        command = "";
        clearStream();
    }


    public void init() {
        getStylesheets().add("smithsonian/merlin/gui/css/table.css");
        getStyleClass().add("white");
        getStylesheets().add("smithsonian/merlin/gui/css/session.css");

        // MainView.topMenu.setDisable(true);
        status = new Label("Please scan in the item you want to check back in.");
        status.setFont(Font.font(null, FontWeight.BOLD, 20));
        status.setPadding(new Insets(10));
        locations = FileManager.loadLocations();
        locationTable = new LocationTable(locations, true);

        HBox commands = new HBox();
        commands.setAlignment(Pos.CENTER);
        commands.setSpacing(150);
        commands.getChildren().addAll(CMD_UNDO, CMD_END);

        VBox options = new VBox();
        options.setSpacing(30);
        options.getChildren().addAll(locationTable, status, commands);

        loadSession();
        itemLocationList = new ItemLocationList(locationTable);

        this.getChildren().addAll(itemLocationList, options);
        this.setSpacing(50);

        CMD_UNDO.disable();
        CMD_UNDO.setOnMousePressed(me -> {
            if (!CMD_UNDO.getDisabled()) {
                command = CMD_UNDO.getName();
                executeCommands();
            }
        });

        CMD_END.focusRed();
        CMD_END.setOnMousePressed(me -> {
            if (!CMD_END.getDisabled()) {
                command = CMD_END.getName();
                executeCommands();
            }
        });
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

    private void checkForCommands() {
        if (stream.contains(CMD_UNDO.getName())) {
            command = CMD_UNDO.getName();
            clearStream();
        } else if (stream.contains(CMD_END.getName())) {
            command = CMD_END.getName();
            clearStream();
        }
    }

    private void executeCommands() {
        if (command.equals(CMD_UNDO.getName())) {
            Item temp = itemLocationList.undo();
            DBController.checkInItem(temp);
            command = "";
            CMD_UNDO.disable();
            CMD_END.focusRed();
            status.setStyle("-fx-text-fill: black;");
            status.setText("Item " + temp.getBarcode() + " was put back into the cart.");
            sure = false;
        } else if (command.equals(CMD_END.getName())) {
            saveSessionToXML();
            if (itemLocationList.getCheckedOutItems().isEmpty()) {
                saveSessionToPDF();
                Text text = new Text("All items checked back in successfully.\n" +
                        "Timestamp: " + timeStampOut + "\n" +
                        itemLocationList.getItems().size() + " items scanned in total.\n" +
                        "A PDF report has been created.");
                TextFlow flow = new TextFlow(text);
                flow.setLineSpacing(10);
                flow.setTextAlignment(TextAlignment.CENTER);
                new Popup(MainView.stage, flow, 5, Color.web("#99CC00"));
                mainView.startFreshSession();
            } else {
                // make sure END_CHECKIN was not hit accidentally
                if (!sure) {
                    Text text = new Text(itemLocationList.getCheckedOutItems().size() + " item(s) still missing.\n" +
                            "Please scan in END_CHECKIN again to confirm.");
                    TextFlow flow = new TextFlow(text);
                    flow.setLineSpacing(10);
                    flow.setTextAlignment(TextAlignment.CENTER);
                    new Popup(MainView.stage, flow, 2, Color.RED);
                    sure = true;
                } else {
                    saveSessionToPDF();
                    Text text = new Text("Checkin ended with " + itemLocationList.getCheckedOutItems().size() + " item(s) missing.\n" +
                            "Please report this to the Project POC.");
                    TextFlow flow = new TextFlow(text);
                    flow.setLineSpacing(10);
                    flow.setTextAlignment(TextAlignment.CENTER);
                    new Popup(MainView.stage, flow, 5, Color.RED);
                    mainView.startFreshSession();
                }
            }
            command = "";
        } else {
            checkItemIn();
        }
    }

    private void checkItemIn() {
        if (timeStampIn == null) // set timeStampIn
            timeStampIn = new SimpleDateFormat("MM-dd-yyyy-HH-mm").format(new Date());

        sure = false;
        Item item;
        if ((item = itemLocationList.getItemByBarcode(stream)) != null) {
            if (itemLocationList.checkInItem(stream, timeStampIn) != null) {
                status.setStyle("-fx-text-fill: #99CC00;");
                status.setText("Item " + item.getBarcode() + " was checked back in. (" + itemLocationList.getCheckedOutItems().size() + " left)");
                DBController.checkInItem(item);
                CMD_UNDO.enable();
            } else {
                status.setStyle("-fx-text-fill: red;");
                status.setText("Item " + stream + " is already checked in. (" + itemLocationList.getCheckedOutItems().size() + " left)");
            }
        } else if (!stream.trim().isEmpty()){
            status.setStyle("-fx-text-fill: red;");
            status.setText("Item " + stream + " is not in this badge. (" + itemLocationList.getCheckedOutItems().size() + " left)");
        }

        if (itemLocationList.getCheckedOutItems().isEmpty()) {
            CMD_END.focusGreen();
        }

        clearStream();
        // debug
        // for (int i = 0; i < items.size(); i++) System.out.println(items.get(i).getBarcode());
    }

    private void saveSessionToXML() {
        ObservableList<Item> items = itemLocationList.getItems();
        String status = "ongoing";
        if (itemLocationList.getCheckedOutItems().isEmpty()) status = "finished";
        FileManager.writeSessionToFile(new Session(System.getProperty("user.name"), cartNumber, timeStampOut, status, items));
    }

    private void saveSessionToPDF() {
        ObservableList<Item> items = itemLocationList.getItems();
        String status = "ongoing";
        if (itemLocationList.getCheckedOutItems().isEmpty()) status = "finished";
        ReportManager.savePDF(new Session(System.getProperty("user.name"), cartNumber, timeStampOut, status, items));
    }

    private void loadSession() {
        try {
            FileChooser fc = new FileChooser();
            File dir = new File(Options.shared_folder_path + "/" + Options.museum);
            dir.mkdirs();
            fc.setInitialDirectory(dir);
            fc.setTitle("Choose the session file you want to load");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Session Files (*.xml)", "*.xml");
            fc.getExtensionFilters().add(extFilter);
            File file = fc.showOpenDialog(mainView.stage);

            Session session = FileManager.loadSessionFromFile(file.getPath()); // TODO change
            DBController.useDatabase(session); // tell DBController which database to use
            timeStampOut = session.getTimeStamp();
            cartNumber = session.getCart();
            MainView.stage.setTitle("Merlin 1.0 - CheckIn - CART#" + cartNumber);
        } catch (Exception e) {
            mainView.startFreshSession();
        }
    }

    private void clearStream() {
        stream = "";
    }
}