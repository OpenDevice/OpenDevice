package br.com.criativasoft.opendevice.core.connection;

import br.com.criativasoft.opendevice.connection.*;
import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.command.CommandStreamReader;
import br.com.criativasoft.opendevice.core.command.CommandStreamSerializer;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Factory for Output Connections
 *
 * @author Ricardo JL Rufino
 * @date 06/09/14.
 */
public class OutputConnections {

    /**
     * Create a USB connection with first serial port available
     */
    public <T extends DeviceConnection> T  usb(){
        return usb(null);
    }

    /**
     * Create a USB connection.
     * @param port - The serial port like: "COM3", "/dev/ttyUSB0", "/dev/ttyACM0"
     */
    public <T extends DeviceConnection> T  usb(String port){
        IUsbConnection connection = load(IUsbConnection.class);
        if(connection != null) connection.setConnectionURI(port);
        return (T) connection;
    }

    /**
     * Create a bluetooth connection with first device available. <br/>
     * Do not forget that you must pair with the PC first.
     */
    public <T extends DeviceConnection> T  bluetooth(){
        return bluetooth(null);
    }


    /**
     * Create a bluetooth connection. <br/>
     * Do not forget that you must pair with the PC first.
     * @param uri bluetooth address:
     */
    public <T extends DeviceConnection> T  bluetooth(String uri){
        IBluetoothConnection connection = load(IBluetoothConnection.class);
        if(connection != null) connection.setConnectionURI(uri);
        return (T) connection;
    }

    public <T extends DeviceConnection> T tcp(String address){
        ITcpConnection connection = load(ITcpConnection.class);
        if(connection != null){
            BaseDeviceManager deviceManager = (BaseDeviceManager) BaseDeviceManager.getInstance();
            if(deviceManager != null){
                connection.setDiscoveryService(deviceManager.getDiscoveryService());
            }
            connection.setConnectionURI(address);
        }
        return (T) connection;
    }

    public <T extends DeviceConnection> T  websocket(String address){
        IWSConnection connection = load(IWSConnection.class);
        if(connection != null) connection.setConnectionURI(address);
        return (T) connection;
    }


    private <T> T load(Class<T> klass){

        try{
            Class.forName("java.util.ServiceLoader");
        }catch(ClassNotFoundException ex){
            throw new RuntimeException("This java version don't support dynamic loading (ServiceLoader), you need use direct class ex: new BluetoothConnection(addr)");
        }

        // lockup....
        ServiceLoader<T> service = ServiceLoader.load(klass);

        Iterator<T> iterator = service.iterator();

        if(iterator.hasNext()){
            T connection = iterator.next();
            if(connection instanceof StreamConnection){
                StreamConnection conn = (StreamConnection) connection;
                conn.setSerializer(new CommandStreamSerializer()); // for de/serialization..
                conn.setStreamReader(new CommandStreamReader());   // for reading streams
            }
            return connection;
        }

        throw new RuntimeException("Provider for connection class: " + klass.getSimpleName() + ", not found ! (TIP: Add dependency)");
    }
}
