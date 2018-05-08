package smithsonian.merlin.gui.panes;

import smithsonian.merlin.gui.MainView;
import smithsonian.merlin.gui.components.BarcodeTable;
import smithsonian.merlin.gui.components.OptionBox;
import smithsonian.merlin.gui.components.Popup;
import smithsonian.merlin.gui.components.ScanOptionsBar;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import smithsonian.merlin.util.*;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by albesmn on 8/3/2016.
 */
public class CheckOutPane extends BorderPane {

    private MainView mainView;
    private BarcodeTable barcodeTable;
    private ScanOptionsBar optionsTop;
    private ScanOptionsBar optionsBottom;

    // command boxes
    private final int CMD_WIDTH = 130;
    private final OptionBox CMD_STARTSCAN;
    private final OptionBox CMD_ENDSCAN;
    private final OptionBox CMD_END;

    private OptionBox[] locationBoxes;
    private Location[] locations;
    private Location[] itemLocations;

    private boolean scanningItems;
    private boolean enteringCart;
    private boolean sure;
    private String command = "";
    private String stream = "";
    private String cartNumber = "";
    private String timeStamp = null;
    private int lastInsert = 0;

    /**
     * Adding all actors to a scene, organized in a BorderPane
     *
     * @return the scene with applied layout
     */
    public CheckOutPane(MainView mainView) {
        this.mainView = mainView;

        CMD_STARTSCAN = new OptionBox("###START_SCAN###", false, CMD_WIDTH);
        CMD_ENDSCAN = new OptionBox("###END_SCAN###", false, CMD_WIDTH);
        CMD_END = new OptionBox("###END_CHECKOUT###", false, CMD_WIDTH);
    }

    public void init() {
        locations = FileManager.loadLocations();

        // MainView.topMenu.setDisable(true);
        getStyleClass().add("borderpane");
        getStylesheets().add("smithsonian/merlin/gui/css/session.css");

        optionsTop = new ScanOptionsBar();
        barcodeTable = new BarcodeTable();
        optionsBottom = new ScanOptionsBar();

        // create OptionBoxes
        itemLocations = new Location[6];
        locationBoxes = new OptionBox[locations.length];
        for (int i = 0; i < locations.length; i++)
            locationBoxes[locations[i].getLevel()] = new OptionBox(locations[i].getName(), true, CMD_WIDTH, true);

        // prefill museum
        // System.out.println(SessionChoicePane.museum);
        locationBoxes[0].setText(Options.museum);
        itemLocations[0] = new Location(locations[0].getName(), Options.museum, 0);

        // add OptionBoxes to ScanOptionsBars
        optionsTop.setBoxes(locationBoxes[0], locationBoxes[1], locationBoxes[2]);
        optionsBottom.setBoxes(locationBoxes[3], locationBoxes[4], locationBoxes[5]);

        CMD_ENDSCAN.setPadding(new Insets(10));
        CMD_STARTSCAN.setPadding(new Insets(10));
        CMD_END.setPadding(new Insets(5));

        // add all elements to the this
        this.setTop(optionsTop);
        this.setRight(CMD_END);
        this.setLeft(CMD_STARTSCAN);
        this.setBottom(optionsBottom);
        Label cart = showCartInputDialog();
        cart.setMinHeight(260);
        this.setCenter(cart);

        optionsTop.setSpacing((MainView.FIXED_WIDTH - optionsTop.getChildren().size() * CMD_WIDTH) / 2 - 10);
        optionsBottom.setSpacing((MainView.FIXED_WIDTH - optionsBottom.getChildren().size() * CMD_WIDTH) / 2 - 10);
        barcodeTable.setMinHeight(this.getHeight() - 2 * locationBoxes[0].getHeight() - 40);
        barcodeTable.setMaxWidth(MainView.FIXED_WIDTH - CMD_ENDSCAN.getWidth() - CMD_STARTSCAN.getWidth() - 40);

        disableAllBoxes();
    }

    private Label showCartInputDialog() {
        enteringCart = true;
        Label caption = new Label("Please scan in your cart number to continue");
        caption.setFont(Font.font(null, FontWeight.BOLD, 25));
        caption.setStyle("-fx-text-fill: #99CC00;");
        caption.setAlignment(Pos.CENTER);
        caption.setPadding(new Insets(30));

        disableAllBoxes();

        return caption;
    }

    public void handleKeyReleased(KeyEvent ke) {
        if (ke.getCode() == KeyCode.ENTER) {
            if (enteringCart) {
                if (!stream.trim().isEmpty()) {
                    cartNumber = stream;
                    if (isInteger(cartNumber) && Integer.parseInt(cartNumber) < 10)
                        cartNumber = "0" + cartNumber;
                    enteringCart = false;
                    MainView.stage.setTitle(MainView.stage.getTitle() + " - CART#" + cartNumber);
                    this.setCenter(barcodeTable);
                    for (int i = 0; i < locationBoxes.length; i++) {
                        if (!locationBoxes[i].getName().equals("###NOT USED###"))
                            locationBoxes[i].enable();
                    }
                    CMD_END.disable();
                    CMD_STARTSCAN.enable();
                }
            } else {
                checkForCommands();
                executeCommands();
                if (scanningItems) {
                    insertListData();
                    for (int i = 0; i < locationBoxes.length; i++)
                        locationBoxes[i].removeFocus();
                } else {
                    for (int i = 0; i < locationBoxes.length; i++) {
                        if (locationBoxes[i].getName().equals(command))
                            locationBoxes[i].focusGreen();
                        else
                            locationBoxes[i].removeFocus();
                        if (locationBoxes[i].getName().equals(command) && !stream.trim().isEmpty()) {
                            locationBoxes[i].setText(stream);
                            itemLocations[locations[i].getLevel()] = new Location(locations[i].getName(), stream, locations[i].getLevel());
                            locationBoxes[i].removeFocus();
                            command = "";
                            clearStream();
                        }
                    }
                }
            }
        } else if (ke.getCode() == KeyCode.DELETE || ke.getCode() == KeyCode.BACK_SPACE) { // avoid illegal characters in item names on deletion
            clearStream();
        }
    }

