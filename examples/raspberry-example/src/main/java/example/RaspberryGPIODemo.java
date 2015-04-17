package example;

import javax.swing.JFrame;

import br.com.criativasoft.opendevice.core.SimpleDeviceManager;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.raspberry.RaspberryGPIO;


/**
 *
 */
public class RaspberryGPIODemo extends SimpleDeviceManager {

    Device led = new Device(1, DeviceType.DIGITAL).gpio(1); // same as RaspiPin.GPIO_01

    public static void main(String[] args) throws Exception {
        new RaspberryGPIODemo();
    }

    public RaspberryGPIODemo() throws Exception {

        connect(new RaspberryGPIO());
        
        JFrame tobi = new JFrame("youwillneverfigurethisout");
        tobi.setVisible(true);
        tobi.setTitle("Xbatz GUI Example");
        tobi.setSize(400, 200);
        tobi.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        while(isConnected()){
            led.on();
            delay(500);
            led.off();
            delay(500);
        }
    }
}
