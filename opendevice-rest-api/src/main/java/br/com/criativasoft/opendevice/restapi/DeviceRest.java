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


import br.com.criativasoft.opendevice.connection.ServerConnection;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.*;
import br.com.criativasoft.opendevice.core.metamodel.DeviceVO;
import br.com.criativasoft.opendevice.core.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Collection;
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
    private ServerConnection connection;

    private String clientUUID = "clientname-123456"; // PEGAR DO USUARIO LOGADO !!

    @GET
    @Path("/{uid}/value/{value}")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseCommand setValue(@PathParam("uid") int uid, @PathParam("value") String value){

        DeviceCommand command = new DeviceCommand(CommandType.ON_OFF, uid, Long.parseLong(value));
        command.setApplicationID(clientUUID);
        connection.notifyListeners(command);

        String connectionUUID = connection.getUID();
        ResponseCommand response = new ResponseCommand(CommandStatus.DELIVERED, connectionUUID);

        return response;
    }

    @GET
    @Path("/{uid}/value")
    @Produces(MediaType.APPLICATION_JSON)
    public String getValue(@PathParam("uid") int uid) {

        GetDevicesRequest request = new GetDevicesRequest(GetDevicesRequest.FILTER_BY_ID, uid);
        request.setApplicationID(clientUUID);
        request.setConnectionUUID(connection.getUID());

        request.setApplicationID(clientUUID);
        request.setConnectionUUID(connection.getUID());

        Message response = connection.notifyAndWait(request);

        if(response instanceof  GetDevicesResponse){

            GetDevicesResponse devicesResponse = (GetDevicesResponse) response;

            Collection<Device> deviceList = devicesResponse.getDevices();

            if(!deviceList.isEmpty()){
                Device device = deviceList.iterator().next();
                return ""+device.getValue();
            }

        }

        return "";

    }

    @GET
    @Path("/{uid}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceVO getDevice(@PathParam("uid") int uid) {

        GetDevicesRequest request = new GetDevicesRequest(GetDevicesRequest.FILTER_BY_ID, uid);
        request.setApplicationID(clientUUID);
        request.setConnectionUUID(connection.getUID());

        request.setApplicationID(clientUUID);
        request.setConnectionUUID(connection.getUID());

        Message response = connection.notifyAndWait(request);

        if(response instanceof  GetDevicesResponse){

            GetDevicesResponse devicesResponse = (GetDevicesResponse) response;

            Collection<Device> deviceList = devicesResponse.getDevices();

            if(!deviceList.isEmpty()){
                Device device = deviceList.iterator().next();
                return new DeviceVO(device);
            }

        }

        return null;

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
    public List<DeviceVO> list() throws IOException {

        GetDevicesRequest request = new GetDevicesRequest();
        request.setApplicationID(clientUUID);
        request.setConnectionUUID(connection.getUID());

        Message response = connection.notifyAndWait(request);

        List<DeviceVO> devices = new LinkedList<DeviceVO>();

        if(response instanceof  GetDevicesResponse){

            GetDevicesResponse devicesResponse = (GetDevicesResponse) response;

            Collection<Device> deviceList = devicesResponse.getDevices();

            for (Device device : deviceList) {
                devices.add(new DeviceVO(device));
            }

        }

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
