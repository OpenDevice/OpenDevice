package br.com.criativasoft.opendevice.core.connection;

import br.com.criativasoft.opendevice.connection.IRestServerConnection;
import br.com.criativasoft.opendevice.connection.ITcpServerConnection;
import br.com.criativasoft.opendevice.connection.IWSServerConnection;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Factory for Input Connections
 *
 * @author Ricardo JL Rufino
 * @date 06/09/14.
 */
public class InputContections {

    public IRestServerConnection rest(int port){
        IRestServerConnection connection = load(IRestServerConnection.class);
        if(connection != null) connection.setPort(port);
        return connection;
    }

    public IWSServerConnection websocket(int port){
        IWSServerConnection connection = load(IWSServerConnection.class);
        if(connection != null) connection.setPort(port);
        return connection;
    }

    public ITcpServerConnection tcp(int port){
        ITcpServerConnection connection = load(ITcpServerConnection.class);
        if(connection != null) connection.setPort(port);
        return connection;
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
            return iterator.next();
        }

        throw new RuntimeException("Provider for connection class: " + klass.getSimpleName() + ", not found ! (TIP: Add pom/jar dependency)");
    }


}
