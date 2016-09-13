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
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.command.*;
import br.com.criativasoft.opendevice.core.metamodel.DeviceHistoryQuery;
import br.com.criativasoft.opendevice.core.metamodel.DeviceVO;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceHistory;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Device Rest Interface for controling devices. <br/>
 * See https://opendevice.atlassian.net/wiki/display/DOC/Rest+API
 *
 * @author Ricardo JL Rufino
 * @date 04/07/14.
 */
@Path("device")
@RequiresAuthentication
public class DeviceRest {

    private static final Logger log = LoggerFactory.getLogger(DeviceRest.class);

    @Inject
    private DeviceManager manager;

    @Inject
    private ServerConnection connection;

    @HeaderParam("X-AppID")
    private String applicationID;

    @GET
    @Path("/{uid}/value/{value}")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseCommand setValue(@PathParam("uid") int uid, @PathParam("value") String value){

        String connectionUUID = connection.getUID();
        ResponseCommand response;
        Device device = manager.findDeviceByUID(uid);

        if(device != null){
            DeviceCommand command = new DeviceCommand(CommandType.DIGITAL, uid, Long.parseLong(value));
            command.setApplicationID(getApplicationID());
            connection.notifyListeners(command);

            response = new ResponseCommand(CommandStatus.DELIVERED, connectionUUID);
        }else{
            response = new ResponseCommand(CommandStatus.NOT_FOUND, connectionUUID);
        }
        response.setApplicationID(getApplicationID());

        return response;
    }

    @GET
    @Path("/{uid}/value")
    @Produces(MediaType.APPLICATION_JSON)
    public String getValue(@PathParam("uid") int uid) {

        Device device = manager.findDeviceByUID(uid);

        if(device != null){
            return Long.toString(device.getValue());
        }

        return "";// TODO: return error ?

    }

    @GET
    @Path("/{uid}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceVO getDevice(@PathParam("uid") int uid) {

        Device device = manager.findDeviceByUID(uid);

        if(device != null){
            return new DeviceVO(device);
        }

        return null;
    }

    @DELETE
    @Path("/{uid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("uid") int uid){

        Device device = manager.findDeviceByUID(uid);

        if(device != null){
            manager.getDeviceDao().delete(device);
            return Response.status(Response.Status.OK).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DeviceVO> list() throws IOException {


        List<DeviceVO> devices = new LinkedList<DeviceVO>();

        Collection<Device> deviceList = manager.getDevices();

        if(deviceList != null){
            for (Device device : deviceList) {
                devices.add(new DeviceVO(device));
            }
        }

        return devices;
    }


    @POST
    @Path("/{uid}/history")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DeviceHistory> getDeviceHistory(@PathParam("uid") int uid, DeviceHistoryQuery query) {
        query.setDeviceID(uid);
        return manager.getDeviceHistory(query);
    }

    @GET
    @Path("/sync")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sync() throws  IOException{
        manager.send(new GetDevicesRequest());
        return Response.status(Response.Status.OK).build();
    }

    private String getApplicationID(){

        if(applicationID != null && applicationID.length() != 0) return applicationID;

        applicationID = connection.getApplicationID();

        if(applicationID != null  && applicationID.length() != 0) return applicationID;

        applicationID = TenantProvider.getCurrentID();

        return applicationID;

    }

}
