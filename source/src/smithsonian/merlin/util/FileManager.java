package smithsonian.merlin.util;

/**
 * Created by albesmn on 7/25/2016.
 */


import com.google.common.io.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

public class FileManager {

    public static void writeSessionToFile(Session session) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            String creator = session.getCreator();
            String timeStamp = session.getTimeStamp();
            String cartNumber = session.getCart();
            ObservableList<Item> items = session.getItems();

            // set root element
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("SESSION");
            doc.appendChild(rootElement);

            // save creator to root
            Attr attr = doc.createAttribute("creator");
            attr.setValue(creator);
            rootElement.setAttributeNode(attr);

            // save timestamp to root (Format: MM-dd-yyyy-HH-mm)
            attr = doc.createAttribute("timestamp");
            attr.setValue(timeStamp);
            rootElement.setAttributeNode(attr);

            // loop through items and add them to the DOM structure
            for (int i = 0; i < items.size(); i++) {
                Element item = doc.createElement("ITEM");

                // add barcode attribute
                attr = doc.createAttribute("barcode");
                attr.setValue(items.get(i).getBarcode());
                item.setAttributeNode(attr);

                // add checkedin attribute
                attr = doc.createAttribute("returnTime");
                String checkedIn = items.get(i).getTimeStamp();
                attr.setValue(checkedIn);
                item.setAttributeNode(attr);

                // add location tag
                rootElement.appendChild(item);
                Element location = doc.createElement("LOCATION");
                item.appendChild(location);

                Location[] loc = items.get(i).getLocation();
                for (int j = 0; j < loc.length; j++) {
                    if (loc[j] != null) {
                        attr = doc.createAttribute("level");
                        attr.setValue(Integer.toString(loc[j].getLevel()));

                        Element el = doc.createElement(loc[j].getName().replace("###", ""));
                        el.setAttributeNode(attr);
                        el.appendChild(doc.createTextNode(loc[j].getValue()));
                        location.appendChild(el); // with location tag: location, without: item
                    }
                }
            }

            // write items to a xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(doc);

            // declare all attributes
            String date = timeStamp.replace('.', '-').replace('/', '-');
            String outputName = "CART" + cartNumber + "_" + date + ".xml";
            String outputFolderName = "CART" + cartNumber + "_" + date + "/";
            String folder = Options.shared_folder_path;
            String museum = Options.museum.toLowerCase();

            String finalPath = "/" + museum + "/" + session.getStatus() + "/" + outputFolderName;

            String delNew = "/" + museum + "/new/" + outputFolderName;
            String delOngoing = "/" + museum + "/ongoing/" + outputFolderName;

            if (session.getStatus().equals("ongoing")) {
                // delete old files from local
                File old = new File("res/sessions" + delNew);
                if (old.exists()) deleteFileOrFolder(Paths.get("res/sessions" + delNew));

                // System.out.println(session.getStatus());

                // delete old files from shared
                old = new File(folder + delNew);
                if (old.exists()) deleteFileOrFolder(Paths.get(folder + delNew));
            } else if (session.getStatus().equals("finished")) {
                // delete old files from local
                File old = new File("res/sessions" + delNew);
                System.out.println(old.getAbsolutePath());
                if (old.exists()) deleteFileOrFolder(Paths.get("res/sessions" + delNew));
                old = new File("res/sessions" + delOngoing);
                if (old.exists()) deleteFileOrFolder(Paths.get("res/sessions" + delOngoing));

                // System.out.println(session.getStatus());

                // delete old files from shared
                old = new File(folder + delNew);
                if (old.exists()) deleteFileOrFolder(Paths.get(folder + delNew));
                old = new File(folder + delOngoing);
                if (old.exists()) deleteFileOrFolder(Paths.get(folder + delOngoing));
            }

            // create file
            // System.out.println(finalPath);
            String localPath = "res/sessions" + finalPath;
            String sharedPath = folder + finalPath;
            new File(localPath).mkdirs();
            new File(sharedPath).mkdirs();
            localPath += outputName;
            sharedPath += outputName;

            // debug
            System.out.println(localPath);
            System.out.println(sharedPath);

            File localFile = new File(localPath);
            File sharedFile = new File(sharedPath);

