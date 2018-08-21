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

package br.com.criativasoft.opendevice.core;

import br.com.criativasoft.opendevice.core.model.Device;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Ricardo JL Rufino
 * @date 12/10/16
 */
public class LocalTenantContext implements TenantContext {

    private String tenantID;

    // Map <Name, Device>
    protected Map<String, Device> devices;

    public LocalTenantContext(String id) {
        this.tenantID = id;
        devices = new ConcurrentHashMap<String, Device>();
    }

    @Override
    public void addDevice(Device device) {
        device.setManaged(true);
        devices.put(device.getName(), device);
    }

    @Override
    public void updateDevice(Device device) {
        addDevice(device);
    }

    @Override
    public void cleanUp() {

    }

    @Override
    public void removeDevice(Device device) {
        device.setManaged(false);
        devices.remove(device.getName());
    }

    @Override
    public Device getDeviceByUID(int uid) {

        if(uid <= 0) return null;

        for (Device device : devices.values()) {
            if(device.getUid() == uid) return device;
        }

        return null;
    }

    @Override
    public Device getDeviceByName(String name) {

        for (Device device : devices.values()) {
            if(device.getName() != null && device.getName().equals(name)) return device;
        }

        return null;
    }

    public Collection<Device> getDevices(){
        return devices.values();
    }

    public String getId() {
        return tenantID;
    }
}
