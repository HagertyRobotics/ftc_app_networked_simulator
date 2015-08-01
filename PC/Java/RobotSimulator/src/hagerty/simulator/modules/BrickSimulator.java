package hagerty.simulator.modules;

import hagerty.simulator.legacy.data.SimData;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Model class for a Motor Controller, called "Brick" to avoid confusion with "Controller"
 *
 * @author Hagerty High
 */
@XmlAccessorType(XmlAccessType.NONE)
public abstract class BrickSimulator implements Runnable {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected final StringProperty alias;
    protected final StringProperty serial;
    protected IntegerProperty mPort;

    byte[] mReceiveData = new byte[1024];
    byte[] mSendData = new byte[1024];

    public BrickSimulator() {
        this.alias = new SimpleStringProperty("");
        mPort = new SimpleIntegerProperty(0);
        serial = new SimpleStringProperty("");
    }


    @Override
    public void run() {
    	byte[] packet;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                packet = receivePacketFromPhone();
            	handleIncomingPacket(packet, packet.length, false);
            }
            // Catch unhandled exceptions and cleanup
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, e.toString());
    	}
    }

    protected abstract byte[] receivePacketFromPhone();

	public abstract void handleIncomingPacket(byte[] data, int length, boolean wait);

    public abstract String getName();

	public abstract void setupDebugGuiVbox(VBox vbox);

	public abstract void populateDebugGuiVbox();

	public abstract void fixupUnMarshaling();

	public abstract void populateDetailsPane(Pane pane);

	public abstract SimData findSimDataName(String name);

    //---------------------------------------------------------------
    //
    // Getters and Setters for the marshaler and demarshaler
    //
    public String getAlias() {
        return alias.get();
    }

    @XmlElement
    public void setAlias(String alias) {
        this.alias.set(alias);
    }

    public Integer getPort() {
    	return mPort.get();
    }

    @XmlElement
    public void setPort(Integer port) {
    	mPort.set(port);
    }

    public String getSerial() {
        return serial.get();
    }

    @XmlElement
    public void setSerial(String serial) {
        this.serial.set(serial);
    }

    public StringProperty aliasProperty() {
        return alias;
    }

    public StringProperty serialProperty() {
        return serial;
    }



}

