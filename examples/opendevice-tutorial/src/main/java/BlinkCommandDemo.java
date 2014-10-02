import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.connection.Connections;


/**
 * Tutorial: https://opendevice.atlassian.net/wiki/display/DOC/A.+First+Steps+with+OpenDevice
 * For arduino/energia use: opendevice-hardware-libraries/arduino/OpenDevice/examples/UsbConnection
 * For arduino(with bluetooth): opendevice-hardware-libraries/arduino/OpenDevice/examples/BluetoothConnection
 * @author Ricardo JL Rufino
 * @date 17/02/2014
 */
public class BlinkCommandDemo implements ConnectionListener {

    public BlinkCommandDemo() throws Exception {

        DeviceConnection conn = Connections.out.usb();
        conn.addListener(this);
        conn.connect();
        long delay = 500;

        while(conn.isConnected()) {
            conn.send(DeviceCommand.ON(1)); // '1' is Device ID not pin !
            Thread.sleep(delay);
            conn.send(DeviceCommand.OFF(1));
            Thread.sleep(delay);
        }

        System.out.println("TERMINATED !");
    }
	
	public static void main(String[] args) throws Exception {
        new BlinkCommandDemo();
	}


    // ------------------------------------------------------------
    // ------------- ConnectionListener Impl --------------------------

    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {
        String type = message.getClass().getSimpleName();
        System.out.println("onMessageReceived("+type+"): "+ message);
    }

    @Override
    public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
        System.out.println("connectionStateChanged :  " + status);
    }
}
