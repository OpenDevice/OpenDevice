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

package br.com.criativasoft.opendevice.wsrest;

import br.com.criativasoft.opendevice.connection.IWSServerConnection;
import br.com.criativasoft.opendevice.restapi.DeviceRest;
import org.atmosphere.nettosphere.Config;


/**
 * WebSocket Server Connection
 * @author Ricardo JL Rufino
 * @date 11/06/2013
 */
public class WSServerConnection extends AbstractAtmosphereConnection implements IWSServerConnection {
	
    private static WSServerConnection INSTANCE;

    public static WSServerConnection getInstance() {
        return INSTANCE;
    }

    public WSServerConnection() {
        this(-1);
    }

	public WSServerConnection(int port) {
		super(port);
        INSTANCE = this;
	}

    @Override
    protected void configure(Config.Builder conf) {
        conf.resource(DeviceRest.class);
        conf.resource(DeviceConnectionResource.class);

        conf.initParam("org.atmosphere.websocket.messageContentType", "application/json");
        conf.initParam("org.atmosphere.websocket.messageMethod", "POST");

    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
