import br.com.criativasoft.opendevice.connection.IWSServerConnection;
import br.com.criativasoft.opendevice.core.SimpleDeviceManager;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.core.model.Sensor;


/**
 *
 * Run using Maven using: mvn compile exec:java -Dexec.mainClass=WebSocketDemo
 * Access the URL in the browser: http://localhost:8181   <br/>
 * And access the URL in the another tab: http://localhost:8181/rest-jquery.html   <br/>
 *
 * https://opendevice.atlassian.net/wiki/display/DOC/Step+2+-+Adding+REST
 * For arduino/energia use: opendevice-hardware-libraries/arduino/OpenDevice/examples/UsbConnection
 * For arduino(with bluetooth): opendevice-hardware-libraries/arduino/OpenDevice/examples/BluetoothConnection
 * For energia/launchpad : examples/EnergiaLaunchPadBasic
 *
 * @author Ricardo JL Rufino
 * @date 17/08/2014
 */
public class WebSocketDemo extends SimpleDeviceManager {

    public static void main(String[] args) throws Exception {
        new WebSocketDemo();
    }

    public WebSocketDemo() throws Exception {

        setApplicationID("clientname-123456");

        // setup connection with arduino/hardware
        addOutput(Connections.out.usb()); // Connect to first USB port available
        //addOutput(Connections.out.bluetooth("001303141907"));

        // Configure a Websocket interface for receiving commands over HTTP
        IWSServerConnection server = Connections.in.websocket(8181);
        // Static WebResources
        String current = System.getProperty("user.dir");
        System.out.println("Current Directory: " + current);
        server.addWebResource( current + "/webapp");
        server.addWebResource( current + "/target/classes/webapp"); //  running exec:java

        addInput(server);
        connect(); // Connects all configured connections

        addDevice(new Device(1, "led 1", DeviceType.DIGITAL));
        addDevice(new Device(2, "led 2", DeviceType.DIGITAL));
        addDevice(new Device(3, "led 3", DeviceType.DIGITAL));
        addDevice(new Sensor(4, "btn 1", DeviceType.DIGITAL));
        addDevice(new Sensor(5, "btn 2", DeviceType.DIGITAL));

    }

}