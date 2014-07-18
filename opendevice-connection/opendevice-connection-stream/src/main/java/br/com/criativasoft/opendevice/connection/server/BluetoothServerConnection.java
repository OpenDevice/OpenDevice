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

package br.com.criativasoft.opendevice.connection.server;

import br.com.criativasoft.opendevice.connection.AbstractStreamConnection;
import br.com.criativasoft.opendevice.connection.BluetoothConnection;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.serialize.DefaultSteamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;

public class BluetoothServerConnection extends AbstractStreamConnection {
	
	private static final Logger log = LoggerFactory.getLogger(BluetoothConnection.class);
	
	private String deviceURI = new javax.bluetooth.UUID("27012f0c68af4fbf8dbe6bbaf7aa432a", false).toString();
	
	private StreamConnection connection;
	
	@Override
	public void connect() throws ConnectionException {
		try {

			if(!isConnected()){
				
				initConnection(); // Setup
			
				// open the streams
				input = connection.openInputStream();
				output = connection.openOutputStream();

                getStreamReader().setInput(input);

                if(reader instanceof DefaultSteamReader){
                    ((DefaultSteamReader)reader).startReading();
                }
				
				setStatus(ConnectionStatus.CONNECTED);
			
			}
			
		} catch (IOException e) {
			throw new ConnectionException(e);
		}
	}

	@Override
	public void disconnect() throws ConnectionException {
		if(isConnected()){
			try {
				connection.close();
				super.disconnect();
				connection = null; // release...
			} catch (IOException e) {
				throw new ConnectionException(e);
			}
			
			log.debug("Disconnected !");
		}else{
			log.info("disconnect :: not connected !");
		}
	}

	private void initConnection() throws IOException{
		if(connection == null){
			StreamConnectionNotifier server = (StreamConnectionNotifier) Connector.open(getDeviceURLConnection());
			log.info("Waiting for incoming connection...");
			connection = server.acceptAndOpen();
			String bluetoothAddress = RemoteDevice.getRemoteDevice(connection).getBluetoothAddress();
			log.debug("Conneted to: " + bluetoothAddress);
		}
	}
	
	private String getDeviceURLConnection(){
		return "btspp://localhost:"+deviceURI+";name=BluetoothConnectionServer;authenticate=false;encrypt=false";
	}
	
	@Override
	public boolean isConnected() {
		return connection != null && super.isConnected();
	}
	
}
