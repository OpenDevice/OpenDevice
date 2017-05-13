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

    private String id;

    // Map <UID, Device>
    protected Map<Integer, Device> devices;

    public LocalTenantContext(String id) {
        this.id = id;
        devices = new ConcurrentHashMap<Integer, Device>();
    }

    @Override
    public void addDevice(Device device) {
        device.setManaged(true);
        devices.put(device.getUid(), device);
    }

    @Override
    public void updateDevice(Device device) {
        addDevice(device);
    }

    @Override
    public void removeDevice(Device device) {
        device.setManaged(false);
        devices.remove(device.getUid());
    }

    @Override
    public Device getDeviceByUID(int uid) {
        return devices.get(uid);
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
        return id;
    }
}
