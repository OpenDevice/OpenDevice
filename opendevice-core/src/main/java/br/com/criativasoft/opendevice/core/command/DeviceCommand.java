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

package br.com.criativasoft.opendevice.core.command;

import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceType;

public class DeviceCommand extends Command {

    public DeviceCommand(){
        super();
    }

    /**
     * It is a command used to control devices or indicating a command has been received. Is bound to a Device.
     * @param type
     * @param deviceID - Device UID ({@link br.com.criativasoft.opendevice.core.model.Device#getUid()})
     * @param value
     */
	public DeviceCommand(CommandType type, int deviceID, double value) {
		super(type);
		this.deviceID = deviceID;
		this.value = value;
	}

	private static final long serialVersionUID = 3448172706632529764L;
	
	private int deviceID;
	private double value;
	
	public int getDeviceID() {
		return deviceID;
	}
	
	public double getValue() {
		return value;
	}

    /**@see br.com.criativasoft.opendevice.core.command.CommandType#isDeviceCommand(CommandType) */
    public static boolean isCompatible(Command command){
        return CommandType.isDeviceCommand(command.getType());
    }

    /**@see br.com.criativasoft.opendevice.core.command.CommandType#isDeviceCommand(CommandType) */
	public static boolean isCompatible(CommandType type){
		return CommandType.isDeviceCommand(type);
	}

    public static boolean isCompatible(int type){
        return isCompatible(CommandType.getByCode(type));
    }

    public static DeviceCommand GPIO( int pin, long value){
        return new DeviceCommand(CommandType.GPIO_DIGITAL, pin, value);
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

    public static CommandType getCommandType(DeviceType deviceType){
        if (deviceType == DeviceType.DIGITAL) {
            return CommandType.DIGITAL;
        }else if (deviceType == DeviceType.ANALOG
                || deviceType == DeviceType.ANALOG_SIGNED
                || deviceType == DeviceType.FLOAT2
                || deviceType == DeviceType.FLOAT4 ||
                deviceType == DeviceType.FLOAT2_SIGNED) {
            return CommandType.ANALOG;
        }else if (deviceType == DeviceType.NUMERIC) {
            return CommandType.NUMERIC;
        }else{
            return null;
        }
    }

}

