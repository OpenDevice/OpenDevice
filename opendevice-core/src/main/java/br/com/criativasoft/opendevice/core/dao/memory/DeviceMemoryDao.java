/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.core.dao.memory;

import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.model.Device;

import java.util.LinkedList;
import java.util.List;

/**
 * TODO: PENDING DOC
 *
 * @autor Ricardo JL Rufino
 * @date 27/08/14.
 */
public class DeviceMemoryDao implements DeviceDao {

    private List<Device> devices = new LinkedList<Device>();

    @Override
    public Device getById(long id) {
        for (Device device : devices){
            if(device.getId() == id){
                return device;
            }
        }

        return null;
    }

    public Device getByUID(long uid) {
        for (Device device : devices){
            if(device.getUid() == uid){
                return device;
            }
        }

        return null;
    }

    private boolean exist(Device device){
        if(device.getId() > 0){
            Device find = getById(device.getId());
            return find != null;
        }else{
            for (Device find : devices){
                if(device == find || device.equals(find)){ // check if same instance.
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public void persist(Device entity) {

        if(!exist(entity)){
            devices.add(entity);
        }

    }

    @Override
    public void update(Device entity) {
        // nothing
    }

    @Override
    public void delete(Device entity) {

        boolean removed = devices.remove(entity); // remove by instance

        // remove by ID.
        if(!removed && entity.getId() > 0){
            Device find = getById(entity.getId());
            if(find != null){
                devices.remove(find);
            }
        }

    }

    @Override
    public void refresh(Device entity) {
        // nothing
    }

    @Override
    public List<Device> listAll() {
        return devices;
    }
}
