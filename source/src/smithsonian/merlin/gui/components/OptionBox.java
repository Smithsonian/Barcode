package smithsonian.merlin.gui.components;

import smithsonian.merlin.util.BarcodeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.File;

/**
 * Created by albesmn on 7/26/2016.
 */
public class OptionBox extends VBox {

    private String name;
    private TextField input;
    private ImageView iv;
    private Image image;
    private String path;
    private boolean hasText;
    private int width;
    private boolean disabled;
    private Label caption;

    public OptionBox(String name, boolean hasText, int width, boolean black) {
        this.name = name;
        this.hasText = hasText;
        this.width = width;

        init();
    }

    public OptionBox(String name, boolean hasText, int width) {
        this(name, hasText, width, false);
    }

    public OptionBox(String name) {
        this(name, true, 170, false);
    }

    private void init() {
        // create barcode image and retrieve its path
        path = "res/barcodes/" + name + ".png";

        this.getStylesheets().add("smithsonian/merlin/gui/css/box.css");
        BarcodeManager.generateBarcode(name, "datamatrix", false);

        // create ImageView for displaying the loaded barcode image
        File file = new File(path);
        image = new Image(file.toURI().toString(), true);
        iv = new ImageView();
        iv.setFitWidth(width);
        iv.setPreserveRatio(true);
        iv.setImage(image);

        // create label
        caption = new Label(name.replace("###", ""));
        caption.setFont(new Font(width / 7));
        caption.setPadding(new Insets(5));
        caption.setAlignment(Pos.CENTER);

        setAlignment(Pos.CENTER);

        // create textfield
        if (hasText) {
            input = new TextField();
            input.setMaxWidth(iv.getFitWidth());
            input.setPadding(new Insets(5));
            input.setFont(new Font(20));
            input.setDisable(true);

            input.setBorder(new Border(new BorderStroke(Color.BLACK,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            getChildren().addAll(iv, caption, input);
        } else
            getChildren().addAll(iv, caption);
    }

    public void setText(String text) {
        input.setText(text);
    }

    public void disable() {
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(0.99);
        iv.setEffect(colorAdjust);
        disabled = true;
    }

    public void enable() {
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(0);
        iv.setEffect(colorAdjust);
        disabled = false;
    }

    public void focusRed() {
        if (hasText)
            input.setBorder(new Border(new BorderStroke(Color.RED,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        caption.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }

    public void focusGreen() {
        if (hasText)
            input.setBorder(new Border(new BorderStroke(Color.web("#99CC00"),
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        caption.setStyle("-fx-text-fill: #99CC00; -fx-font-weight: bold;");
    }

    public void removeFocus() {
        if (hasText)
            input.setBorder(new Border(new BorderStroke(Color.BLACK,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        caption.setStyle("-fx-text-fill: black; -fx-font-weight: normal;");
    }

    public String toString() {
        return name;
    }

    public void requestFocus() {
        input.requestFocus();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TextField getInput() {
        return input;
    }

    public void setInput(TextField input) {
        this.input = input;
    }

    public boolean getDisabled() {
        return disabled;
    }
}
