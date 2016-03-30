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

package br.com.criativasoft.opendevice.core.listener.impl;

import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceListener;

import java.util.Collection;

/**
 * It maps the value of the current device to the ID of another registered device. </br>
 * Example, it is possible to map the values read by a radio frequency sensor to the corresponding devices.
 * @author Ricardo JL Rufino
 * @date 09/01/16
 */
public class DeviceMappingListener implements DeviceListener {

    public static final int MAP_VALUE_TO_ID = 1;
    public static final int MAP_VALUE_TO_VALUE = 2;

    private int mappingType;

    public DeviceMappingListener(int mappingType) {
        this.mappingType = mappingType;
    }

    @Override
    public void onDeviceChanged(Device device) {
        Collection<Device> devices = BaseDeviceManager.getInstance().getDevices();

        for (Device found : devices) {

            if( device.getUid()!= found.getUid()){

                if(mappingType == MAP_VALUE_TO_ID && found.getUid() == device.getValue()){
                    found.setValue(found.getValue()+1);
                }else if(mappingType == MAP_VALUE_TO_VALUE && found.getValue() == device.getValue()){
                    found.notifyListeners();
                }

            }

        }

    }
}
