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

package br.com.criativasoft.opendevice.connection.discovery;

import java.io.IOException;
import java.util.Set;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 05/11/15
 */
public interface DiscoveryService {

    void listen();

    Set<NetworkDeviceInfo> scan(long timeout, String deviceName) throws IOException;

    void scan(long timeout, String deviceName, DiscoveryListener listener);

    void stop();
}
