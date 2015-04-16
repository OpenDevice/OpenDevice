package br.com.criativasoft.opendevice.core.model;

import br.com.criativasoft.opendevice.core.command.GPIO;

/**
 * Sensors are basically the same as devices.
 * @author Ricardo JL Rufino
 * @date 06/09/14.
 * @see br.com.criativasoft.opendevice.core.model.Device
 */
public class Sensor extends Device {

    /**
     * Create new Sensor with type  {@link DeviceType#DIGITAL}
     * @param uid Must match with 'id' configured in the physical module
     */
    public Sensor(int uid) {
        super(uid);
        setCategory(DeviceCategory.GENERIC_SENSOR);
    }

    /**
     * Create new Sensor
     * @param uid Must match with 'id' configured in the physical module
     * @param type Use a of constants: {@link DeviceType#DIGITAL} , {@link DeviceType#ANALOG}
     */
    public Sensor(int uid, DeviceType type) {
        super(uid, type);
        setCategory(DeviceCategory.GENERIC_SENSOR);
    }

    /**
     * Create new Device
     * @param name Logical name of device
     * @param uid Must match with 'id' configured in the physical module
     * @param type Use a of constants: {@link DeviceType#DIGITAL} , {@link DeviceType#ANALOG}
     */
    public Sensor(int uid, String name, DeviceType type) {
        this(uid, name, type, DeviceCategory.GENERIC_SENSOR);
    }

    /**
     * Create new Sensor
     * @param uid Must match with 'id' configured in the physical module
     * @param type Use a of constants: {@link DeviceType#DIGITAL} , {@link DeviceType#ANALOG}
     * @param category Does not influence the communication logic, only the GUIs
     */
    public Sensor(int uid, DeviceType type, DeviceCategory category) {
        super(uid, null, type, category);
    }

    /**
     * Create new Sensor
     * @param uid Must match with 'id' configured in the physical module
     * @param name Logical name of device
     * @param type Use a of constants: {@link DeviceType#DIGITAL} , {@link DeviceType#ANALOG}
     * @param category Does not influence the communication logic, only the GUIs
     */
    public Sensor(int uid, String name, DeviceType type, DeviceCategory category) {
        super(uid, name, type, category);
    }

    /**
     * Configure GPIO for this device. <br/>
     * This type of configuration is ideal for devices like the Raspberry.<br/>
     * Or when it is used to save the settings in the EPROM of low processing power devices
     * @param pin
     * @return
     */
    public Device gpio(int pin, GPIO.InputMode inputMode){
        this.gpio = new GpioInfo(pin, inputMode);
        return this;
    }

}
