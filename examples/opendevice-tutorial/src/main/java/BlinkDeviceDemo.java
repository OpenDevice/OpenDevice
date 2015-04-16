import br.com.criativasoft.opendevice.core.SimpleDeviceManager;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.webclient.WebSocketClientConnection;

/**
 *
 * Tutorial: https://opendevice.atlassian.net/wiki/display/DOC/A.+First+Steps+with+OpenDevice
 * For arduino/energia use: opendevice-hardware-libraries/arduino/OpenDevice/examples/UsbConnection
 * For arduino(with bluetooth): opendevice-hardware-libraries/arduino/OpenDevice/examples/BluetoothConnection
 *
 * @author Ricardo JL Rufino
 * @date 17/02/2014
 */
public class BlinkDeviceDemo extends SimpleDeviceManager {

    public static void main(String[] args) throws Exception {
        new BlinkDeviceDemo();
    }

    public BlinkDeviceDemo() throws Exception {

        Device led = new Device(1, Device.DIGITAL);

        connect(Connections.out.bluetooth("00:11:06:14:04:57"));

        while(true){
            led.on();
            delay(500);
            led.off();
            delay(500);
        }
    }

}


