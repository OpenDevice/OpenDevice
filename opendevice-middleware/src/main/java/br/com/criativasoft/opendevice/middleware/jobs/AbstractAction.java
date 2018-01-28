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

package br.com.criativasoft.opendevice.middleware.jobs;

import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.middleware.model.actions.ActionSpec;

/**
 *
 * @author Ricardo JL Rufino
 * @date 04/11/16
 */
public abstract class AbstractAction<T extends ActionSpec> {

    protected T spec;

    protected Device targetDevice; // DEVICE

    public void setSpec(T spec) {
        this.spec = spec;
    }

    public T getSpec() {
        return spec;
    }

    public void setTargetDevice(Device targetDevice) {
        this.targetDevice = targetDevice;
    }

    public Device getTargetDevice() {
        return targetDevice;
    }

    public abstract void execute() throws ActionException;

    protected String replaceDeviceVariables(String text){
        if(targetDevice != null){

            if(targetDevice.getType() == DeviceType.DIGITAL){
                text = text.replaceAll("\\$VALUE", (targetDevice.getValue() == 0 ? "OFF" : "ON"));
            }else{
                text = text.replaceAll("\\$VALUE", ""+targetDevice.getValue());
            }
            text = text.replaceAll("\\$DEVICE_NAME", ""+targetDevice.getName());
            text = text.replaceAll("\\$DEVICE_TITLE", ""+targetDevice.getTitle());
        }
        return text;
    }

}
