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

import br.com.criativasoft.opendevice.connection.ServerConnection;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.ResponseCommand;
import br.com.criativasoft.opendevice.wsrest.io.WSEventsLogger;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.atmosphere.annotation.Suspend;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;

/**
 * TODO: PENDING DOC
 *
 * @author Ricardo JL Rufino
 * @date 08/07/14.
 */
@Path("/ws/device/{topic}")
@RequiresAuthentication
public class WebSocketResource {

    private static final Logger log = LoggerFactory.getLogger(WebSocketResource.class);

    private @PathParam("topic") Broadcaster topic;

    private @Context AtmosphereResource resource;

    @Inject /* Using: ConnectionGuiceProvider */
    private ServerConnection connection;

    @GET
    @Suspend(contentType = "application/json", listeners = WSEventsLogger.class)
    public Response onConnect() {
        resource.getRequest().setAttribute("lastConnectionDate", new Date());
        return Response.status(Response.Status.OK).build();
    }


    @POST
    // @Broadcast(writeEntity = true)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseCommand onMessageReceived(Command command, @Context HttpHeaders headers) {

        log.debug("Command receivend in topic:{} -> {}", topic.getID(), command);

        // TODO: Verificar autenticação.... (basicamente teve enviar um comando de AUTH com o Login, e isso é registrado com resource.uuid())
        // Se não tiver autenticado a respota deve ser AuthRespose(subtype: AUTH_REQUIRED)
//        ResponseCommand response = null;

        // Find caller ID.
        if(command.getConnectionUUID() == null)   command.setConnectionUUID(resource.uuid());
        command.setApplicationID(topic.getID()); // This is APPLICATION ID TOKEN !
        command.setConnectionUUID(this.resource.uuid());

         connection.notifyListeners(command); // broadcast to all clients (browser/android/desktop)

        // return new Broadcastable(params, response, topic);
        return null;
    }

}
