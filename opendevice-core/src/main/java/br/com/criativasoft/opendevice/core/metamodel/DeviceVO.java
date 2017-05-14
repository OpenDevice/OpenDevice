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

import br.com.criativasoft.opendevice.core.model.*;
import br.com.criativasoft.opendevice.core.model.test.ActionDef;
import br.com.criativasoft.opendevice.core.model.test.GenericCategory;
import br.com.criativasoft.opendevice.core.model.test.GenericDevice;
import br.com.criativasoft.opendevice.core.model.test.Property;

import java.util.*;

public class DeviceVO {
	
	private int id; // Mapped to UID on Device.class
	private String name;
	private String title;
	private int type;
	private int category;
	private long value;
	private int parentID;
    private boolean sensor=false;
	
	private long lastUpdate;
	private Date dateCreated;

	private List<String> actions = new LinkedList<String>();
    private List<Integer> devices = new LinkedList<Integer>();

	private Map<String, Object> properties = new HashMap<String, Object>();

	public DeviceVO() {

	}
	
	public DeviceVO(Device device) {
		this(device.getUid(), device.getName(), device.getType().getCode(),
				(device.getCategory() == null ? DeviceCategory.GENERIC.getCode() : device.getCategory().getCode()),
		     device.getValue(), device.getLastUpdate(), device.getDateCreated());

        this.title = device.getTitle();

        if(device instanceof Sensor){
            setSensor(true);
        }

		if(device instanceof PhysicalDevice){
            Board board = ((PhysicalDevice) device).getBoard();
            if(board != null) setParentID(board.getUid());
		}

		if(device instanceof Board){
			Set<PhysicalDevice> devices = ((Board) device).getDevices();
            for (Device current : devices) {
                this.devices.add(current.getUid());
            }
        }

		// FIXME: remove later
		if(device instanceof GenericDevice){

			GenericDevice generic = (GenericDevice) device;
			GenericCategory category = generic.getCategory();

			for (ActionDef actionDef : category.getActions()) {
				actions.add(actionDef.getName());
			}

			for (Property property : generic.getProperties()) {
				properties.put(property.getName(), property.getValue());
			}

		}
	}

    public DeviceVO(int id, String name, DeviceType type, DeviceCategory category, long value) {
        this(id, name, type.getCode(), category.getCode(), value);
    }

    public DeviceVO(int id, String name, int type, int category, long value) {
        this(id, name, type, category, value, 0, null);
    }

	public DeviceVO(int id, String name, int type, int category, long value,long lastUpdate, Date dateCreated) {
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
	public long getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setParentID(int parentID) {
		this.parentID = parentID;
	}

	public int getParentID() {
		return parentID;
	}

    public void setDevices(List<Integer> devices) {
        this.devices = devices;
    }

    public List<Integer> getDevices() {
        return devices;
    }

    public void setSensor(boolean sensor) {
        this.sensor = sensor;
    }

    public boolean isSensor() {
        return sensor;
    }

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setActions(List<String> actions) {
		this.actions = actions;
	}

	public List<String> getActions() {
		return actions;
	}
}
