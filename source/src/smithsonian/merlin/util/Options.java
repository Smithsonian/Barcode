package smithsonian.merlin.util;

import org.ini4j.Wini;

import java.io.File;

/**
 * Created by albesmn on 8/24/2016.
 */
public class Options {

    public static String currentLayout;
    public static String shared_folder_path;
    public static String museum;

    public static void loadOptions() {
        try {
            Wini ini = new Wini(new File("res/options.ini"));
            museum = ini.get("variables", "museum");
            currentLayout = ini.get("variables", "layout_name");
            shared_folder_path = ini.get("variables", "shared_folder_path");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveOptions() {
        try {
            Wini ini = new Wini(new File("res/options.ini"));
            ini.put("variables", "museum", museum);
            ini.put("variables", "layout_name", currentLayout);
            ini.put("variables", "shared_folder_path", shared_folder_path);
            ini.store();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
