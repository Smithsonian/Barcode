package smithsonian.merlin.gui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import smithsonian.merlin.util.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by albesmn on 8/11/2016.
 */
public class ItemLocationList extends ScrollPane {

    private ListView<Item>[] lists;
    private ObservableList<Item> allItems;
    private LocationTable locationTable;
    private GridPane gp;

    private Item lastItem;
    private Position lastItemPos;

    public ItemLocationList(LocationTable locationTable) {
        this.locationTable = locationTable;

        init();
    }

    private void init() {
        setFitToWidth(true);
        setMinWidth(800);
        setStyle("-fx-background-color: transparent;");

        gp = new GridPane();
        gp.setMinWidth(getMinWidth() - 50);
        gp.setPadding(new Insets(10));

        gp.getStyleClass().add("white");
        gp.getStylesheets().add("smithsonian/merlin/gui/css/session.css");

        int[] locationIDs = DBController.getAllLocationIDs();
        int offset = DBController.getNumberOfSameLevels();
        Location[] tempLoc;
        Location[] locations = FileManager.loadLocations();
        allItems = FXCollections.observableArrayList();

        VBox locationBox = new VBox();
        HBox locLayout;
        Label locCaption;
        Label locValue;
        tempLoc = DBController.getLocationFromID(locationIDs[0]);
        lists = new ListView[locationIDs.length];

        for (int i = 0; i < offset; i++) {
            if (tempLoc[i] != null) {
                locLayout = new HBox();
                locCaption = new Label(locations[i].getName().replace("###", ""));
                locCaption.setFont(Font.font(null, FontWeight.BOLD, 23));
                locCaption.setMinWidth(180);
                locValue = new Label(tempLoc[i].getValue());
                locValue.setFont(new Font(23));
                locValue.setMinWidth(170);
                locLayout.setSpacing(10);
                locLayout.getChildren().addAll(locCaption, locValue);
                locationBox.getChildren().add(locLayout);
            }
        }
        locationBox.setPadding(new Insets(5, 5, 50, 5));
        gp.add(locationBox, 0, 0, 2, 1);

        for (int i = 0; i < locationIDs.length; i++) {
            tempLoc = DBController.getLocationFromID(locationIDs[i]);
            locationBox = new VBox();
            locationBox.setMinWidth(350);
            for (int j = offset; j < tempLoc.length; j++) {
                if (tempLoc[j] != null) {
                    locLayout = new HBox();
                    locCaption = new Label(locations[j].getName().replace("###", ""));
                    locCaption.setFont(Font.font(null, FontWeight.BOLD, 19));
                    locCaption.setMinWidth(180);
                    locValue = new Label(tempLoc[j].getValue());
                    locValue.setFont(new Font(19));
                    locValue.setMinWidth(170);
                    locLayout.setSpacing(10);
                    locLayout.getChildren().addAll(locCaption, locValue);
                    locationBox.getChildren().add(locLayout);
                }
            }
            locationBox.setPadding(new Insets(5));
            locationBox.setBorder(new Border(new BorderStroke(Color.BLACK,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

            ObservableList<Item> items = DBController.getItemsInLocation(locationIDs[i]);
            lists[i] = new ListView<>(items);
            lists[i].setPadding(new Insets(5));
            lists[i].setMinWidth(400);
            lists[i].getSelectionModel().clearSelection();
            lists[i].setBorder(new Border(new BorderStroke(Color.BLACK,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            lists[i].setPadding(new Insets(0));
            lists[i].setPrefHeight(lists[i].getItems().size() * 36 + 2); // set height of table to items' height

            final int j = i;
            lists[i].setOnMousePressed(event -> {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    locationTable.displayLocation(lists[j].getSelectionModel().getSelectedItem());
                }
            });

            lists[i].setCellFactory(column -> new ListCell<Item>() {
                protected void updateItem(Item item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item.getBarcode());
                        if (item.getTimeStamp() != null) {
                            if (getIndex() % 2 == 0)
                                setStyle("-fx-background-color: #99CC00; -fx-text-fill: white;");
                            else
                                setStyle("-fx-background-color: #669966; -fx-text-fill: white;");
                        }
                        if (item == lastItem && lastItem.getTimeStamp() != null) {
                            setStyle("-fx-background-color: #0096C9; -fx-text-fill: white;"); // highlight current item
                        }
                    }
                }
            });

            lists[i].getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                // System.out.println("Selected item: " + newValue);
                for (int x = 0; x < lists.length; x++) {
                    lists[x].getSelectionModel().clearSelection();
                    // System.out.println(x);
                }
            });

            allItems.addAll(items);
            gp.add(locationBox, 0, i + 1);
            gp.add(lists[i], 1, i + 1);
            gp.setVgap(20);
        }

        setContent(gp);
    }

    public Item undo() {
        lastItem.setTimeStamp(null);
        lists[lastItemPos.getTable()].refresh();
        checkForCompletion();
        return lastItem;
    }

    public Item getItemByBarcode(String barcode) {
        Item result = null;
        for (int i = 0; i < allItems.size(); i++) {
            if (allItems.get(i).getBarcode().equals(barcode)) {
                result = allItems.get(i);
                break;
            }
        }
        return result;
    }

    public Position checkInItem(String barcode, String timeStamp) {
        Item item = getItemByBarcode(barcode);
        if (item.getTimeStamp() == null) {
            item.setTimeStamp(timeStamp);
            return checkInItem(item);
        } else { // item is already checked in
            return null;
        }
    }

    private void checkForCompletion() {
        // check for completion of single table
        for (int i = 0; i < lists.length; i++) {
            ObservableList<Item> items = lists[i].getItems();
            if (allReturned(items)) {
                gp.getChildren().get(2 * i + 1).setStyle("-fx-background-color: #99CC00;");
                // System.out.println(i);
            } else {
                gp.getChildren().get(2 * i + 1).setStyle("-fx-background-color: white;");
            }
        }
    }

    private boolean allReturned(ObservableList<Item> items) {
        boolean allReturned = true;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getTimeStamp() == null) {
                allReturned = false;
                break;
            }
        }
        return allReturned;
    }

    public Position checkInItem(Item item) {
        Position result = null;
        for (int i = 0; i < lists.length; i++) {
            ObservableList<Item> items = lists[i].getItems();
            for (int j = 0; j < items.size(); j++)
                if (items.get(j) == item) {
                    locationTable.displayLocation(item);
                    lastItem = item; // for undo
                    result = new Position(i, j);
                    lastItemPos = result;
                    checkForCompletion();
                    centerLastItem();
                }
            lists[i].refresh();
        }
        return result;
    }

    private void centerLastItem() {
        Node table = lists[lastItemPos.getTable()];
        int row = lastItemPos.getRow();
        double h = getContent().getBoundsInLocal().getHeight();
        double y = table.getBoundsInParent().getMinY() + (row * 36);
        double v = getViewportBounds().getHeight();
        setVvalue(getVmax() * ((y - 0.5 * v) / (h - v)));
    }

    private void centerTable(Node table) {
        double h = getContent().getBoundsInLocal().getHeight();
        double y = table.getBoundsInParent().getMinY() + 340;
        double v = getViewportBounds().getHeight();
        setVvalue(getVmax() * ((y - 0.5 * v) / (h - v)));
    }

    public ObservableList<Item> getItems() {
        return allItems;
    }

    public ObservableList<Item> getCheckedOutItems() {
        List<Item> wrapList = new ArrayList<>();
        ObservableList<Item> result = FXCollections.observableList(wrapList);

        for (int i = 0; i < allItems.size(); i++) {
            if (allItems.get(i).getTimeStamp() == null)
                result.add(allItems.get(i));
        }

        return result;
    }
}
