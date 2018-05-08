package smithsonian.merlin.gui.components;

import javafx.geometry.Insets;
import javafx.scene.layout.HBox;

/**
 * Created by albesmn on 7/26/2016.
 */
public class ScanOptionsBar extends HBox {

    public ScanOptionsBar() {
        setPadding(new Insets(5));

        getStyleClass().add("vbox");
        getStylesheets().add("smithsonian/merlin/gui/css/bar.css");
    }

    public void setBoxes(OptionBox... boxes) {
        getChildren().addAll(boxes);
    }

    public void addOptionsBox(OptionBox box) {
        getChildren().add(box);
    }
}
