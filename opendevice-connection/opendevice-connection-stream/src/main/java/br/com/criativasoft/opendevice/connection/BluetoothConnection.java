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

package br.com.criativasoft.opendevice.connection;

import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.serialize.DefaultSteamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.IOException;

public class BluetoothConnection extends AbstractStreamConnection{
	
	private static final Logger log = LoggerFactory.getLogger(BluetoothConnection.class);
	
	private String deviceURI;
	
	private StreamConnection connection;
	
	public BluetoothConnection(String deviceURI) {
		super();
		this.deviceURI = (deviceURI != null ? deviceURI.replaceAll("[^A-Za-z0-9]", "") :  null); // clear formating
	}
	
//	/**
//	 * 
//	 * @param deviceURI
//	 * @param connection
//	 * @param reader
//	 */
//	protected BluetoothConnection(String deviceURI, StreamConnection connection, StreamSerializer reader) {
//		super();
//		this.deviceURI = deviceURI;
//		this.connection = connection;
//		try {
//			setSerializer((StreamSerializer)reader.clone());
//		} catch (CloneNotSupportedException e) {
//		}
//		setStatus(ConnectionStatus.CONNECTED);
//	}

	@Override
	public void connect() throws ConnectionException {
		
		try {

			if(!isConnected()){
				
				initConnection(); // Setup
			
				// open the streams
				setInput(connection.openInputStream());
				setOutput(connection.openOutputStream());
				
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
	
	private void initConnection() throws IOException{
		if(connection == null){
			String uri = getDeviceURLConnection();
			log.debug("Connecting to: " + uri);
			connection = (StreamConnection) Connector.open(uri);
			log.debug("Connectend ! internal connection: " + connection.getClass());
		}
	}
	
	public String getDeviceURLConnection(){
		return "btspp://"+deviceURI+":1;authenticate=false;encrypt=false;master=false";
	}
	
	public String getDeviceURI() {
		return deviceURI;
	}

	@Override
	public void disconnect() throws ConnectionException {
		if(isConnected()){
			try {
				connection.close();
				super.disconnect();
				connection = null;
			} catch (IOException e) {
				throw new ConnectionException(e);
			}
			
			log.debug("Disconnected !");
		}else{
			log.info("disconnect :: not connected !");
		}
	}


}
