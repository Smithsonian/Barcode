package smithsonian.merlin.gui.components;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import smithsonian.merlin.gui.MainView;
import smithsonian.merlin.util.BarcodeManager;
import smithsonian.merlin.util.FileManager;
import smithsonian.merlin.util.Location;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import smithsonian.merlin.util.Options;

/**
 * Created by albesmn on 8/5/2016.
 */
public class LayoutEditor extends Stage {

    private Location[] locations;
    private TextField[] options;
    private CheckBox[] checkBoxes;
    private TextField layoutName;
    private Label caption;
    private Button save;
    private Button exit;
    private VBox layout;

    private final int FIXED_WIDTH = 400;
    private final int FIXED_HEIGHT = 800;

    public LayoutEditor(Location[] locations) {
        this.locations = locations;

        this.initStyle(StageStyle.UNDECORATED);
        this.setMinWidth(FIXED_WIDTH);
        this.setMinHeight(FIXED_HEIGHT);
        this.setMaxHeight(FIXED_WIDTH);
        this.setMaxHeight(FIXED_HEIGHT);
        init();
    }

    private void init() {
        layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(15);
        layout.setPadding(new Insets(20));
        layout.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        caption = new Label("Layout Editor");
        caption.setFont(Font.font(null, FontWeight.BOLD, 25));
        caption.setPadding(new Insets(10));
        layout.getChildren().add(caption);

        options = new TextField[locations.length];
        checkBoxes = new CheckBox[locations.length];
        for (int i = 0; i < locations.length; i++) {
            final int j = i; // for text field

            // textfields
            options[locations[i].getLevel()] = new TextField(locations[locations[i].getLevel()].getName().replace("###", ""));
            options[locations[i].getLevel()].setPadding(new Insets(10));
            options[locations[i].getLevel()].setFont(new Font(17));
            options[locations[i].getLevel()].setBorder(new Border(new BorderStroke(Color.BLACK,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            options[locations[i].getLevel()].setOnMouseClicked(e -> options[locations[j].getLevel()].selectAll()); // select all text on click

            // label
            String level = null;
            switch (locations[i].getLevel()) {
                case 0:
                    level = "Top Level";
                    break;
                case 1:
                    level = "2nd Level";
                    break;
                case 2:
                    level = "3rd Level";
                    break;
                case 3:
                    level = "4th Level";
                    break;
                case 4:
                    level = "5th Level";
                    break;
                case 5:
                    level = "6th Level";
                    break;
            }
            Label levelLabel = new Label(level);
            levelLabel.setFont(Font.font(null, FontWeight.BOLD, 18));
            levelLabel.setPadding(new Insets(10, 20, 10, 10));

            // checkboxes
            checkBoxes[locations[i].getLevel()] = new CheckBox();
            checkBoxes[locations[i].getLevel()].setPadding(new Insets(10, 0, 10, 10));
            checkBoxes[locations[i].getLevel()].setFont(new Font(17));
            checkBoxes[locations[i].getLevel()].setIndeterminate(false);
            if (!options[locations[i].getLevel()].getText().equals("NOT USED"))
                checkBoxes[locations[i].getLevel()].setSelected(true);
            else
                options[locations[i].getLevel()].setDisable(true);
            checkBoxes[locations[i].getLevel()].selectedProperty().addListener(new ChangeListener<Boolean>() {
                public void changed(ObservableValue<? extends Boolean> ov,
                                    Boolean old_val, Boolean new_val) {
                    options[locations[j].getLevel()].setDisable(!new_val);
                    if (!new_val)
                        options[locations[j].getLevel()].setText("NOT USED");
                    else
                        options[locations[j].getLevel()].setText("");
                }
            });

            HBox locationBox = new HBox();
            HBox.setHgrow(levelLabel, Priority.ALWAYS);
            HBox.setHgrow(checkBoxes[locations[i].getLevel()], Priority.ALWAYS);
            HBox.setHgrow(options[locations[i].getLevel()], Priority.ALWAYS);
            locationBox.getChildren().addAll(checkBoxes[locations[i].getLevel()], levelLabel, options[locations[i].getLevel()]);
            layout.getChildren().add(locationBox);
        }


        layoutName = new TextField();
        layoutName.setPromptText("Enter layout name");
        layoutName.setPadding(new Insets(10));
        layoutName.setFont(new Font(17));
        layoutName.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        Label status = new Label("");
        status.setPadding(new Insets(10));
        status.setFont(Font.font(null, FontWeight.BOLD, 16));
        status.setTextFill(Color.RED);
        status.setAlignment(Pos.CENTER);

        save = new Button("SAVE & EXIT");
        exit = new Button("DISCARD & EXIT");
        save.setMinWidth(FIXED_WIDTH);
        exit.setMinWidth(FIXED_WIDTH);
        save.setPadding(new Insets(10));
        exit.setPadding(new Insets(10));
        save.setFont(new Font(17));
        exit.setFont(new Font(17));

        save.setOnAction(e -> {
            Location[] newLocations = new Location[locations.length];
            boolean save = true;
            if (layoutName.getText().trim().isEmpty()) {
                save = false;
                status.setText("PLEASE PROVIDE A LAYOUT NAME");
                layoutName.setBorder(new Border(new BorderStroke(Color.RED,
                        BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            } else if (layoutName.getText().trim().toLowerCase().equals("default")) {
                save = false;
                status.setText("LAYOUT NAME CAN'T BE DEFAULT");
                layoutName.setBorder(new Border(new BorderStroke(Color.RED,
                        BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            } else if (!layoutName.getText().matches("[a-zA-Z0-9-_]*$")) {
                save = false;
                status.setText("ONLY [ _ ] AND [ - ] ALLOWED");
                layoutName.setBorder(new Border(new BorderStroke(Color.RED,
                        BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            }

            for (int i = 0; i < locations.length; i++) {
                if (!options[locations[i].getLevel()].getText().trim().isEmpty() && options[locations[i].getLevel()].getText().length() >= 3) {
                    options[locations[i].getLevel()].setBorder(new Border(new BorderStroke(Color.BLACK,
                            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                    Location loc = locations[locations[i].getLevel()];
                    loc.setName("###" + options[locations[i].getLevel()].getText().toUpperCase() + "###");
                    newLocations[locations[i].getLevel()] = loc;
                    BarcodeManager.generateBarcode(newLocations[locations[i].getLevel()].getName(), "datamatrix", false); // generate new barcode images
                } else if (options[locations[i].getLevel()].getText().length() < 3) {
                    save = false;
                    status.setText("MINIMUM LENGTH 3 CHARACTERS");
                    options[locations[i].getLevel()].setBorder(new Border(new BorderStroke(Color.RED,
                            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                } else {
                    save = false;
                    status.setText("PLEASE FILL IN ALL FIELDS");

                    options[locations[i].getLevel()].setBorder(new Border(new BorderStroke(Color.RED,
                            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                }
            }

            if (save) {
                FileManager.saveLocations(layoutName.getText(), newLocations);
                Options.currentLayout = layoutName.getText();
                Options.saveOptions();
                Text text = new Text("New Layout <" + layoutName.getText() + "> saved successfully.");
                TextFlow flow = new TextFlow(text);
                flow.setLineSpacing(10);
                flow.setTextAlignment(TextAlignment.CENTER);
                new Popup(MainView.stage, flow, 2, Color.web("#99CC00"));
                this.close();
            }
        });
        exit.setOnAction(e -> this.close());
        layout.getChildren().addAll(layoutName, status, save, exit);


        Scene scene = new Scene(layout);
        this.setScene(scene);
    }
}
