
import br.com.criativasoft.opendevice.connection.TCPConnection;
import br.com.criativasoft.opendevice.core.SimpleDeviceManager;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceType;

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

        Device led = new Device(1, DeviceType.DIGITAL);

        // setup connection with arduino/hardware
        addOutput(Connections.out.usb()); // Connect to first USB port available
        // addOutput(Connections.out.tcp("192.168.0.204:8081"));

        connect();

        addDevice(led);

        while(true){
            led.on();
            delay(500);
            led.off();
            delay(500);
        }
    }

}
