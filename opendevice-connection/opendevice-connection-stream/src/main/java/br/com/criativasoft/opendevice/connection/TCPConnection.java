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

import java.io.IOException;
import java.net.Socket;

/**
 * TCP Socket bases connection
 */
public class TCPConnection extends AbstractStreamConnection{
	
	private static final Logger log = LoggerFactory.getLogger(TCPConnection.class);
	
	private String deviceURI;

    private Socket connection;

    /**
     * @param deviceURI - (Ex.: 192.168.0.101:1234)
     */
	public TCPConnection(String deviceURI) {
		super();
        this.deviceURI = deviceURI;
	}

	@Override
	public void connect() throws ConnectionException {
		
		try {

			if(!isConnected()){
				
				initConnection(); // Setup
			
				// open the streams
				setInput(connection.getInputStream());
				setOutput(connection.getOutputStream());
				
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

            int index = deviceURI.lastIndexOf(":");
            String port = (index > 0 ?  deviceURI.substring(index + 1, deviceURI.length()) : "80");
            String host = deviceURI.substring(0, index);

            log.debug("Connecting to: " + deviceURI);

            connection = new Socket(host, Integer.parseInt(port));

			log.debug("Connectend !");
		}
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
