package smithsonian.merlin.util;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.krysalis.barcode4j.BarcodeGenerator;
import org.krysalis.barcode4j.BarcodeUtil;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;

/**
 * Created by albesmn on 7/25/2016.
 */
public class BarcodeManager {

    /**
     * Generates a barcode with the given paramteres and saves it to the PC as a .png
     *
     * @param text     Given text to transform into barcode
     * @param type     Type of the barcode, supported: codabar, code39, code128, datamatrix, fourstate, int2of5, pdf417, postnet, upcean
     * @param readable Defines if human-readable caption should be printed
     * @return The path of the generated barcode image
     */
    public static boolean generateBarcode(String text, String type, boolean readable) {
        String path = "res/barcodes/" + text + ".png";
        boolean generated = false;
        if (!new File(path).exists()) {
            try {
                // generate and load config
                DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
                generateBarcodeCfg(type, readable);
                Configuration cfg = builder.buildFromFile(new File("res/barcodes/barcodecfg.xml"));

                // generate barcode .png
                BarcodeGenerator gen = BarcodeUtil.getInstance().createBarcodeGenerator(cfg);
                OutputStream out = new java.io.FileOutputStream(new File(path));
                BitmapCanvasProvider provider = new BitmapCanvasProvider(
                        out, "image/x-png", 300, BufferedImage.TYPE_BYTE_GRAY, true, 0);
                gen.generateBarcode(provider, text);
                provider.finish();
                out.close();
                generated = true;

                // debug
                System.out.println("Barcode img created: " + path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return generated;
    }


    /**
     * Generates the barcode config with the given parameters and saves it to the PC
     *
     * @param type     Type of the barcode
     * @param readable Defines if human-readable caption should be printed
     */
    private static void generateBarcodeCfg(String type, boolean readable) {
        try {
            File file = new File("res/barcodes/barcodecfg.xml");

            String read = "none";
            if (readable) read = "bottom";

            String content = "<barcode><" + type + ">" +
                    "<human-readable-placement>" + read + "</human-readable-placement>" +
                    "<shape>force-square</shape>" +
                    "</" + type + ">" + "</barcode>";
            file.createNewFile();

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
