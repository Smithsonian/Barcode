package smithsonian.merlin.gui.components;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import smithsonian.merlin.gui.MainView;
import smithsonian.merlin.util.FileManager;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import smithsonian.merlin.util.Options;

import java.io.File;

/**
 * Created by albesmn on 7/25/2016.
 */
public class TopMenu extends MenuBar {

    private MainView mainView;
    private Menu file, settings, help;

    public TopMenu(MainView mainView) {

        this.mainView = mainView;
        init();
    }

    private void init() {
        getStylesheets().add("smithsonian/merlin/gui/css/topmenu.css");

        file = new Menu("File");
        MenuItem newSession = new MenuItem("New Session...");
        newSession.setOnAction(event -> mainView.startFreshSession());
        file.getItems().add(newSession);

        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(event -> {
            Options.saveOptions();
            System.exit(0);
        });
        file.getItems().add(exit);

        // CheckMenuItem notifications = new CheckMenuItem("Benachrichtigungen");

        settings = new Menu("Settings");
        MenuItem editLocations = new MenuItem("Create new layout...");
        editLocations.setOnAction(event -> openLayoutEditor());
        settings.getItems().add(editLocations);
        MenuItem loadLayout = new MenuItem("Load layout...");
        loadLayout.setOnAction(event -> loadLayout());
        settings.getItems().add(loadLayout);
        MenuItem setSharedDriveDirectory = new MenuItem("Update shared folder directory...");
        setSharedDriveDirectory.setOnAction(event -> setSharedDriveDirectory());
        settings.getItems().add(setSharedDriveDirectory);

        help = new Menu("Help");
        help.getItems().add(new MenuItem("Help"));
        help.getItems().add(new MenuItem("About"));

        getMenus().addAll(file, settings, help);
    }

    private void loadLayout() {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("res/layouts"));
        fc.setTitle("Choose the layout file you want to load");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Layout Files (*.xml)", "*.xml");
        fc.getExtensionFilters().add(extFilter);
        File file = fc.showOpenDialog(MainView.stage);

        String name = file.getName().replace(".xml", "");
        Options.currentLayout = name;

        Text text = new Text("Layout <" + name + "> successfully loaded.\nProgram reset.");
        TextFlow flow = new TextFlow(text);
        flow.setLineSpacing(10);
        flow.setTextAlignment(TextAlignment.CENTER);
        new Popup(MainView.stage, flow, 2, Color.web("#99CC00"));
        mainView.startFreshSession();
    }

    private void openLayoutEditor() {
        Stage stage = new LayoutEditor(FileManager.loadLocations());
        stage.setResizable(false);
        stage.requestFocus();
        stage.setAlwaysOnTop(true);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(MainView.stage);
        stage.show();
    }

    private void setSharedDriveDirectory() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose the shared folder and hit enter");

        if (Options.shared_folder_path != null && !Options.shared_folder_path.isEmpty())
            dc.setInitialDirectory(new File(Options.shared_folder_path));
        File file = dc.showDialog(MainView.stage);

        String path = file.getAbsolutePath();

        if (path.endsWith("dpo-merlin")) {
            Options.shared_folder_path = path;

            Text text = new Text("Path updated successfully.");
            TextFlow flow = new TextFlow(text);
            flow.setLineSpacing(10);
            flow.setTextAlignment(TextAlignment.CENTER);
            new Popup(MainView.stage, flow, 2, Color.web("#99CC00"));
            FileManager.loadMuseumsFromSharedDrive();
            FileManager.loadDefaultLayoutFromSharedDrive();
            mainView.startFreshSession();
        } else {
            Text text = new Text("Path doesn't end with 'dpo-merlin'.");
            TextFlow flow = new TextFlow(text);
            flow.setLineSpacing(10);
            flow.setTextAlignment(TextAlignment.CENTER);
            new Popup(MainView.stage, flow, 2, Color.RED);
        }
    }
}
