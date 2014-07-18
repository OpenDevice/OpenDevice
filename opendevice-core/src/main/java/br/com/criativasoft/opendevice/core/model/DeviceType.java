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
 * Represento um tipo de dispositivo (IDe) .
 * @author Ricardo JL Rufino
 * @date 04/09/2011 12:44:44
 */
public enum DeviceType implements EnumCode {
	
	ANALOG(1, "Analógico"), 
	DIGITAL(2, "Digital");

	private int code;
	private String description;
		
	/**
	 *  	
	 * @param code - Device type code. MAX 127.
	 * @param description
	 */
	private DeviceType(int code, String description) {
		this.code = (byte) code;
		this.description = description;
	}


	public int getCode() {
		return code;
	}

	public String getDescription() {
		return description;
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