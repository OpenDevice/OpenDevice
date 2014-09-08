package br.com.criativasoft.opendevice.core.connection;

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.IBluetoothConnection;
import br.com.criativasoft.opendevice.connection.ITcpConnection;
import br.com.criativasoft.opendevice.connection.IUsbConnection;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Factory for Output Connections
 *
 * @autor Ricardo JL Rufino
 * @date 06/09/14.
 */
public class OutputConnections {

    public IUsbConnection usb(){
        return usb(null);
    }

    public IUsbConnection usb(String port){
        IUsbConnection connection = load(IUsbConnection.class);
        if(connection != null) connection.setConnectionURI(port);
        return connection;
    }

    public IBluetoothConnection bluetooth(){
        return bluetooth(null);
    }


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
            return connection;
        }

        return null;
    }
}
