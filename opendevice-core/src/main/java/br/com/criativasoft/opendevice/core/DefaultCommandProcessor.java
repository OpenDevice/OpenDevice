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

package br.com.criativasoft.opendevice.core;

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.IRemoteClientConnection;
import br.com.criativasoft.opendevice.connection.ServerConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.*;
import br.com.criativasoft.opendevice.core.connection.MultipleConnection;
import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.model.*;
import br.com.criativasoft.opendevice.core.model.test.GenericDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 *
 * @author Ricardo JL Rufino
 *         Date: 13/05/17
 */
public class DefaultCommandProcessor {

    private static final Logger log = LoggerFactory.getLogger(DefaultCommandProcessor.class);

    private BaseDeviceManager manager;

    private AtomicBoolean processingNewDevices = new AtomicBoolean(false);

    private List<Device> partialDevices = new LinkedList<Device>(); // Devices from partial GetDevicesResponse

    public DefaultCommandProcessor(BaseDeviceManager manager) {
        this.manager = manager;
    }

    public void onMessageReceived(Message message, DeviceConnection connection){

        Command command = (Command) message;

        OpenDeviceConfig config = OpenDeviceConfig.get();

        if(command.getApplicationID() == null || command.getApplicationID().length() == 0){
            command.setApplicationID(connection.getApplicationID());
        }

        MultipleConnection inputConnections = manager.getInputConnections();
        MultipleConnection outputConnections = manager.getOutputConnections();

        CommandType type = command.getType();

        if(log.isDebugEnabled()) log.debug("Command Received - Type: {} (from: " + connection.toString() + ")", type.toString());

        // Comandos de DIGITAL e similares..
        if (DeviceCommand.isCompatible(type) || type == CommandType.INFRA_RED) {

            DeviceCommand deviceCommand = (DeviceCommand) command;

            int deviceID = deviceCommand.getDeviceID();
            double value = deviceCommand.getValue();

            Device device = manager.findDeviceByUID(deviceID);

            if(log.isDebugEnabled()) log.debug("Device Change. ID:{}, Value:{}", deviceID, value);

            if(device != null){
                if(device.getType() == Device.NUMERIC){ // fire the event 'onChange' every time a reading is taken
                    device.setValue(value, false);
                }else if (device.getValue() != value){ // for ANALOG, DIGITAL.
                    device.setValue(value, false);
                }else{ // not changed
                    return;
                }
            }

            // If it is received by the physical module (Bluetooth / USB / Wifi), need not be managed by CommandDelivery
            // just be sent to client conenctions ..
            if (outputConnections != null && outputConnections.exist(connection)) {
                try {
                    if(inputConnections != null && inputConnections.getSize() > 0){
                        log.debug("Sending to input connections...");
                        inputConnections.send(command);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Command received by clients (WebSockets / Rest / etc ...)
            // It must be sent to the physical module, and monitor the response.
            if (inputConnections != null && inputConnections.exist(connection)) {

                if(outputConnections.hasConnections()){
                    log.debug("Sending to output connections ("+outputConnections.getSize()+")...");
                    try {
                        manager.sendTo(deviceCommand, outputConnections);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(config.isBroadcastInputs()){
                    try {
                        Set<DeviceConnection> inputs = inputConnections.getConnections();
                        for (DeviceConnection input : inputs) {
                            if(input != connection) inputConnections.send(deviceCommand);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (type == CommandType.SET_PROPERTY) {

            SetPropertyCommand cmd = (SetPropertyCommand) command;

            int deviceID = cmd.getDeviceID();

            Device device = manager.findDeviceByUID(deviceID);

            if(device instanceof GenericDevice){
                GenericDevice genericDevice = (GenericDevice) device;
                genericDevice.setProperty(cmd.getProperty(), cmd.getValue());
                try {
                    if(genericDevice.getConnection() == null) log.warn("Device '" + device + "'  has no connection !");
                    else genericDevice.getConnection().send(cmd);
                } catch (IOException e) {
                    e.printStackTrace(); // TODO: melhor tratamento..
                }
            }

            // FIXME ? oque precisa ser feito ainda
            // no caso da camera não precisar jogar em todos imouts
            // acho que agora vai ser a hora de fazer o mapeamento dos devices.
        } else if (type == CommandType.ACTION) {

            ActionCommand cmd = (ActionCommand) command;

            Device device =  manager.findDeviceByUID(cmd.getDeviceID());

            if(device instanceof GenericDevice){
                GenericDevice genericDevice = (GenericDevice) device;
                // TODO: falta a logica interna de execucao das actions... (usar listeners normais ?)
                //genericDevice.setProperty(cmd.getAction(), cmd.getValue());
                try {
                    genericDevice.getConnection().send(cmd);
                } catch (IOException e) {
                    e.printStackTrace(); // TODO: melhor tratamento..
                }
            }


        } else if (type == CommandType.PING_REQUEST) {

            Command pingResponse = new SimpleCommand(CommandType.PING_RESPONSE, 0);
            try {
                connection.send(pingResponse);
            } catch (IOException e) {
            }

        } else if (type == CommandType.GET_DEVICES) {

            GetDevicesRequest request = (GetDevicesRequest) message;

            // Received GET_DEVICES with ForceSync ( broadcast to output devices )
            if(request.isForceSync() && inputConnections.exist(connection)){

                if(outputConnections.hasConnections()){
                    log.debug("Sending to output connections...");
                    manager.syncDevices(outputConnections, request);
                }

            }else{
                List<Device> devices = new LinkedList<Device>();

                // No filter
                if(request.getFilter() <= 0) devices.addAll(manager.getDevices());

                if(request.getFilter() == GetDevicesRequest.FILTER_BY_ID){
                    Object id = request.getFilterValue();
                    if(id instanceof Integer || id instanceof Long){
                        Device device =  manager.findDeviceByUID((Integer) id);
                        if(device != null) devices.add(device);
                    }
                }

                GetDevicesResponse response = new GetDevicesResponse(devices, command.getConnectionUUID());
                response.setApplicationID(command.getApplicationID());
                response.setConnectionUUID(command.getConnectionUUID());

                try {

                    connection.send(response);

                } catch (CommandException e) {
                    log.error(e.getMessage(), e);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }

        } else if (type == CommandType.DEVICE_COMMAND_RESPONSE) {

//                ResponseCommand responseCommand = (ResponseCommand) command;
            // log.debug("ResponseStatus: " + responseCommand.getStatus());

        } else if (type == CommandType.CONNECT_RESPONSE) {

            ResponseCommand response = (ResponseCommand) command;

            if(response.getStatus() == CommandStatus.UNAUTHORIZED){
                try {
                    log.info("The access information is invalid or are not configured (Authorization Required)");
                    connection.disconnect();
                } catch (ConnectionException e) {
                }
            }

        // When received from microcontrollers, this will received multiple times (1 for device)
        } else if (type == CommandType.GET_DEVICES_RESPONSE) {

            // note if connection is IRemoteClientConnection, is a client not the 'server' running
            // this flag is used to control devices registratin and sync UIDs
            boolean fromClientOrDevice = !(connection instanceof IRemoteClientConnection);

            GetDevicesResponse response = (GetDevicesResponse) command;

            processingNewDevices.set(true);

            partialDevices.addAll(response.getDevices());

            if(!response.isLast()){
                return;
            }

            Collection<Device> loadDevices = partialDevices;

            // Resolver Parent (Boards)
            GetDevicesResponse.resolveParents(loadDevices);

            log.info("Loaded Devices: " + loadDevices.size() + " , from: " + connection.getClass().getSimpleName());
            DeviceDao dao = manager.getValidDeviceDao();
            boolean syncIds = false; // firmware use dynamic ids
            int nextID = -1;

            for (Device device : loadDevices) {
                log.debug(" - " + device.toString());

                Device found =  manager.findDeviceByUID(device.getUid());

                // Fallback, recovery previous device cleared/replaced
                // If the name/id does not match, the name has priority
                if(found == null || !found.getName().equals(device.getName())){
                    found =  manager.findDeviceByName(device.getName());
                    if(fromClientOrDevice) device.setUID(0); // clear, need resyc
                }

                if(found == null){

                    // Device not have ID, Get next ID from database
                    if(device.getUid() <= 0 && !config.isRemoteIDGeneration()){
                        if(nextID == -1) nextID = dao.getNextUID();
                        syncIds = true;
                        device.setUID(nextID++);
                        // TODO: Notify Client Applications ???
                    }

                    device.setApplicationID(response.getApplicationID());
                    if(device.getCategory() != null) {
                        device.setCategory(dao.getCategoryByCode(device.getCategory().getCode())); // update reference
                    }

                    // Adding new device to existing Board
                    if(device instanceof PhysicalDevice){
                        PhysicalDevice physical = (PhysicalDevice) device;
                        Board board = physical.getBoard();
                        if(board != null){
                            board = (Board) manager.findDeviceByName(board.getName());
                            physical.setBoard(board);
                        }

                    }

                    manager.addDevice(device);
                }else{

                    // For devices (check if need send/sync IDs to devices)
                    if(fromClientOrDevice){

                        // Firmware has ben cleared/replaced
                        // This will help recover IDs.
                        if(device.getUid() <= 0 || device.getUid() != found.getUid()){
                            device.setUID(found.getUid());
                            syncIds = true;
                        }

                    // For Clientes (update DeviceID on Local)
                    }else{

                        //  NOTE: probably found a device with the same name on the server, so we should update the client
                        found.setUID(device.getUid());

                        // Runtime devices from LocalDeviceManager (see addDevice)
                        if(!found.isManaged()){
                            found.setManaged(true);
                        }

                    }

                    found.setValue(device.getValue());

                }

            }

            if(syncIds){
                try {

                    // If command received from a client application
                    if(connection instanceof ServerConnection){
                        GetDevicesResponse devicesResponse = new GetDevicesResponse(new LinkedList<Device>(loadDevices), command.getConnectionUUID());
                        devicesResponse.setApplicationID(command.getApplicationID());
                        manager.sendTo(devicesResponse, connection);
                     // If from microcontrollers/device send only IDs
                    }else{
                        SyncDevicesIdCommand syncDevicesIdCommand = new SyncDevicesIdCommand(new LinkedList<Device>(loadDevices));
                        syncDevicesIdCommand.setApplicationID(command.getApplicationID());
                        manager.sendTo(syncDevicesIdCommand, connection);
                    }

                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }

            }

            processingNewDevices.set(false);
            partialDevices.clear();

            // Save device properties
            // NOTE: This command is send from Websocket/Client interface
        } else if (type == CommandType.DEVICE_SAVE) {

            SaveDeviceCommand saveDeviceCommand = (SaveDeviceCommand) command;

            Device device = saveDeviceCommand.getDevice();

            if(device != null){

                Device found =  manager.findDeviceByUID(device.getUid());

                if(found != null){

                    found =  manager.getDeviceDao().getById(found.getId()); // sync with database

                    found.setTitle(device.getTitle());
                    if(device.getCategory() != null){
                        found.setCategory(device.getCategory());
                    }

                    // HACK: force initialize LAZY properties...
                    if(found instanceof PhysicalDevice){
                        Board board = ((PhysicalDevice) found).getBoard();
                        board.getUid(); //
                    }

                    manager.updateDevice(found);

                }

            }else{
                throw new IllegalStateException("Device with UID dont exist ! At this time it is not possible to create devices.");
            }


        // Sync offline data from clients
        } else if (type == CommandType.SYNC_HISTORY) {

            SyncHistoryCommand syncHistoryCommand = (SyncHistoryCommand) command;

            Collection<DeviceHistory> list = syncHistoryCommand.getList();

            for (DeviceHistory history : list) {

                Device device = manager.findDeviceByUID((int) history.getDeviceID());

                // Replace to real/database ID
                if(device != null){
                    history.setDeviceID(device.getId());
                    history.setNeedSync(false);
                }
                manager.saveDeviceHistory(device, history);
            }


        }
    }

    public boolean isProcessingNewDevices() {
        return processingNewDevices.get();
    }
}
