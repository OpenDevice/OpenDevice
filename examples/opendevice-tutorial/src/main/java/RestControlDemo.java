import br.com.criativasoft.opendevice.core.SimpleDeviceManager;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import br.com.criativasoft.opendevice.core.model.DeviceType;


/**
 * In this example we add a control interface via REST, allowing integration with any other programming language or other software. <br/>
 * Access the URL in the browser: http://localhost:8181/device/1/value/1   <br/>
 * Or open sample: opendevice-samples/src/main/webapp/rest-jquery.html  <br/>
 *
 * Tutorial: https://opendevice.atlassian.net/wiki/display/DOC/Step+2+-+Adding+REST
 * For arduino/energia use: opendevice-hardware-libraries/arduino/OpenDevice/examples/UsbConnection
 * For arduino(with bluetooth): opendevice-hardware-libraries/arduino/OpenDevice/examples/BluetoothConnection
 * @author Ricardo JL Rufino
 * @date 17/02/2014
 */
public class RestControlDemo extends SimpleDeviceManager implements DeviceListener {

    public static void main(String[] args) throws Exception {
        new RestControlDemo();
    }

    public RestControlDemo() throws Exception {

        Device led = new Device(1, DeviceType.DIGITAL);

        // setup connection with arduino/hardware
        addOutput(Connections.out.usb()); // Connect to first USB port available

        // Configure a Rest interface for receiving commands over HTTP
        addInput(Connections.in.rest(8181));

        addListener(this); // monitor changes on devices
        connect();

        addDevice(led);

    }

    // ------------- DeviceListener Impl --------------------------
    // ------------------------------------------------------------

    @Override
    public void onDeviceChanged(Device device) {
        System.out.println("DeviceChanged = " + device);
    }


}