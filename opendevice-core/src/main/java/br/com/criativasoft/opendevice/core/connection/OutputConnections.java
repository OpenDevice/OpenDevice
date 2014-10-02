package br.com.criativasoft.opendevice.core.connection;

import br.com.criativasoft.opendevice.connection.*;
import br.com.criativasoft.opendevice.core.command.CommandStreamReader;
import br.com.criativasoft.opendevice.core.command.CommandStreamSerializer;
import br.com.criativasoft.opendevice.core.model.Device;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Factory for Output Connections
 *
 * @autor Ricardo JL Rufino
 * @date 06/09/14.
 */
public class OutputConnections {

    /**
     * Create a USB connection with first serial port available
     * @return
     */
    public IUsbConnection usb(){
        return usb(null);
    }

    /**
     * Create a USB connection.
     * @param port - The serial port like: "COM3", "/dev/ttyUSB0", "/dev/ttyACM0"
     * @return
     */
    public IUsbConnection usb(String port){
        IUsbConnection connection = load(IUsbConnection.class);
        if(connection != null) connection.setConnectionURI(port);
        return connection;
    }

    /**
     * Create a bluetooth connection with first device available. <br/>
     * Do not forget that you must pair with the PC first.
     * @return
     */
    public IBluetoothConnection bluetooth(){
        return bluetooth(null);
    }


    /**
     * Create a bluetooth connection. <br/>
     * Do not forget that you must pair with the PC first.
     * @param uri bluetooth address:
     * @return
     */
    public IBluetoothConnection bluetooth(String uri){
        IBluetoothConnection connection = load(IBluetoothConnection.class);
        if(connection != null) connection.setConnectionURI(uri);
        return connection;
    }

    public DeviceConnection tcp(String address){
        ITcpConnection connection = load(ITcpConnection.class);
        if(connection != null) connection.setConnectionURI(address);
        return connection;
    }


    private <T> T load(Class<T> klass){

        // lonkup....
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
