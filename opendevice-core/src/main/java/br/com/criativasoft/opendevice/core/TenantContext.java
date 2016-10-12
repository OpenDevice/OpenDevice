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

package br.com.criativasoft.opendevice.core;

import br.com.criativasoft.opendevice.core.model.Device;

import java.util.Collection;

/**
 * TenantContext acts as a kind of cache data for each client / application / tenant
 * @author Ricardo JL Rufino
 * @date 12/10/16
 */
public interface TenantContext {

    public Collection<Device> getDevices();

    public void addDevice(Device device);

    public Device getDeviceByUID(int uid);

    public String getId();
}
