package smithsonian.merlin.util;

import javafx.collections.ObservableList;

/**
 * Created by albesmn on 8/8/2016.
 */
public class Session {

    private String creator;
    private String cart;
    private String timeStamp;
    private String status;
    private ObservableList<Item> items;

    public Session(String creator, String cart, String timeStamp, String status, ObservableList<Item> items) {
        this.creator = creator;
        this.cart = cart;
        this.timeStamp = timeStamp;
        this.status = status;
        this.items = items;
    }

    public Session(String creator, String cart, String timeStamp, ObservableList<Item> items) {
        this(creator, cart, timeStamp, null, items);
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCart() {
        return cart;
    }

    public void setCart(String cart) {
        this.cart = cart;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ObservableList<Item> getItems() {
        return items;
    }

    public void setItems(ObservableList<Item> items) {
        this.items = items;
    }
}
