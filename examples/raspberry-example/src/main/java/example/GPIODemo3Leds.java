package example;

import br.com.criativasoft.opendevice.core.SimpleDeviceManager;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.raspberry.RaspberryGPIO;


/**
 *
 */
public class GPIODemo3Leds extends SimpleDeviceManager {

    Device led1 = new Device(1, DeviceType.DIGITAL).gpio(0); // same as RaspiPin.GPIO_01
    Device led2 = new Device(2, DeviceType.DIGITAL).gpio(1); // same as RaspiPin.GPIO_01
    Device led3 = new Device(3, DeviceType.DIGITAL).gpio(3); // same as RaspiPin.GPIO_01

    public static void main(String[] args) throws Exception {
        new GPIODemo3Leds();
    }

    public GPIODemo3Leds() throws Exception {

        connect(new RaspberryGPIO());
        
        while(isConnected()){
            for (int i = 1; i <= 3; i++) {
                Device led = findDeviceByUID(i);
                led.on();
                delay(100);
                led.off();
                delay(100);

            }


        }
    }
}
