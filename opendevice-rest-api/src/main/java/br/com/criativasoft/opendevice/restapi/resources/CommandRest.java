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

package br.com.criativasoft.opendevice.restapi.resources;

import br.com.criativasoft.opendevice.connection.ServerConnection;
import br.com.criativasoft.opendevice.connection.message.Request;
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.command.*;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 *         Date: 06/05/17
 */
@Path("/api/cmd")
@RequiresAuthentication
@Produces(MediaType.APPLICATION_JSON)
public class CommandRest {

    private static final Logger log = LoggerFactory.getLogger(DeviceRest.class);

    @Inject
    private DeviceManager manager;

    @Inject
    private ServerConnection connection;

    @POST
    public Command onMessageReceived(Command command) {

        String connectionUUID = connection.getUID();

        command.setApplicationID(getApplicationID());

        if(command == null) return new ResponseCommand(CommandStatus.BAD_REQUEST, connectionUUID);;

        ResponseCommand response = null;

        if(command instanceof Request){

            if(command instanceof GetDevicesRequest){

                response = new GetDevicesResponse(manager.getDevices(), connectionUUID);

            }else{ // FIXME: implement resposse for other requests (see WaitResponseListener)
                return new ResponseCommand(CommandStatus.BAD_REQUEST, connectionUUID);
            }

        }else{
            connection.notifyListeners(command);
            response = new ResponseCommand(CommandStatus.DELIVERED, connectionUUID);
        }

        response.setApplicationID(getApplicationID());

        return response;
    }


    private String getApplicationID(){

        String applicationID = TenantProvider.getCurrentID();

        if(applicationID != null && applicationID.length() != 0) return applicationID;

        return connection.getApplicationID();

    }
}
