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

package br.com.criativasoft.opendevice.core;

import br.com.criativasoft.opendevice.connection.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.CommandException;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.command.GetDevicesResponse;
import br.com.criativasoft.opendevice.core.connection.MultipleConnection;
import br.com.criativasoft.opendevice.core.model.Device;

import java.io.IOException;

public abstract class AbstractDeviceController implements ConnectionListener, DeviceManager {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractDeviceController.class);
	
	/** Conexões de clientes : Websockets , http, rest, etc... */
	private MultipleConnection inputConnections;
	
	/** Conexão com os módulos físicos (middleware) ou a um proxy  */
	private MultipleConnection outputConnections;
	
	private CommandDelivery delivery = new CommandDelivery(this);
	
	
	protected void initInputConnections(){
		inputConnections = new MultipleConnection();
		inputConnections.addListener(this);
	}
	
	protected void initOutputInputConnections(){
		outputConnections = new MultipleConnection();
		outputConnections.addListener(this);
	}
	
	
	protected void connectAll() throws ConnectionException{
		if(inputConnections != null) inputConnections.connect();
		if(outputConnections != null) outputConnections.connect();
	}

    protected void disconnectAll(){

        if(inputConnections != null) {
            try {
                inputConnections.disconnect();
            } catch (ConnectionException e) {
                e.printStackTrace();
            }
        }

        if(outputConnections != null){
            try {
                outputConnections.disconnect();
            } catch (ConnectionException e) {
                e.printStackTrace();
            }
        }

    }
	
	public void addConnectionIN(DeviceConnection connection){
		
		if(inputConnections == null) initInputConnections();
		
		inputConnections.addConnection(connection);
		
	}

	public void addConnectionOut(DeviceConnection connection){
		
		if(outputConnections == null) initOutputInputConnections();
		
		outputConnections.addConnection(connection);
	}


    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {

        if(!(message instanceof Command)){
            log.debug("Message received : " + message);
            return;
        }

        Command command = (Command) message;

		log.debug("Command received : " + CommandType.getByCode(command.getType().getCode()).toString());
		
		CommandType type = command.getType();

		if (type == CommandType.GET_DEVICES) {
			
			GetDevicesResponse response = new GetDevicesResponse(getDevices(), command.getConnectionUUID());

			try {
				
				connection.send(response);
				
			} catch (CommandException e) {
				log.error(e.getMessage(), e);
			} catch (IOException e) {
                log.error(e.getMessage(), e);
            }

            // Comandos de ON_OFF e similares..
		}else if(DeviceCommand.isCompatible(type)){
			
			DeviceCommand deviceCommand = (DeviceCommand) command;
			
			int deviceID = deviceCommand.getDeviceID();
			long value = deviceCommand.getValue();
			
			Device device = findDevice(deviceID);
			
			if(device != null){
				device.setValue(value);
			}
			
			// Se foi recebido pelo modulo físico(Bluetooth/USB/Wifi), não precisa ser gerenciado pelo CommandDelivery
			// basta ser enviado para os clientes..
			if(outputConnections != null && outputConnections.exist(connection)){
				log.debug("Sending to input connections...");
                try {
                    inputConnections.send(command); // Não precisa de time-out.
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
			
			// Comando recebido pelos clientes (WebSockets / Rest / etc...)
			// Ele deve ser enviado para o modulo físico, e monitorar a resposta. 
			if(inputConnections != null && inputConnections.exist(connection)){
				log.debug("Sending to output connections...");
                try {
                    sendTo(deviceCommand, outputConnections);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
			
		}		

	}

	
	protected void sendTo(Command command, DeviceConnection connection) throws  IOException {
		if(connection != null && connection.isConnected()){
			delivery.sendTo(command, connection);
		}
	}
	
	@Override
	public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {
		log.debug("connectionStateChanged :: "+ connection.getClass().getSimpleName() + ", status = " + status);
	}
	

	@Override
	public void send(Command command) throws IOException {
		
		if(outputConnections != null){
			delivery.sendTo(command, outputConnections);
		}
		
		if(inputConnections != null){
			delivery.sendTo(command, inputConnections);
		}
		
	}

}
