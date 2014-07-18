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

import br.com.criativasoft.opendevice.atemospherews.io.EventsLogger;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.command.ResponseCommand;
import br.com.criativasoft.opendevice.core.command.ResponseCommandStatus;
import br.com.criativasoft.opendevice.core.metamodel.CommandVO;
import org.atmosphere.annotation.Suspend;
import org.atmosphere.cpr.Broadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * TODO: PENDING DOC
 *
 * @autor Ricardo JL Rufino
 * @date 08/07/14.
 */
@Path("/device/connection/{topic}")
public class DeviceConnectionResource {

    private static final Logger log = LoggerFactory.getLogger(DeviceConnectionResource.class);

    private @PathParam("topic") Broadcaster topic;

    @Inject
    private DeviceConnection connection;

    @GET
    @Suspend(contentType = "application/json", listeners = EventsLogger.class)
    public String suspend() {
        return "";
    }


    @POST
    // @Broadcast(writeEntity = true)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseCommand publish(CommandVO params, @Context HttpHeaders headers) {

        if(log.isTraceEnabled()) log.trace("Command receivend in {} -> {}", topic.getID(), params);

        // TODO: Verificar autenticação....

        ResponseCommand response = null;

        // Find caller ID.
        String connectionUUID = null;
        List<String> header = headers.getRequestHeader("connectionUUID");
        if(header == null || header.isEmpty()){
            header = headers.getRequestHeader("X-Atmosphere-tracking-id");
        }
        if(header!= null && !header.isEmpty()) connectionUUID = header.iterator().next();

        // not found in header
        if(connectionUUID == null || connectionUUID.length() == 0 || connectionUUID.equals("0")){
            connectionUUID = params.getConnectionUUID();
        }

        System.out.println(" >> connectionUUID =" + connectionUUID);

        CommandType type = CommandType.getByCode(params.getType());

        if(CommandType.isDeviceCommand(type)){
            long value = Long.parseLong(params.getValue());
            DeviceCommand command = new DeviceCommand(type, params.getDeviceID(), value);
            command.setConnectionUUID(connectionUUID);
            command.setClientID(topic.getID());

            connection.notifyListeners(command); // broadcast to all clients (browser/android/desktop)

            response = new ResponseCommand(type, connectionUUID, ResponseCommandStatus.SUCCESS);
        }

        System.out.println("CommandParams = " + params);

        response=  new ResponseCommand(type, connectionUUID, ResponseCommandStatus.NOT_IMPLEMENTED);

        // return new Broadcastable(params, response, topic);
        return response;
    }

//    @POST
//    @Broadcast
//    @Produces(MediaType.APPLICATION_JSON)
//    public Broadcastable publish(CommandParams params, @Context HttpHeaders headers) {
//
//        ResponseCommand response = null;
//
//        // Find caller ID.
//        String requestUID = null;
//        List<String> header = headers.getRequestHeader("requestUID");
//        if(header == null || header.isEmpty()){
//            header = headers.getRequestHeader("X-Atmosphere-tracking-id");
//        }
//        if(header!= null && !header.isEmpty()) requestUID = header.iterator().next();
//
//        // not found in header
//        if(requestUID == null || requestUID.length() == 0 || requestUID.equals("0")){
//            requestUID = params.getRequestUID();
//        }
//
//        System.out.println(" >> requestUID =" + requestUID);
//
//        CommandType type = CommandType.getByCode(params.getType());
//
//        if(CommandType.isDeviceCommand(type)){
//            long value = Long.parseLong(params.getValue());
//            DeviceCommand command = new DeviceCommand(type, params.getDeviceID(), value);
//            command.setRequestUID(requestUID);
//            connection.notifyListeners(command);
//
//            // response = new ResponseCommand(type, requestUID, ResponseCommandStatus.SUCCESS);
//        }
//
//        System.out.println("CommandParams = " + params);
//
//        // response=  new ResponseCommand(type, requestUID, ResponseCommandStatus.NOT_IMPLEMENTED);
//
//        return new Broadcastable(params, "", topic);
//    }
}
