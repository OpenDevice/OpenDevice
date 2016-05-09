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

import br.com.criativasoft.opendevice.connection.discovery.NetworkDeviceInfo;

/**
 * Discovery Network Devices response. Used with: DiscoveryServiceImpl
 * @author Ricardo JL Rufino
 * @date 04/11/15
 */
public class DiscoveryResponse extends ResponseCommand implements ExtendedCommand {

    private NetworkDeviceInfo deviceInfo;

    public DiscoveryResponse() {
        super(CommandType.DISCOVERY_RESPONSE);
    }

    @Override
    public void deserializeExtraData(String extradata) {
        String[] data = extradata.split(Command.DELIMITER);
        deviceInfo = new NetworkDeviceInfo();
        deviceInfo.setName(data[0]);
        deviceInfo.setType(Integer.parseInt(data[1]));
        deviceInfo.setDeviceLength(Integer.parseInt(data[2]));
        deviceInfo.setPort(Integer.parseInt(data[3]));
    }

    public NetworkDeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public String serializeExtraData() {
        throw new IllegalStateException("Not implemented !"); // not required
    }
}