    public void handleKeyTyped(KeyEvent ke) {
        stream += ke.getCharacter();
        stream = stream.replace("\n", "").replace("\r", "");

        // debug
        // System.out.println(stream);
    }

    private void checkForCommands() {
        for (int i = 0; i < locations.length; i++) {
            if (stream.contains(locations[i].getName())) {
                clearStream();
                command = locations[i].getName();
                sure = false;
                break;
            }
        }

        // check if end or start scan
        if (stream.contains(CMD_ENDSCAN.getName())) {
            clearStream();
            command = CMD_ENDSCAN.getName();
        } else if (stream.contains(CMD_STARTSCAN.getName())) {
            clearStream();
            command = CMD_STARTSCAN.getName();
        } else if (stream.contains(CMD_END.getName())) {
            clearStream();
            command = CMD_END.getName();
        }

        // debug
        // System.out.println("Command: " + command);
    }

    private void executeCommands() {
        if (command.equals(CMD_STARTSCAN.getName())) {
            if (timeStamp == null) {
                timeStamp = new SimpleDateFormat("MM-dd-yyyy-HH-mm").format(new Date());
                DBController.createDB(new Session(System.getProperty("user.name"), cartNumber, timeStamp, null));
            }
            scanningItems = true;
            sure = false;
            disableAllBoxes();
            CMD_ENDSCAN.enable();
            Label ph = new Label("You can now start scanning in items");
            ph.setFont(new Font(20));
            barcodeTable.setPlaceholder(ph);
            this.setRight(CMD_ENDSCAN);
        } else if (command.equals(CMD_ENDSCAN.getName())) {
            scanningItems = false;

            for (int i = 0; i < locationBoxes.length; i++) {
                if (!locationBoxes[i].getName().equals("###NOT USED###"))
                    locationBoxes[i].enable();
                CMD_ENDSCAN.disable();
                CMD_STARTSCAN.enable();
            }
            if (barcodeTable.getItems().size() > 0) CMD_END.enable();
            this.setRight(CMD_END);

            if (lastInsert != getBarcodeTable().getItems().size())
                saveSession(); // save session

        } else if (command.equals(CMD_END.getName())) {
            // make sure END_CHECKOUT was not hit accidentally
            if (!sure) {
                Text text = new Text("Please scan in END_CHECKOUT again to confirm.");
                TextFlow flow = new TextFlow(text);
                flow.setLineSpacing(10);
                flow.setTextAlignment(TextAlignment.CENTER);
                new Popup(MainView.stage, flow, 2, Color.web("#99CC00"));
                sure = true;
            } else {
                ReportManager.savePDF(new Session(System.getProperty("user.name"), cartNumber, timeStamp, "new", getBarcodeTable().getItems()));
                Text text = new Text("Checkout session saved successfully.\n" +
                        "Timestamp: " + timeStamp + "\n" +
                        getBarcodeTable().getItems().size() + " items scanned in total.");
                TextFlow flow = new TextFlow(text);
                flow.setLineSpacing(10);
                flow.setTextAlignment(TextAlignment.CENTER);
                new Popup(MainView.stage, flow, 5, Color.web("#99CC00"));
                mainView.startFreshSession();
            }
            command = "";
        }
    }

    private void insertListData() {
        sure = false;
        if (!stream.trim().isEmpty()) {
            if (!checkForDuplicate()) {
                Location[] location = itemLocations.clone();
                Item item = new Item(stream, location);
                barcodeTable.addItem(item);
            }
            clearStream();
        }

        // debug
        // for (int i = 0; i < itemLocations.length; i++) if (itemLocations[i] != null)  System.out.println(itemLocations[i].getName() + itemLocations[i].getValue());
    }

    private boolean checkForDuplicate() {
        boolean result = false;
        for (int i = 0; i < barcodeTable.getItems().size(); i++) {
            if (barcodeTable.getItems().get(i).getBarcode().equals(stream)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private void disableAllBoxes() {
        for (int i = 0; i < locationBoxes.length; i++) {
            locationBoxes[i].disable();
        }
        CMD_ENDSCAN.disable();
        CMD_STARTSCAN.disable();
        CMD_END.disable();
    }

      /*
           UTILITY SECTION
     */

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    private void clearStream() {
        stream = "";
    }

    private void saveSession() {
        // save items to database
        DBController.useDatabase(new Session(System.getProperty("user.name"), cartNumber, timeStamp, "new", null));
        DBController.saveItemsInCurrentLocation(
                FXCollections.observableArrayList(
                        getBarcodeTable().getItems().subList(lastInsert, getBarcodeTable().getItems().size()))); // only save new items
        lastInsert = getBarcodeTable().getItems().size();

        // save to local drive
        FileManager.writeSessionToFile(new Session(System.getProperty("user.name"), cartNumber, timeStamp, "new", getBarcodeTable().getItems()));

        // save to shared folder

    }

    public BarcodeTable getBarcodeTable() {
        return barcodeTable;
    }

}
