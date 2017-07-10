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


import br.com.criativasoft.opendevice.core.metamodel.EnumCode;

/**
 * Represento um tipo de dispositivo  .
 * @author Ricardo JL Rufino
 * @date 04/09/2011 12:44:44
 */
public enum DeviceType implements EnumCode {

    DIGITAL(1, "Digital", PhysicalDevice.class), // TODO:CHANGE TO BYNARY
    ANALOG(2, "Analog", PhysicalDevice.class),
    ANALOG_SIGNED(3, "Analog+", PhysicalDevice.class),
    NUMERIC(4, "Numeric", PhysicalDevice.class), // Similar to ANALOG , but launches the event 'onChange' every time a reading is taken
    FLOAT2(5, "Float2", PhysicalDevice.class),
    FLOAT2_SIGNED(6, "Float+", PhysicalDevice.class),
    FLOAT4(7, "Float4", PhysicalDevice.class),
    CHARACTER(8, "Character", null),

    //
    BOARD(10, "Board", Board.class), // Hold Multiple Devices
    MANAGER(11, "MANAGER", null) // Middleware/Server
    ;

	private int code;
	private String description;
    private Class klass;
		
	/**
	 *  @param code - Device type code. MAX 127.
	 * @param description
     * @param klass
     */
	private DeviceType(int code, String description, Class klass) {
		this.code = (byte) code;
		this.description = description;
        this.klass = klass;
    }


	public int getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

    public Class getKlass() {
        return klass;
    }

    public static DeviceType getByCode(int code){
        DeviceType[] values = DeviceType.values();
        for (DeviceType type : values) {
            if(type.getCode() == code){
                return type;
            }
        }

        return null;
    }
	
}