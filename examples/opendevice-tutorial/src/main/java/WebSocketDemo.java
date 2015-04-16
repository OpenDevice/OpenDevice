import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.IWSServerConnection;
import br.com.criativasoft.opendevice.connection.discovery.DiscoveryService;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.SimpleDeviceManager;
import br.com.criativasoft.opendevice.core.command.GetDevicesRequest;
import br.com.criativasoft.opendevice.core.command.GetDevicesResponse;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.core.model.Sensor;

import java.io.File;
import java.io.IOException;


/**
 * Run using Maven: mvn compile exec:java -Dexec.mainClass=WebSocketDemo
 *
 * Access the URL in the browser: http://localhost:8181<br/>

 * @author Ricardo JL Rufino
 * @date 17/08/2014
 */
public class WebSocketDemo extends SimpleDeviceManager {

    public static void main(String[] args) throws Exception {
        new WebSocketDemo();
    }


    public WebSocketDemo() throws Exception {


        // setup connection with arduino/hardware
        addOutput(Connections.out.usb()); // Connect to first USB port available
        addOutput(Connections.out.bluetooth("00:13:03:14:19:07"));

        // Configure a Websocket interface for receiving commands over HTTP
        IWSServerConnection server = Connections.in.websocket(8181);
        // Static WebResources
        String current = System.getProperty("user.dir");
        System.out.println("Current Directory: " + current);
        server.addWebResource( current + "/src/main/resources/webapp");
        server.addWebResource( current + "/target/classes/webapp"); //  running exec:java

        addInput(server);

        connect(); // Connects all configured connections

        // Enable discovery service  (Allows clients automatically find that server
        DiscoveryService.listen(8181);
    }


}