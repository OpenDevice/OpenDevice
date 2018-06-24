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

package br.com.criativasoft.opendevice.middleware.resources;

import br.com.criativasoft.opendevice.connection.*;
import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.connection.ConnectionInfo;
import br.com.criativasoft.opendevice.core.connection.ConnectionType;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.connection.MultipleConnection;
import br.com.criativasoft.opendevice.mqtt.MQTTResource;
import br.com.criativasoft.opendevice.restapi.io.ErrorResponse;
import br.com.criativasoft.opendevice.restapi.model.AccountType;
import br.com.criativasoft.opendevice.wsrest.AbstractAtmosphereConnection;
import br.com.criativasoft.opendevice.wsrest.WebSocketResource;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 18/10/16
 */
@Path("/middleware/connections")
@RequiresAuthentication
@RequiresRoles(AccountType.ROLES.ACCOUNT_MANAGER)
@Produces(MediaType.APPLICATION_JSON)
public class ConnectionsRest {

    private static final Logger log = LoggerFactory.getLogger(WebSocketResource.class);

    @Inject /* Using: ConnectionGuiceProvider */
    private ServerConnection connection;

    @Inject /* Using: ConnectionGuiceProvider */
    private DeviceManager manager;

    @GET
    public List<ConnectionInfo> list() throws IOException {

        List<ConnectionInfo> list = new LinkedList<ConnectionInfo>();

        String appID = TenantProvider.getCurrentID();

        // Clients
        if(connection instanceof AbstractAtmosphereConnection){
            List<ConnectionInfo> connections = ((AbstractAtmosphereConnection) connection).getConnections();

            for (ConnectionInfo info : connections) {
                if(appID != null && appID.equals(info.getApplicationID())){
                    list.add(info);
                }
            }
        }


        MultipleConnection outputConnections = ((BaseDeviceManager)manager).getOutputConnections();

        Set<DeviceConnection> connections = outputConnections.getConnections();

        for (DeviceConnection deviceConnection : connections) {
            if (appID != null && appID.equals(deviceConnection.getApplicationID())) {

                ConnectionInfo info = new ConnectionInfo();
                info.setUuid(deviceConnection.getUID());
                info.setFistConnection(deviceConnection.getFistConnectionDate());
                info.setLastConnection(deviceConnection.getLastConnectionDate());
                info.setApplicationID(deviceConnection.getApplicationID());

                if (deviceConnection instanceof MQTTResource) {
                    MQTTResource resource = (MQTTResource) deviceConnection;
                    info.setHost(resource.getDeviceName());
                    info.setType(ConnectionType.MQTT.name());
                }else if (deviceConnection instanceof IUsbConnection) {
                    info.setHost(((IUsbConnection) deviceConnection).getConnectionURI());
                    info.setType(ConnectionType.USB.name());
                }else if (deviceConnection instanceof IBluetoothConnection) {
                    info.setHost(((IBluetoothConnection) deviceConnection).getConnectionURI());
                    info.setType(ConnectionType.BLUETOOTH.name());
                }else if (! (deviceConnection instanceof ServerConnection)){
                    info.setHost(connection.getUID());
                    info.setType(ConnectionType.WIFI.name());
                }else{
                    info = null;
                }

                if(info!= null) list.add(info);

            }

        }


        return list;
    }

    @GET @Path("/discovery")
    public List<ConnectionInfo> discovery(@QueryParam("type") ConnectionType type) throws IOException {

        List<ConnectionInfo> list = new LinkedList<ConnectionInfo>();

        // TODO: Check: OpenDeviceConfig.get().isTenantsEnabled();

        if(type == ConnectionType.USB){

            List<String> ports = UsbConnection.listAvailable();
            for (String port : ports) {
                ConnectionInfo info = new ConnectionInfo(type.name(), port);
                list.add(info);
            }
        }

        return list;
    }

    @POST
    public Response save(ConnectionInfo info) throws IOException {

        ConnectionType type = ConnectionType.valueOf(info.getType());

        DeviceConnection connection = null;

        if(type == ConnectionType.USB){

            connection = Connections.out.usb(info.getHost());
        }

        if(connection != null){

            manager.addOutput(connection);

            connection.connect();

            if(connection.isConnected()){
//                connection.send(new GetDevicesRequest());
            }
        }

        return Response.ok().build();
    }

    @DELETE @Path("/{uuid}")
    public Response delete(@PathParam("uuid") String uuid) throws IOException {

        MultipleConnection outputConnections = ((BaseDeviceManager)manager).getOutputConnections();

        boolean removed = false;

        Set<DeviceConnection> connections = outputConnections.getConnections();

        for (DeviceConnection deviceConnection : connections) {

            if(deviceConnection.getUID().equals(uuid)){
                if(deviceConnection instanceof  StreamConnection) {
                    manager.removeOutput(deviceConnection);
                    removed = true;
                    break;
                }else if(deviceConnection instanceof MQTTResource) {
                    return ErrorResponse.BAD_REQUEST("MQTT not allow remote disconnection!");
                }else{
                    return ErrorResponse.BAD_REQUEST("Disconnection not allow for this connection !");
                }

            }
        }


        if(removed)return Response.ok().build();
        else return ErrorResponse.BAD_REQUEST("Connection not found");

    }
}
