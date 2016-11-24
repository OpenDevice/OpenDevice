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

import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.metamodel.DeviceHistoryQuery;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceCategory;
import br.com.criativasoft.opendevice.core.model.DeviceHistory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/**
 * TODO: PENDING DOC
 *
 * @author Ricardo JL Rufino
 * @date 27/08/14.
 */
public class DeviceDaoMemory implements DeviceDao {

    private Map<String, List<Device>> deviceMap = new HashMap<String, List<Device>>();

    private List<Device> getCurrentDevices(){

        return deviceMap.get(TenantProvider.getCurrentID());

    }

    @Override
    public DeviceCategory getCategoryByCode(int code) {
        return BaseDeviceManager.getInstance().getCategory(code);
    }

    @Override
    public Device getById(long id) {

        List<Device> devices = getCurrentDevices();

        if(devices == null) return null;

        for (Device device : devices){
            if(device.getId() == id){
                return device;
            }
        }

        return null;
    }

    public Device getByUID(int uid) {

        List<Device> devices = getCurrentDevices();

        if(devices == null) return null;

        for (Device device : devices){
            if(device.getUid() == uid){
                return device;
            }
        }

        return null;
    }

    @Override
    public int getNextUID() {
        if(deviceMap.isEmpty()) return 1;

        int max = 0;

        List<Device> devices = getCurrentDevices();

        for (Device device : devices) {
            if(device.getUid() > max) max = device.getUid();
        }

        return max + 1;
    }

    private boolean exist(Device device){
        if(device.getId() > 0){
            Device find = getById(device.getId());
            return find != null;
        }else{
            List<Device> devices = getCurrentDevices();
            if(devices == null) return false;

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
        List<Device> devices = getCurrentDevices();

        if(!exist(entity)){

            if(devices == null){
                devices = new LinkedList<Device>();
                deviceMap.put(TenantProvider.getCurrentID(), devices);
            }

            devices.add(entity);
        }

    }

    @Override
    public void deleteHistory(Device device) {

    }

    @Override
    public Device update(Device entity) {
        // nothing
        return null;
    }

    @Override
    public void delete(Device entity) {

        List<Device> devices = getCurrentDevices();

        if(devices == null || devices.isEmpty()) return;

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
        List<Device> devices = getCurrentDevices();
        if(devices == null) return new ArrayList<Device>();
        return devices;
    }

    @Override
    public List<DeviceHistory> getDeviceHistory(DeviceHistoryQuery query) {
        throw new NotImplementedException();
    }

    @Override
    public void persistHistory(DeviceHistory history) {
        //
    }
}
