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
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.command.*;
import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.metamodel.DeviceHistoryQuery;
import br.com.criativasoft.opendevice.core.metamodel.DeviceVO;
import br.com.criativasoft.opendevice.core.metamodel.EnumVO;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceHistory;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.restapi.io.ErrorResponse;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
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
@Path("/api/devices")
@RequiresAuthentication
@Produces(MediaType.APPLICATION_JSON)
public class DeviceRest {

    private static final Logger log = LoggerFactory.getLogger(DeviceRest.class);

    @Inject
    private DeviceManager manager;

    @Inject
    private DeviceDao dao;

    @Inject
    private ServerConnection connection;

    @GET
    @Path("/{uid}/value/{value}")
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
    public String getValue(@PathParam("uid") int uid) {

        Device device = manager.findDeviceByUID(uid);

        if(device != null){
            return Double.toString(device.getValue());
        }

        return "";// TODO: return error ?

    }

    @GET
    @Path("/{uid}")
    public DeviceVO getDevice(@PathParam("uid") int uid) {

        Device device = manager.findDeviceByUID(uid);

        if(device != null){
            return new DeviceVO(device);
        }else{
            throw new NullPointerException("Device not found"); // use custom serializer
        }


    }

    @DELETE
    @Path("/{uid}")
    public Response delete(@PathParam("uid") int uid){
        Device device = manager.findDeviceByUID(uid);
        if(device != null){
            manager.removeDevice(device);
            return Response.status(Response.Status.OK).entity("{}").build();
        }

        return ErrorResponse.status(Response.Status.NOT_FOUND, "Device not found !");
    }

    @GET
    public List<DeviceVO> list() throws IOException {

        Collection<Device> deviceList = manager.getDevices();

        List<DeviceVO> devices = new LinkedList<DeviceVO>();

        if(deviceList != null){
            for (Device device : deviceList) {
                if(device.getType() == null) throw new IllegalStateException("Device type is NULL = " + device);
                devices.add(new DeviceVO(device));
            }
        }

        return devices;
    }

    @POST
    @Path("/{uid}/history")
    public List<DeviceHistory> getHistory(@PathParam("uid") int uid, DeviceHistoryQuery query ) {
        Device device = manager.findDeviceByUID(uid); // find user device

        if (device != null) {
            query.setDeviceID(device.getId());
            query.setDeviceUID(uid);

            List<DeviceHistory> list = manager.getDeviceHistory(query);

//            try {
//                PrintStream out = new PrintStream(new FileOutputStream("/media/ricardo/Dados/TEMP/teste/data1.csv"));
//                int index = 0;
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//                for (DeviceHistory history : list) {
//                    out.println((index++) + "," + history.getValue() + "," + sdf.format(new Date(history.getTimestamp())));
//
//                out.close();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }

            return list;
        } else {
            return new ArrayList<DeviceHistory>();
        }
    }

    @DELETE
    @Path("/{uid}/history")
    public Response deleteHistory(@PathParam("uid") int uid) {
        Device device = manager.findDeviceByUID(uid); // find user device

        if (device != null) {

            // List<DeviceHistory> list = manager.getDeviceHistory(query);
            dao.deleteHistory(device);
            device.setValue(0);

            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }


    @GET
    @Path("/sync")
    public Response sync() throws  IOException{
        manager.send(new GetDevicesRequest());
        return Response.status(Response.Status.OK).build();
    }

    private String getApplicationID(){

        String applicationID = TenantProvider.getCurrentID();

        if(applicationID != null && applicationID.length() != 0) return applicationID;

        applicationID = connection.getApplicationID();

        if(applicationID != null  && applicationID.length() != 0) return applicationID;

        applicationID = TenantProvider.getCurrentID();

        return applicationID;

    }

    @GET @Path("/types")
    public List<EnumVO> listTypes() {

        List<EnumVO> list = new LinkedList<EnumVO>();

        for (DeviceType value : DeviceType.values()) {
            if(value != DeviceType.MANAGER){
                list.add(new EnumVO(value));
            }
        }

        return list;
    }

}
