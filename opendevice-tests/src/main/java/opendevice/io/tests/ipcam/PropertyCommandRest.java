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

package opendevice.io.tests.ipcam;

import br.com.criativasoft.opendevice.connection.ServerConnection;
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.command.SetPropertyCommand;
import br.com.criativasoft.opendevice.core.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Ricardo JL Rufino
 * @date 11/01/16
 */
@Path("property")
public class PropertyCommandRest {

    // property/{deviceID}/{propID}/value

    @Inject
    private ServerConnection connection;

    @Inject
    private DeviceManager manager;

    @HeaderParam("X-AppID")
    private String applicationID;

    private static final Logger log = LoggerFactory.getLogger(PropertyCommandRest.class);

    @GET
    @Path("/{deviceID}/{property}/{value}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setValue(@PathParam("deviceID") int deviceID, @PathParam("property") String property, @PathParam("value") String value) {

        System.out.println("REST SET: " + deviceID + ", property:" + property + ", value: " + value);

        Device device = manager.findDeviceByUID(deviceID);

        // FIXME: dynamic mapping
        int valueInt = Integer.parseInt(value);

        connection.notifyListeners(new SetPropertyCommand(deviceID, property, valueInt));

        System.out.println("manager : " + manager);
        System.out.println("connection : " + connection);
        return Response.status(Response.Status.OK).build();
    }
}
