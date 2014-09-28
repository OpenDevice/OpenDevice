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

package br.com.criativasoft.opendevice.core.metamodel;

import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceCategory;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.core.model.Sensor;

import java.util.Date;

public class DeviceVO {
	
	private int id; // Mapped to UID on Device.class
	private String name;
	private int type;
	private int category;
	private long value;
    private boolean sensor=false;
	
	private Date lastUpdate;
	private Date dateCreated;
	
	public DeviceVO() {

	}
	
	public DeviceVO(Device device) {
		this(device.getUid(), device.getName(), device.getType().getCode(), device.getCategory().getCode(), 
		     device.getValue(), device.getLastUpdate(), device.getDateCreated());

        if(device instanceof Sensor){
            setSensor(true);
        }
	}

    public DeviceVO(int id, String name, DeviceType type, DeviceCategory category, long value) {
        this(id, name, type.getCode(), category.getCode(), value);
    }

    public DeviceVO(int id, String name, int type, int category, long value) {
        this(id, name, type, category, value, null, null);
    }

	public DeviceVO(int id, String name, int type, int category, long value,Date lastUpdate, Date dateCreated) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.category = category;
		this.value = value;
		this.lastUpdate = lastUpdate;
		this.dateCreated = dateCreated;
	}


	public int getId() {
		return id;
	}
    public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getCategory() {
		return category;
	}
	public void setCategory(int category) {
		this.category = category;
	}
	public long getValue() {
		return value;
	}
	public void setValue(long value) {
		this.value = value;
	}
	public Date getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

    public void setSensor(boolean sensor) {
        this.sensor = sensor;
    }

    public boolean isSensor() {
        return sensor;
    }
}
