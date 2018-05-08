package smithsonian.merlin.util;

/**
 * Created by albesmn on 7/26/2016.
 */
public class Item {

    private String barcode;
    private String timeStamp;
    private Location[] location;

    public Item(String barcode, Location[] location, String timeStamp) {
        this.barcode = barcode;
        this.location = location;
        this.timeStamp = timeStamp;
    }

    public Item(String barcode, Location[] location) {
        this(barcode, location, null);
    }

    public Item(String barcode) {
        this(barcode, null, null);
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String toString() {
        return barcode;
    }

    public Location[] getLocation() {
        return location;
    }

    public void setLocation(Location[] location) {
        this.location = location;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
