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

package br.com.criativasoft.opendevice.core.filter;

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Limits the number of commands received to make concurrency control  or limit the amount of data.
 * This filter checks the last time the device was changed and compares with the interval parameter.
 * @author Ricardo JL Rufino on 12/10/14.
 */
public class FixedReadIntervalFilter implements CommandFilter{

    private static final Logger log = LoggerFactory.getLogger(FixedReadIntervalFilter.class);

    private long interval;

    private DeviceManager manager;

    /**
     * @param interval - interval in milliseconds
     * @param manager
     */
    public FixedReadIntervalFilter(long interval, DeviceManager manager) {
        this.interval = interval;
        this.manager = manager;
    }

    @Override
    public boolean filter(Command command, DeviceConnection connection) {

        if (DeviceCommand.isCompatible(command)) {

            DeviceCommand deviceCommand = (DeviceCommand) command;

            int deviceID = deviceCommand.getDeviceID();

            Device device = manager.findDeviceByUID(deviceID);

            if(device != null){
                long lastUpdate = device.getLastUpdate();

                if(lastUpdate <= 0) return true; // never chaged.

                // Ignore...
                if( (System.currentTimeMillis() - lastUpdate) < interval ){
                    return false;
                }
            }

        }

        return true;

    }
}
