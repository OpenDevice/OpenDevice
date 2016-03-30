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

package opendevice.io.tests;

import br.com.criativasoft.opendevice.connection.discovery.NetworkDeviceInfo;
import br.com.criativasoft.opendevice.core.discovery.DiscoveryServiceImpl;

import java.io.IOException;
import java.util.Set;

/**
 * @author Ricardo JL Rufino
 * @date 08/11/15
 */
public class TestDiscovery {

    public static void main(String[] args) throws IOException {

        Set<NetworkDeviceInfo> networkDeviceInfos = new DiscoveryServiceImpl().scan(5000, null);

        for (NetworkDeviceInfo networkDeviceInfo : networkDeviceInfos) {
            System.out.println(networkDeviceInfo);
        }

    }
}
