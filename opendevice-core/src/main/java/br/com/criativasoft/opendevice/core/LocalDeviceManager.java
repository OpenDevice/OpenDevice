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

package br.com.criativasoft.opendevice.core;

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.dao.memory.DeviceMemoryDao;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * TODO: PENDING DOC
 *
 * @author Ricardo JL Rufino
 * @date 24/08/14.
 */
public class LocalDeviceManager extends BaseDeviceManager {

    protected static final Logger log = LoggerFactory.getLogger(LocalDeviceManager.class);

    private String applicationID = OpenDeviceConfig.LOCAL_APP_ID;

    private Set<Device> runtimeDevices = new LinkedHashSet<Device>();

    public void setApplicationID(String applicationID) {
        TenantProvider.setCurrentID(applicationID);
        this.applicationID = applicationID;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public LocalDeviceManager(){
        super();
        setDeviceDao(new DeviceMemoryDao());
    }

    @Override
    public void addInput(DeviceConnection connection) {
        if(connection.getApplicationID() == null) {
            connection.setApplicationID(this.applicationID);
        }
        super.addInput(connection);
    }

    @Override
    public void addOutput(DeviceConnection connection) {
        if(connection.getApplicationID() == null) {
            connection.setApplicationID(this.applicationID);
        }
        super.addOutput(connection);
    }

    @Override
    public void send(Command command) throws IOException {
        if(command.getApplicationID() == null){
            command.setApplicationID(getApplicationID());
        }
        super.send(command);
    }

    @Override
    public Collection<Device> getDevices() {

        Set<Device> devices = new LinkedHashSet<Device>();
        Collection<Device> dblist = super.getDevices();

        devices.addAll(runtimeDevices);
        devices.addAll(dblist); // TODO: this must be enabled (comentado devido a duplicidade nos testes)


        return devices;
    }


    @Override
    public void addDevice(Device device) {
        super.addDevice(device);
        runtimeDevices.add(device);
    }

    @Override
    public Device findDeviceByUID(int deviceUID) {

        if(!runtimeDevices.isEmpty()){
            for (Device runtimeDevice : runtimeDevices) {
                if(runtimeDevice.getUid() == deviceUID){
                    return runtimeDevice;
                }
            }
        }

        return super.findDeviceByUID(deviceUID);
    }

    @Override
    public DeviceDao getValidDeviceDao() {

        if(TenantProvider.getCurrentID() == null){
            TenantProvider.setCurrentID(getApplicationID());
        }

        // Check if tenant is valid.
        if(TenantProvider.getCurrentID() != null && getConfig().isSupportTenants() && OpenDeviceConfig.LOCAL_APP_ID.equals(TenantProvider.getCurrentID())){
            throw new IllegalStateException("In Multi-Tenant support don't allow '*' in applicationID !");
        }

        return super.getValidDeviceDao();
    }
}
