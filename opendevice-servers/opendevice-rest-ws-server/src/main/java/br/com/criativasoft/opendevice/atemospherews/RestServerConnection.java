/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

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

package br.com.criativasoft.opendevice.atemospherews;

import br.com.criativasoft.opendevice.atemospherews.AbstractAtmosphereConnection;
import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.restapi.DeviceRest;
import org.atmosphere.cpr.AtmosphereInterceptor;
import org.atmosphere.nettosphere.Config;


/**
 * WebSocket Server Connection
 * @author Ricardo JL Rufino
 * @date 11/06/2013
 */
public class RestServerConnection extends AbstractAtmosphereConnection implements AtmosphereInterceptor, ConnectionListener {

	public RestServerConnection(int port) {
		super(port);
	}

    @Override
    protected void configure(Config.Builder conf) {
        conf.resource(DeviceRest.class);
    }

}
