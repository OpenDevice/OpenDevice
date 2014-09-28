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

package br.com.criativasoft.opendevice.wsrest.guice.config;

import br.com.criativasoft.opendevice.connection.ServerConnection;
import com.google.inject.Provider;


/**
 * Guice provider to Inject ServerConnection into Rest Resources. </br>
 * TODO: Deve ser configurado em um filtro ou interceptor, usando #setConnection.
 * @autor Ricardo JL Rufino
 * @date 05/07/14.
 */
public class ConnectionGuiceProvider implements Provider<ServerConnection> {

    private static ThreadLocal<ServerConnection> connectionThreadLocal = new ThreadLocal<ServerConnection>();

    public static void setConnection(ServerConnection connection){
        connectionThreadLocal.set(connection);
    }

    @Override
    public ServerConnection get() {
        return connectionThreadLocal.get();
    }
}
