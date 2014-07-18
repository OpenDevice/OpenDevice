/*
 * ******************************************************************************
 *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.atemospherews.guice.config;

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import com.google.inject.Provider;


/**
 * Guice provider to Inject DeviceConnection into Rest Resources. </br>
 * TODO: Deve ser configurado em um filtro ou interceptor, usando #setConnection.
 * @autor Ricardo JL Rufino
 * @date 05/07/14.
 */
public class ConnectionGuiceProvider implements Provider<DeviceConnection> {

    private static ThreadLocal<DeviceConnection> connectionThreadLocal = new ThreadLocal<DeviceConnection>();

    public static void setConnection(DeviceConnection connection){
        connectionThreadLocal.set(connection);
    }

    @Override
    public DeviceConnection get() {
        return connectionThreadLocal.get();
    }
}
