/*
 * ******************************************************************************
 *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.core.model;

import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.listener.OnDeviceChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Device is an abstraction of a physical device, which may be a lamp, socket, sensor, robot, or even a logical device. <br/>
 * These devices are managed and controlled by a hardware like Arduino, Raspberry and others (see list) or can be built <br/>
 * in an embedded own equipment, this is the proposal of the internet of things. <br/>
 * @author Ricardo JL Rufino
 * @date 04/09/2011 12:40:01
 */
public class Device implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(Device.class);

	private static final long serialVersionUID = 1L;
	
	public static final int VALUE_HIGH = 1;
	public static final int VALUE_LOW = 0;

    public static final int ON = 1;
    public static final int OFF = 0;

    public static final DeviceType ANALOG = DeviceType.ANALOG;
    public static final DeviceType DIGITAL = DeviceType.DIGITAL;
    public static final DeviceType NUMERIC = DeviceType.NUMERIC;
    public static final DeviceType CHARACTER = DeviceType.CHARACTER;

    private long id; // Database ID
	private int uid; // Logic level user ID.
	private String name;
	private DeviceType type;
	private DeviceCategory category = DeviceCategory.GENERIC;
	private long lastUpdate;
	private Date dateCreated;
    private String applicationID;
	
	private long value = VALUE_LOW;

    private volatile Set<OnDeviceChangeListener> listeners = new HashSet<OnDeviceChangeListener>();

    protected volatile GpioInfo gpio;

    public Device(){

    }

    /**
     * Create new Device with type  {@link DeviceType#DIGITAL}
     * @param uid Must match with 'id' configured in the physical module
     */
    public Device(int uid) {
        this(uid, null, DeviceType.DIGITAL, DeviceCategory.GENERIC);
    }

    /**
     * Create new Device
     * @param uid Must match with 'id' configured in the physical module
     * @param type Use a of constants: {@link DeviceType#DIGITAL} , {@link DeviceType#ANALOG}
     */
    public Device(int uid,DeviceType type) {
        this(uid, null, type, DeviceCategory.GENERIC);
    }

    /**
     * Create new Device
     * @param name Logical name of device
     * @param uid Must match with 'id' configured in the physical module
     * @param type Use a of constants: {@link DeviceType#DIGITAL} , {@link DeviceType#ANALOG}
     */
    public Device(int uid, String name, DeviceType type) {
        this(uid, name, type, DeviceCategory.GENERIC);
    }

    /**
     * Create new Device
     * @param uid Must match with 'id' configured in the physical module
     * @param name Logical name of device
     * @param type Use a of constants: {@link DeviceType#DIGITAL} , {@link DeviceType#ANALOG}
     * @param category Does not influence the communication logic, only the GUIs
     */
	public Device(int uid, String name, DeviceType type, DeviceCategory category) {
        this(uid, name, type, category, (type == Device.DIGITAL ? VALUE_LOW : -1));
	}


    /**
     * Create new Device
     * @param uid Must match with 'id' configured in the physical module
     * @param name Logical name of device
     * @param type Use a of constants: {@link DeviceType#DIGITAL} , {@link DeviceType#ANALOG}
     * @param category - Does not influence the communication logic, only the GUIs
     * @param value
     */
	public Device(int uid, String name, DeviceType type, DeviceCategory category, long value) {
		super();
		this.uid = uid;
        this.name = name;
        this.type = type;
        this.category = category;
        this.value = value;

        BaseDeviceManager manager = BaseDeviceManager.getInstance();
        if(manager != null && !manager.isTenantsEnabled()) manager.addDevice(this);
    }


    protected void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public int getUid() {
		return uid;
	}

    public void setUID(int uid) {
        this.uid = uid;
    }

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public DeviceType getType() {
		return type;
	}
	public void setType(DeviceType type) {
		this.type = type;
	}


    public void setValue(long value) {
        this.setValue(value, true);
    }

    /**
     *
     * @param value
     * @param sync - sync state with server
     */
	public void setValue(long value, boolean sync) {

        // fire the event 'onChange' every time a reading is taken
        if(type == NUMERIC || value != this.value){
            setLastUpdate(System.currentTimeMillis());
            this.value = value;
            notifyListeners(sync);
        }

	}

    /**
     * Check if the value is high
     * @return true if value > 0
     */
    public boolean isON(){
        return getValue() > 0;
    }

    /**
     * Check if the value is LOW
     * @return true if value > 0
     */
    public boolean isOFF(){
        return getValue() == 0;
    }

    /**
     * Set value 1(HIGH).
     * shorthand call to setValue(HIGH)
     */
    public void on(){
       this.setValue(Device.VALUE_HIGH);
    }

    /**
     * Set value 0 (LOW).
     * shorthand call to setValue(LOW)
     */
    public void off(){
        this.setValue(Device.VALUE_LOW);
    }
    
    public void toggle(){
        if(getType() == DeviceType.DIGITAL){
            if(isON()) off();
            else on();
        }
    }

    public long getValue() {
		return value;
	}
	
	public void setCategory(DeviceCategory category) {
		this.category = category;
	}

    public void setCategoryClass(Class<DeviceCategory> klass) {

        DeviceManager manager = BaseDeviceManager.getInstance();
        if(manager != null){
            this.category = manager.getCategory(klass);
        }

    }
	
	public DeviceCategory getCategory() {
		return category;
	}
	
	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	public long getLastUpdate() {
		return lastUpdate;
	}
	
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}

    public boolean addListener(OnDeviceChangeListener e) {
        return listeners.add(e);
    }

    public Set<OnDeviceChangeListener> getListeners() {
        return listeners;
    }

    public boolean onChange(OnDeviceChangeListener e) {
        return addListener(e);
    }

    /**
     * Notify All Listeners about received command.
     */
    public void notifyListeners(boolean sync) {

        DeviceManager manager = BaseDeviceManager.getInstance();
        if(manager != null) manager.notifyListeners(this, sync);
        else{
            if(log.isDebugEnabled()) log.debug("None DeviceManager registered for this device: " + this.toString());
        }

    }

    public String getApplicationID() {
        return applicationID;
    }

    public Device setApplicationID(String applicationID) {
        this.applicationID = applicationID;
        return this;
    }

    @Override
	public String toString() {
		return "Device[UID:"+uid+", Name:"+getName()+", Value:"+getValue()+", Type:" + getType()+"]";
	}

    /**
     * Configure GPIO for this device. <br/>
     * This type of configuration is ideal for devices like the Raspberry.<br/>
     * Or when it is used to save the settings in the EPROM of low processing power devices
     * @param pin
     * @return
     */
    public Device gpio(int pin){
        this.gpio = new GpioInfo(pin);
        return this;
    }

    public GpioInfo getGpio() {
        return gpio;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        if (uid != device.uid) return false;
        return !(name != null ? !name.equals(device.name) : device.name != null);

    }

    @Override
    public int hashCode() {
        int result = uid;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    /**
     * Create {@link br.com.criativasoft.opendevice.core.command.DeviceCommand} of type {@link CommandType#DIGITAL} with value HIGH
     * @param deviceID
     * @return
     */
    public static DeviceCommand ON(int deviceID){
        return new DeviceCommand(CommandType.DIGITAL, deviceID, Device.VALUE_HIGH);
    }

    /**
     * Create {@link br.com.criativasoft.opendevice.core.command.DeviceCommand} of type {@link CommandType#DIGITAL} with value LOW
     * @param deviceID
     * @return
     */
    public static DeviceCommand OFF(int deviceID){
        return new DeviceCommand(CommandType.DIGITAL, deviceID, Device.VALUE_LOW);
    }
}
