package smithsonian.merlin.gui.components;

import smithsonian.merlin.util.Item;
import smithsonian.merlin.util.Location;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Created by albesmn on 8/4/2016.
 */
public class LocationTable extends VBox {

    private Location[] locations;
    private LocationBox[] boxes;
    private Label caption;
    private boolean disabled;

    public LocationTable(Location[] locations, boolean disabled) {
        this.locations = locations;
        this.disabled = disabled;

        init();
    }

    private void init() {
        caption = new Label("Location");
        caption.setFont(Font.font(null, FontWeight.BOLD, 25));
        caption.setPadding(new Insets(15, 10, 15, 10));
        boxes = new LocationBox[locations.length];
        for (int i = 0; i < locations.length; i++) {
            boxes[locations[i].getLevel()] = new LocationBox(locations[locations[i].getLevel()], disabled);
        }

        this.getChildren().add(caption);
        this.getChildren().addAll(boxes);
    }

    public void displayLocation(Item item) {
        caption.setText("Location - Item " + item.getBarcode());
        Location[] locs = item.getLocation();
        clearBoxes();

        for (int i = 0; i < locs.length; i++) {
            if (locs[i] != null)
                boxes[locs[i].getLevel()].changeValue(locs[i].getValue());
        }
    }

    private void clearBoxes() {
        for (int i = 0; i < boxes.length; i++) {
            boxes[locations[i].getLevel()].changeValue("");
        }
    }


    private class LocationBox extends HBox {

        private Location location;
        private Label caption;
        private TextField locationValue;
        private boolean disabled;

        public LocationBox(Location location, boolean disabled) {
            this.location = location;
            this.disabled = disabled;

            init();
        }

        private void init() {
            this.getStylesheets().add("smithsonian/merlin/gui/css/box.css");

            caption = new Label(location.getName().replace("###", ""));
            caption.setFont(new Font(20));
            caption.setPadding(new Insets(5));
            caption.setMinWidth(200);
            caption.setMaxWidth(200);
            locationValue = new TextField();
            locationValue.setFont(new Font(20));
            locationValue.setMinWidth(250);
            locationValue.setMaxWidth(250);
            locationValue.setPadding(new Insets(5));
            locationValue.setDisable(disabled);

            this.setPadding(new Insets(5));
            this.getChildren().addAll(caption, locationValue);
        }

        public void changeValue(String value) {
            locationValue.setText(value);
        }
    }
}
