/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.core.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 12/10/16
 */
@Entity
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="uid")
public class Board extends Device {

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, fetch= FetchType.LAZY)
    @JsonIdentityReference(alwaysAsId = true)
    private Set<PhysicalDevice> devices = new LinkedHashSet<PhysicalDevice>();

    public Board() {
    }

    public Board(int uid) {
        super(uid);
    }

    public Board(int uid, DeviceType type) {
        super(uid, type);
    }

    public Board(int uid, String name, DeviceType type) {
        super(uid, name, type);
    }

    public Board(int uid, String name, DeviceType type, DeviceCategory category) {
        super(uid, name, type, category);
    }

    public Board(int uid, String name, DeviceType type, DeviceCategory category, long value) {
        super(uid, name, type, category, value);
    }

    public Set<PhysicalDevice> getDevices() {
        return devices;
    }

    public void setDevices(Set<PhysicalDevice> devices) {
        this.devices = devices;
    }

    public void setDevices(List<Device> devices) {
        for (Device device : devices) {
            if(device instanceof PhysicalDevice) {
                this.devices.add((PhysicalDevice) device);
            }
        }
    }

    public void addDevice(Device device) {
        if(device instanceof PhysicalDevice) {
            ((PhysicalDevice) device).setBoard(this);
            this.devices.add((PhysicalDevice) device);
        }
    }

    @Override
    public void setValue(long value) {
        // ignore.
    }

    @Override
    public long getValue() {
        return 1;
    }

    @Override
    public String toString() {
        return "Board[UID:"+getUid()+", Name:"+getName()+", Value:"+getValue()+", Type:" + getType()+"]";
    }

}
