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

import java.io.Serializable;

/**
 * Represento um tipo de dispositivo .
 * @author Ricardo JL Rufino
 * @date 04/09/2011 12:44:44
 */
public class DeviceCategory implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int code;
	private String description;
		
	public static DeviceCategory LAMP = new DeviceCategory(1, "Lâmpada");
	public static DeviceCategory FAN = new DeviceCategory(2, "Ventilador");
	public static DeviceCategory GENERIC = new DeviceCategory(3, "Genérico");
	public static DeviceCategory POWER_SOURCE = new DeviceCategory(4, "Tomada");
	
	public static DeviceCategory GENERIC_SENSOR = new DeviceCategory(50, "Sensor Genérico");
	public static DeviceCategory IR_SENSOR = new DeviceCategory(51, "Sensor Infra-Vermelho");


    public DeviceCategory(){

    }

	/**
	 * Create new DeviceCategory (or use static constants like #GENERIC)
	 * @param code - Device type code. MAX 127.
	 * @param description
	 */
	public DeviceCategory(int code, String description) {
		super();
		this.code = (byte) code;
		this.description = description;
	}


	public int getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}
	
}