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

package br.com.criativasoft.opendevice.connection;

import java.util.Collection;

/**
 * Component that manages multiple connections
 * @author Ricardo JL Rufino on 05/10/14.
 */
public interface ConnectionManager {

    public Collection<DeviceConnection> getConnections();

    public DeviceConnection findConnection(String uid);

    public void addConnection(DeviceConnection connection);

    public void removeConnection(DeviceConnection connection);
}
