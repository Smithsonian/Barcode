package smithsonian.merlin.gui.components;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.stage.Screen;
import javafx.stage.Stage;


/**
 * Created by albesmn on 8/17/2016.
 */
public class Popup extends javafx.stage.Popup {

    public Popup(Stage owner, TextFlow message, int duration, Color backgroundColor) {
        super();
        Color color = backgroundColor;
        TextFlow label = message;

        label.getStylesheets().add("smithsonian/merlin/gui/css/popup.css");
        label.getStyleClass().add("popup");
        label.setStyle("-fx-background-color: rgb(" + color.getRed() * 255 + "," + color.getGreen() * 255 + "," + color.getBlue() * 255 + ");");
        label.setMinWidth(400);
        label.requestFocus();
        getContent().add(label);

        setOnShowing(e -> new Thread(() -> {
            //Slowly increasy opacity to make it visible
            for (int i = 0; i <= 19; i++) {
                double opacity = (double) i / 20;
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                Platform.runLater(() -> {
                    setOpacity(opacity);
                });
            }

            //Show it for 2 seconds
            try {
                Thread.sleep(duration * 1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            //Decrease opacity to hide the popup
            for (int i = 19; i >= 0; i--) {
                double opacity = (double) i / 20;
                try {
                    Thread.sleep(40);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                Platform.runLater(() -> {
                    setOpacity(opacity);
                });
            }
            Platform.runLater(() -> hide());
        }).start());

        setOnShown(e -> {
            //Add the popup to the monitor with the application on it.
            Rectangle2D primaryScreenBounds;
            if (!owner.isIconified()) {
                ObservableList<Screen> screens = Screen.getScreensForRectangle(owner.getX(), owner.getY(), owner.getWidth(), owner.getHeight());
                primaryScreenBounds = screens.get(0).getVisualBounds();
            } else {
                primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            }
            setX((primaryScreenBounds.getMinX() + primaryScreenBounds.getWidth()) / 2 - getWidth() / 2);
            setY((primaryScreenBounds.getMinY() + primaryScreenBounds.getHeight()) / 2 - getHeight() / 2);
        });

        show(owner);
    }
}
