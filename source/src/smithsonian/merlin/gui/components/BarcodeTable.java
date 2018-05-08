package smithsonian.merlin.gui.components;

import smithsonian.merlin.util.Item;
import smithsonian.merlin.util.Location;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;


/**
 * Created by albesmn on 7/25/2016.
 */
public class BarcodeTable extends TableView<Item> {

    TableColumn barcodes;

    public BarcodeTable() {
        init();
    }

    private void init() {
        barcodes = new TableColumn("Items");

        // replace default placeholder
        Label ph = new Label("Please scan in the location of the item if given");
        ph.setFont(new Font(20));
        setPlaceholder(ph);

        // size columns equally
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // add stylesheet to the table
        getStylesheets().add("smithsonian/merlin/gui/css/table.css");

        barcodes.setCellValueFactory(
                new PropertyValueFactory<Item, String>("barcode")
        );

        getColumns().setAll(barcodes);

        // process double clicking on table entry
        setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                showData(getSelectionModel().getSelectedItem());
            }
        });


        setOnKeyPressed(ke -> {
            /*
            if (ke.getCode() == KeyCode.ENTER && getSelectionModel().getSelectedItem() != null)
                showData(getSelectionModel().getSelectedItem());
            */

            if (ke.getCode() == KeyCode.DELETE && getSelectionModel().getSelectedItem() != null)
                getItems().remove(getSelectionModel().getSelectedItem());
        });
    }

    public void addItem(Item item) {
        getItems().add(item);
        getSelectionModel().select(getItems().size() - 1);
        scrollTo(getItems().size() - 1);

        barcodes.setText("Items - total: " + getItems().size());

        // debug
        // showData(item);
    }

    private void showData(Item item) {
        Location[] loc = item.getLocation();
        System.out.println("Item: " + getSelectionModel().getSelectedItem().getBarcode() + ", checkedIn: " + getSelectionModel().getSelectedItem().getTimeStamp());
        for (int i = 0; i < loc.length; i++) {
            if (loc[i] != null) {
                System.out.println(loc[i].getName() + ": " + loc[i].getValue());
            }
        }
        System.out.println();
    }

    /*
    test code

    HashMap hm = getSelectionModel().getSelectedItem().getLocations();
    final Stage dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    VBox dialogVbox = new VBox();

    // Get a set of the entries
    Set set = hm.entrySet();
    // Get an iterator
    Iterator i = set.iterator();
    // Display elements
    while(i.hasNext()) {
        HBox hb = new HBox(2);
        Map.Entry me = (Map.Entry)i.next();
        hb.getChildren().addAll(new Text(me.getKey().toString()), new Text(me.getValue().toString()));

        System.out.print(me.getKey() + ": ");
        System.out.println(me.getValue());
        dialogVbox.getChildren().add(hb);
    }

    Scene dialogScene = new Scene(dialogVbox, 200, 100);
    dialog.setScene(dialogScene);
    dialog.show();

     */
}
