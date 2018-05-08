package smithsonian.merlin.util;

/**
 * Created by albesmn on 8/2/2016.
 */
public class Location {

    private String name;
    private String value;
    private int level;

    public Location(String name, String value, int level) {
        this.name = name;
        this.value = value;
        this.level = level;
    }


    public Location(String name, int level) {
        this(name, null, level);
    }

    public Location(String name, String value) {
        this(name, value, 0);
    }

    public Location(String name) {
        this(name, null, 0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String toString() {
        return name + " (" + level + ")";
    }
}
