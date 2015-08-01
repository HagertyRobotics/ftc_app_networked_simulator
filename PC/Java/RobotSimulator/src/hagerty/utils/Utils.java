package hagerty.utils;

import hagerty.simulator.modules.*;
import javafx.collections.ObservableList;

import javax.xml.bind.*;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public final class Utils {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static String bufferToHexString(byte[] data, int start, int length) {
        int myStop;
        StringBuilder sb = new StringBuilder();
        //byte [] subArray = Arrays.copyOfRange(a, 4, 6);
        myStop = (length > data.length) ? data.length : length;
        for (int i=start; i < start+myStop; i++) {
            sb.append(String.format("%02x ", data[i]));
        }
        return sb.toString();
    }

    /**
     * Saves the current controller data to the specified file.
     *
     * @param file
     * @param brickList
     */
    public static void saveBrickDataToFile(File file, List<BrickSimulator> brickList) throws Exception {
        JAXBContext context = JAXBContext.newInstance(BrickListWrapper.class, LegacyBrickSimulator.class, MotorBrickSimulator.class, ServoBrickSimulator.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        // Wrapping our controller data.
        BrickListWrapper wrapper = new BrickListWrapper();
        wrapper.setBricks(brickList);

        // Marshalling and saving XML to the file.
        m.marshal(wrapper, file);

        // Save the file path to the registry.
        // setBrickFilePath(file);
    }

    /**
     * Sets the file path of the currently loaded file. The path is persisted in
     * the OS specific registry.
     *
     * @param file        the file or null to remove the path
     * @param preferences
     */
    public static void setBrickFilePath(File file, Preferences preferences) {
        // Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        if (file != null) {
            preferences.put("filePath", file.getPath());
            // Update the stage title.
            // primaryStage.setTitle("FTC Simulator - " + file.getName());
        } else {
            preferences.remove("filePath");

            // Update the stage title.
            // primaryStage.setTitle("FTC Simulator");
        }
    }

    /**
     * Loads controller data from the specified file. The current controller data will
     * be replaced.
     *
     * @param file
     */
    public static void loadBrickDataFromFile(File file, ObservableList<BrickSimulator> brickList) throws Exception {
        JAXBContext context = JAXBContext
                .newInstance(BrickListWrapper.class, LegacyBrickSimulator.class, MotorBrickSimulator.class, ServoBrickSimulator.class);
        Unmarshaller um = context.createUnmarshaller();
        try {
            // Reading XML from the file and unmarshalling to a Wrapper class that just contains a single List
            // of Simulator objects.
            BrickListWrapper wrapper = (BrickListWrapper) um.unmarshal(file);

            // Add the list from the Wrapper class into the local member variable "brickList"
            // This list will be returned from the method getBrickData()
            brickList.clear();
            brickList.addAll(wrapper.getBricks());

            // For the LegacyBrickSimulator objects, since we couldn't get the marshaler to handle the list of small
            // SimData objects(6), we need to create the objects by hand using the returned list of portTypes.
            brickList.forEach(BrickSimulator::fixupUnMarshaling);

            // Save the file path to the registry.
            // setBrickFilePath(file, Preferences.userNodeForPackage(MainApp.class));

        } catch (UnmarshalException e) {
            System.err.println("UnmarshalExcemption: " + e);
        } catch (JAXBException e) {
            System.err.println("UnmarshalExcemption: " + e);
        }
    }

    /**
     * Returns the controller file preference, i.e. the file that was last opened.
     * The preference is read from the OS specific registry. If no such
     * preference can be found, null is returned.
     *
     * @param preferences
     * @return
     */
    public static File getBrickFilePath(Preferences preferences) {
        String filePath = preferences.get("filePath", null);
        if (filePath != null) {
            return new File(filePath);
        } else {
            return null;
        }
    }
}
