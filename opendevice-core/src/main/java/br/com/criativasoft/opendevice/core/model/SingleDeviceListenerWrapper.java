/*
 *
 *  * ******************************************************************************
 *  *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  *  All rights reserved. This program and the accompanying materials
 *  *  are made available under the terms of the Eclipse Public License v1.0
 *  *  which accompanies this distribution, and is available at
 *  *  http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  *  Contributors:
 *  *  Ricardo JL Rufino - Initial API and Implementation
 *  * *****************************************************************************
 *
 */

package br.com.criativasoft.opendevice.core.model;

/**
 * Created by ricardo on 23/09/14.
 */
public class SingleDeviceListenerWrapper implements DeviceListener {

    private int deviceID;
    private DeviceListener listener;

    public SingleDeviceListenerWrapper(int deviceID, DeviceListener listener) {
        this.deviceID = deviceID;
        this.listener = listener;
    }

    @Override
    public void onDeviceChanged(Device device) {

        if(deviceID == device.getUid()){
            if(listener != null) listener.onDeviceChanged(device);
        }

    }
}
