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

package br.com.criativasoft.opendevice.core.command;

import br.com.criativasoft.opendevice.core.model.Device;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Send IDs to save on physical side.
 * The ids must be in order of received;
 */
public class SyncDevicesIdCommand extends Command implements ExtendedCommand {

    private static final long serialVersionUID = -2155798878419286601L;


    private Collection<Device> devices = new LinkedList<Device>();

    public SyncDevicesIdCommand(Collection<Device> devices) {
        super(CommandType.SYNC_DEVICES_ID);

        this.devices.addAll(devices);
    }


    public Collection<Device> getDevices() {
        return devices;
    }


    @Override
    public void deserializeExtraData(String extradata) {

        throw new IllegalStateException("Not implemented ");

    }

    @Override
    public String serializeExtraData() {

        StringBuilder sb = new StringBuilder();
        Iterator<Device> it = devices.iterator();

        sb.append(devices.size());
        sb.append(DELIMITER);

        while (it.hasNext()) {
            Device device = it.next();
            sb.append(device.getUid());
            if (it.hasNext()) sb.append(DELIMITER);

        }
        return sb.toString();
    }

}