            StreamResult result = new StreamResult(localFile.getAbsolutePath());
            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);

            result = new StreamResult(sharedFile.getAbsolutePath());
            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFileOrFolder(final Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
            @Override public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return CONTINUE;
            }

            @Override public FileVisitResult visitFileFailed(final Path file, final IOException e) {
                return handleException(e);
            }

            private FileVisitResult handleException(final IOException e) {
                e.printStackTrace(); // replace with more robust error handling
                return TERMINATE;
            }

            @Override public FileVisitResult postVisitDirectory(final Path dir, final IOException e)
                    throws IOException {
                if(e!=null)return handleException(e);
                Files.delete(dir);
                return CONTINUE;
            }
        });
    };

    public static Session loadSessionFromFile(String filePath) {
        List<Item> items = new ArrayList<>();
        ObservableList<Item> result = FXCollections.observableList(items);
        String timeStampOut = null;
        String cartNumber = null;
        try {
            // load and open session file
            File fXmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            // populate session
            String name = fXmlFile.getName();
            String timestamp = name.substring(name.indexOf("_") + 1, name.length()).replace(".xml", "");
            String cart = name.substring(4, name.indexOf("_"));
            timeStampOut = timestamp; //
            cartNumber = cart; // set cart number

            // get all "item"-nodes from file
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("ITEM");

            // read item contents from xml file
            for (int i = 0; i < nList.getLength(); i++) {
                String barcode;
                Location[] location = new Location[6];
                String returnTime;
                Node nNode = nList.item(i);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    barcode = eElement.getAttribute("barcode"); // get barcode of the item
                    returnTime = eElement.getAttribute("returnTime"); // get return timestamp
                    if (returnTime.trim().isEmpty()) returnTime = null;

                    // get location tags and store them into a hashmap
                    NodeList locationNodes = eElement.getElementsByTagName("LOCATION").item(0).getChildNodes();
                    for (int j = 0; j < locationNodes.getLength(); j++) {
                        if (locationNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            Element temp = (Element) locationNodes.item(j);
                            int level = Integer.parseInt(temp.getAttribute("level"));
                            if (temp.getTextContent().trim().isEmpty()) // location is not filled out, set it to null
                                location[level] = null;
                            else
                                location[level] = new Location(temp.getNodeName(), temp.getTextContent(), level);

                            // debug
                            // System.out.println(barcode + "\n" + temp.getNodeName() + ": " + temp.getTextContent() + " (" + level + ")");
                        }
                    }
                    // add new item with barcode and location to the item list
                    result.add(new Item(barcode, location, returnTime));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Session(null, cartNumber, timeStampOut, result);
    }

    public static void saveLocations(String layoutName, Location[] locations) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // set root element
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("locations");
            doc.appendChild(rootElement);

            // loop through items and add them to the DOM structure
            for (int i = 0; i < locations.length; i++) {
                Element location = doc.createElement("location");

                // add location tag
                rootElement.appendChild(location);

                Element name = doc.createElement("name");
                name.appendChild(doc.createTextNode(locations[i].getName()));
                location.appendChild(name);

                Element level = doc.createElement("level");
                level.appendChild(doc.createTextNode(Integer.toString(locations[i].getLevel())));
                location.appendChild(level);
            }

            // write items to a xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(doc);

            // create file
            File file = new File("res/layouts/" + layoutName + ".xml");
            StreamResult result = new StreamResult(file.getAbsolutePath());

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

    public static Location[] loadLocations() {
        Location[] result = new Location[6];
        try {
            // load and open session file
            File fXmlFile = new File("res/layouts/" + Options.currentLayout + ".xml"); // TODO: take path
            if (!fXmlFile.exists()) {
                Options.currentLayout = "default";
                fXmlFile = new File("res/layouts/default.xml");
            }
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            // get all "item"-nodes from file
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("location");

            // read item contents from xml file
            for (int i = 0; i < nList.getLength(); i++) {
                String name;
                int level;
                Node nNode = nList.item(i);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    name = eElement.getElementsByTagName("name").item(0).getTextContent();
                    level = Integer.parseInt(eElement.getElementsByTagName("level").item(0).getTextContent());

                    result[i] = new Location(name, level);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Load the museums from the museums.txt in order to display them in a ComboBox
     *
     * @return a list of all museums
     */
    public static ObservableList<String> loadMuseums() {
        ObservableList<String> museums = FXCollections.observableArrayList();
        try {
            BufferedReader in = new BufferedReader(new FileReader("res/museums.txt"));

            String line;
            while ((line = in.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.startsWith("#"))
                    museums.add(line);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return museums.sorted();
    }

    public static void loadMuseumsFromSharedDrive(){
        String localPath = "res/museums.txt";
        String sharedPath = Options.shared_folder_path + "/00-dpo/museums.txt";

        File shared = new File(sharedPath);
        File local = new File(localPath);
        if (shared.exists()){
            try {
                com.google.common.io.Files.copy(shared, local);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Museums.txt copied from drive.");
        }
    }

    public static void loadDefaultLayoutFromSharedDrive(){
        String localPath = "res/layouts/default.xml";
        String sharedPath = Options.shared_folder_path + "/00-dpo/default.xml";

        File shared = new File(sharedPath);
        File local = new File(localPath);
        if (shared.exists()){
            try {
                com.google.common.io.Files.copy(shared, local);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Default.xml copied from drive.");
        }
    }
}
