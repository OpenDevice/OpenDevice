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

package br.com.criativasoft.opendevice.core.connection;

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.core.model.Device;

import java.util.Collection;

/**
 * Connections that are used in devices that have the integrated GPIO (like Raspberry)
 *
 * @author Ricardo JL Rufino on 22/10/14.
 */
public interface EmbeddedGPIO extends DeviceConnection {

    /**
     * Function used to link a device with this connection. </br>
     * Note that in some implementations of DeviceManager this function is called automatically
     * @param device
     */
    void attach(Device device);

    Collection<Device> getDevices();
}
