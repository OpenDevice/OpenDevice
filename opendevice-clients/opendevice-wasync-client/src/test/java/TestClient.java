import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.webclient.WebSocketClientConnection;

import java.io.IOException;

/**
 * TODO: PENDING DOC
 *
 * @autor Ricardo JL Rufino
 * @date 11/07/14.
 */
public class TestClient {

    public static void main(String[] args) throws ConnectionException, InterruptedException {

        WebSocketClientConnection connection = new WebSocketClientConnection("http://localhost:8181/device/connection/fake-client-123-123");

        connection.addListener(new ConnectionListener() {
            @Override
            public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
                if(status == ConnectionStatus.CONNECTED){
                    try {
                        connection.send(new DeviceCommand(CommandType.ON_OFF, 1 , 1));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onMessageReceived(Message message, DeviceConnection connection) {

            }
        });

        connection.connect();


        while (connection.isConnected()){
            Thread.sleep(1000);
        }

    }
}
