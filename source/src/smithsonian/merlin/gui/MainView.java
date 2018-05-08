package smithsonian.merlin.gui;

import javafx.scene.image.Image;
import smithsonian.merlin.gui.components.TopMenu;
import smithsonian.merlin.gui.panes.CheckInPane;
import smithsonian.merlin.gui.panes.CheckOutPane;
import smithsonian.merlin.gui.panes.SessionChoicePane;
import smithsonian.merlin.util.BarcodeManager;
import smithsonian.merlin.util.Location;
import smithsonian.merlin.util.FileManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.ini4j.Wini;
import smithsonian.merlin.util.Options;

import java.io.File;

/**
 * Created by albesmn on 7/25/2016.
 */
public class MainView extends Application {

    public final static int FIXED_WIDTH = 1366; // width of the application
    public final static int FIXED_HEIGHT = FIXED_WIDTH / 16 * 9; // height of the application
    public static Stage stage;
    private Scene scene;
    private VBox window;
    private TopMenu topMenu;

    private SessionChoicePane sessionChoicePane;
    private CheckOutPane checkOutPane;
    private CheckInPane checkInPane;

    public enum State {SESSION_CHOICE, CHECKOUT, CHECKIN}
    private State state;



    /**
     * Called on initiation; setting the stage parameters, adding a scene to it and showing it
     *
     * @param stage the primary stage
     * @throws Exception if creation fails
     */

    public void start(Stage stage) throws Exception {
        Options.loadOptions();
        FileManager.loadMuseumsFromSharedDrive();
        FileManager.loadDefaultLayoutFromSharedDrive();

        if (generateBarcodeImages())
            System.out.println("Barcodes created");


        File icon = new File("res/icon.png");
        // setup the stage
        stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("Merlin 1.0 - Choose Session");
        stage.setResizable(false);
        stage.setMinWidth(FIXED_WIDTH);
        stage.setMinHeight(FIXED_HEIGHT);
        stage.setMaxHeight(FIXED_WIDTH);
        stage.setMaxHeight(FIXED_HEIGHT);
        stage.getIcons().add(new Image(icon.toURI().toString()));
        stage.setOnCloseRequest(e -> {
            Options.saveOptions();
            System.exit(0);
        });

        sessionChoicePane = new SessionChoicePane(this);
        checkInPane = new CheckInPane(this);
        checkOutPane = new CheckOutPane(this);

        // basic program layout (MenuBar on top, Rest on bottom)
        topMenu = new TopMenu(this);
        window = new VBox();
        window.getChildren().add(topMenu);
        window.getChildren().add(sessionChoicePane);

        state = State.SESSION_CHOICE;
        scene = new Scene(window);
        scene.setOnKeyReleased(ke -> {
            switch (state) {
                case SESSION_CHOICE:
                    sessionChoicePane.handleKeyReleased(ke);
                    break;
                case CHECKOUT:
                    checkOutPane.handleKeyReleased(ke);
                    break;
                case CHECKIN:
                    checkInPane.handleKeyReleased(ke);
                    break;
            }
        });

        scene.setOnKeyTyped(ke -> {
            switch (state) {
                case SESSION_CHOICE:
                    sessionChoicePane.handleKeyTyped(ke);
                    break;
                case CHECKOUT:
                    checkOutPane.handleKeyTyped(ke);
                    break;
                case CHECKIN:
                    checkInPane.handleKeyTyped(ke);
                    break;
            }
        });

        // add a scene to the stage
        stage.setScene(scene);
        this.stage = stage;

        this.stage.show();
    }

    public void setState(State state) {
        this.state = state;
        switch (state) {
            case SESSION_CHOICE:
                window.getChildren().remove(1);
                window.getChildren().add(1, sessionChoicePane);
                stage.setTitle("Merlin 1.0 - Choose Session");
                break;
            case CHECKOUT:
                window.getChildren().remove(1);
                window.getChildren().add(1, checkOutPane);
                stage.setTitle("Merlin 1.0 - CheckOut");
                checkOutPane.init();
                break;
            case CHECKIN:
                window.getChildren().remove(1);
                window.getChildren().add(1, checkInPane);
                stage.setTitle("Merlin 1.0 - CheckIn");
                checkInPane.init();
                break;
        }
    }

    public void startFreshSession() {
        this.state = State.SESSION_CHOICE;
        sessionChoicePane = new SessionChoicePane(this);
        checkOutPane = new CheckOutPane(this);
        checkInPane = new CheckInPane(this);
        topMenu.setDisable(false);
        setState(state);
    }

    public boolean generateBarcodeImages() {
        Location[] locations = FileManager.loadLocations();
        boolean generated = false;
        for (int i = 0; i < locations.length; i++)
            if (BarcodeManager.generateBarcode(locations[i].getName(), "datamatrix", false)) generated = true;
        return generated;
    }
}
