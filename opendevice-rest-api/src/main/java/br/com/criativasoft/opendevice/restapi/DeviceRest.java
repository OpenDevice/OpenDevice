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

package br.com.criativasoft.opendevice.restapi;


import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.core.command.*;
import br.com.criativasoft.opendevice.core.metamodel.DeviceVO;
import br.com.criativasoft.opendevice.core.model.DeviceCategory;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO: PENDING DOC
 *
 * @autor Ricardo JL Rufino
 * @date 04/07/14.
 */
@Path("device")
public class DeviceRest implements DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceRest.class);

    @Inject
    private DeviceService service;

    @Inject
    private DeviceConnection connection;


    @GET
    @Path("/{id}/value/{value}")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseCommand setValue(@PathParam("id") int id, @PathParam("value") String value){

        String clientUUID = "fake-client-123-123"; // DEVE PEGAR DE ALGUM LUGAR !!
        DeviceCommand command = new DeviceCommand(CommandType.ON_OFF, id, Long.parseLong(value));
        command.setClientID(clientUUID);
        connection.notifyListeners(command);

        String connectionUUID = ""; // this resource
        ResponseCommand response = new ResponseCommand(CommandStatus.DELIVERED, connectionUUID);

        return response;
    }

    @GET
    @Path("/{id}/value")
    @Produces(MediaType.APPLICATION_JSON)
    public String getValue(@PathParam("id") int id) {
        return getService().getValue(id);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String delete(@PathParam("id") int id){
        return getService().delete(id);
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DeviceVO> list(){

        DeviceVO device1 = new DeviceVO(1,"Device 1", DeviceType.DIGITAL, DeviceCategory.LAMP, 0);
        DeviceVO device2 = new DeviceVO(2,"Device 2", DeviceType.DIGITAL, DeviceCategory.POWER_SOURCE, 0);
        DeviceVO device3 = new DeviceVO(3,"Device 3", DeviceType.DIGITAL, DeviceCategory.LAMP, 0);
        DeviceVO device4 = new DeviceVO(4,"Device 4", DeviceType.DIGITAL, DeviceCategory.POWER_SOURCE, 0);

        List<DeviceVO> devices = new LinkedList<DeviceVO>();
        devices.add(device1);
        devices.add(device2);
        devices.add(device3);
        devices.add(device4);

//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        return devices;
    }

    private DeviceService getService(){

        if(service == null){
            service = new DeviceServiceImpl();
            log.warn("@Injection not working !!");
        }

        return service;
    }
}
