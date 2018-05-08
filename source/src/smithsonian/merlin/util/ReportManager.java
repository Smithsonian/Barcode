package smithsonian.merlin.util;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.Row;
import com.google.common.io.Files;
import javafx.collections.ObservableList;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.*;
import java.io.File;

/**
 * Created by albesmn on 7/25/2016.
 */
public class ReportManager {

    public static void savePDF(Session session) {
        try {
            ObservableList<Item> items = session.getItems();
            boolean itemsOut, itemsIn;
            itemsIn = false;
            itemsOut = false;

            for (Item item : items){
                if (item.getTimeStamp() != null) itemsIn = true;
                else itemsOut = true;
            }

            //Initialize Document
            PDDocument doc = new PDDocument();
            PDPage page = new PDPage();

            //Create a landscape page
            page.setMediaBox(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
            doc.addPage(page);

            //Initialize table
            float margin = 10;
            float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
            float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
            float yStart = yStartNewPage;
            float bottomMargin = 0;

            if (itemsOut) {
                //Create the data
                BaseTable table = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true,
                        true);
                //Create Header row
                Row<PDPage> headerRow = table.createRow(15f);
                Location[] locations = FileManager.loadLocations();
                Cell<PDPage> cell = headerRow.createCell(22, "CheckedOut");
                cell.setFont(PDType1Font.HELVETICA_BOLD);
                cell.setFillColor(Color.LIGHT_GRAY);
                cell.setTextColor(Color.BLACK);
                for (int j = 0; j < locations.length; j++) {
                    cell = headerRow.createCell(78 / locations.length, locations[j].getName().replace("###", ""));
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFillColor(Color.LIGHT_GRAY);
                    cell.setTextColor(Color.BLACK);
                }
                table.addHeaderRow(headerRow);

                for (Item item : items) {
                    if (item.getTimeStamp() == null) {
                        Row<PDPage> row = table.createRow(10f);
                        row.createCell(22, item.getBarcode());
                        Location[] temp = item.getLocation();
                        for (int i = 0; i < temp.length; i++) {
                            if (temp[i] == null) temp[i] = new Location(locations[i].getName(), "---");
                            row.createCell(78 / locations.length, item.getLocation()[i].getValue());
                        }
                    }
                }
                table.draw();
            }

            if (itemsIn) {
                BaseTable table = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true,
                        true);
                if (itemsOut) { // start fresh page if there is another table
                    // Page 2
                    PDPage page2 = new PDPage();

                    //Create a landscape page
                    page2.setMediaBox(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
                    doc.addPage(page2);

                    table = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page2, true,
                            true);
                }
                //Create Header row
                Row<PDPage> headerRow = table.createRow(15f);
                Location[] locations = FileManager.loadLocations();
                Cell<PDPage> cell = headerRow.createCell(22, "CheckedIn");
                cell.setFont(PDType1Font.HELVETICA_BOLD);
                cell.setFillColor(Color.LIGHT_GRAY);
                cell.setTextColor(Color.BLACK);
                for (int j = 0; j < locations.length; j++) {
                    cell = headerRow.createCell(78 / locations.length, locations[j].getName().replace("###", ""));
                    cell.setFont(PDType1Font.HELVETICA_BOLD);
                    cell.setFillColor(Color.LIGHT_GRAY);
                    cell.setTextColor(Color.BLACK);
                }
                table.addHeaderRow(headerRow);

                for (Item item : items) {
                    if (item.getTimeStamp() != null) {
                        Row<PDPage> row = table.createRow(10f);
                        row.createCell(22, item.getBarcode());
                        Location[] temp = item.getLocation();
                        for (int i = 0; i < temp.length; i++) {
                            if (temp[i] == null) temp[i] = new Location(locations[i].getName(), "---");
                            row.createCell(78 / locations.length, item.getLocation()[i].getValue());
                        }
                    }
                }
                table.draw();
            }

            String timeStamp = session.getTimeStamp();
            String cartNumber = session.getCart();

            // declare all attributes
            String date = timeStamp.replace('.', '-').replace('/', '-');
            String outputName = "CART" + cartNumber + "_" + date + ".pdf";
            String outputFolderName = "CART" + cartNumber + "_" + date + "/";
            String folder = Options.shared_folder_path;
            String museum = Options.museum.toLowerCase();

            String finalPath = "/" + museum + "/" + session.getStatus() + "/" + outputFolderName + outputName;
            String localPath = "res/sessions" + finalPath;
            String sharedPath = folder + finalPath;

            File sharedFile = new File(sharedPath);
            Files.createParentDirs(sharedFile);
            File localFile = new File(localPath);
            Files.createParentDirs(localFile);
            doc.save(sharedFile);
            doc.close();
            Files.copy(sharedFile, localFile);
            // System.out.println("File saved at : " + sharedFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
    public static void savePDF(Session session, ObservableList<Item> items) {
        try {
            //Initialize Document
            PDDocument doc = new PDDocument();
            PDPage page = new PDPage();
            //Create a landscape page
            page.setMediaBox(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
            doc.addPage(page);
            //Initialize table
            float margin = 10;
            float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
            float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
            float yStart = yStartNewPage;
            float bottomMargin = 0;

            //Create the data

            Location[] locations = FileManager.loadLocations();
            for (int j = 0; j < locations.length; j++) {
                locations[j].setName(locations[j].getName().replace("###", ""));
            }

            List<List> data = new ArrayList();
            data.add(new ArrayList<>(
                    Arrays.asList("Barcode", locations[0].getName(), locations[1].getName(), locations[2].getName(), locations[3].getName(),
                            locations[4].getName(), locations[5].getName())));
            for (int i = 0; i < items.size(); i++) {
                Location[] temp = items.get(i).getLocation();
                for (int j = 0; j < temp.length; j++) {
                    if (temp[j] == null)
                        temp[j].setValue("---");
                }
                data.add(new ArrayList<>(
                        Arrays.asList(items.get(i).getBarcode(), temp[0].getValue(), temp[1].getValue(), temp[2].getValue(), temp[3].getValue(),
                                temp[4].getValue(), temp[5].getValue())));
            }


            BaseTable dataTable = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true,
                    true);
            DataTable t = new DataTable(dataTable, page);
            t.addListToTable(data, DataTable.HASHEADER);
            dataTable.draw();
            String path = "res/reports/CART" + session.getCart() + "_" + session.getTimeStamp() + ".pdf";
            File file = new File(path);
            Files.createParentDirs(file);
            doc.save(file);
            doc.close();
            // System.out.println("File saved at : " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
}
