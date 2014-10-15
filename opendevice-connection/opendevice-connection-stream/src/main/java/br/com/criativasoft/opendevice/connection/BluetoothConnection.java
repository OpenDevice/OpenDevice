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

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BluetoothConnection extends AbstractStreamConnection implements IBluetoothConnection{
	
	private static final Logger log = LoggerFactory.getLogger(BluetoothConnection.class);
	
	private StreamConnection connection;

    public BluetoothConnection() {
        super();
    }
	
	public BluetoothConnection(String deviceURI) {
		super();
		setConnectionURI(deviceURI);
	}

    @Override
    public void setConnectionURI(String uri) {
        uri = (uri != null ? uri.replaceAll("[^A-Za-z0-9]", "") :  null); // clear formating
        super.setConnectionURI(uri);
    }

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

            // If no URL specified , find first device available
            if(getConnectionURI() == null) setConnectionURI(getFirstAvailable());

			String uri = getDeviceURLConnection();
			log.debug("Connecting to: " + uri);
			connection = (StreamConnection) Connector.open(uri);
		}
	}
	
	public String getDeviceURLConnection(){
		return "btspp://"+getConnectionURI()+":1;authenticate=false;encrypt=false;master=false";
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


    /**
     * Returns the first available bluetooth device
     * @return If none is available returns NULL
     */
    public static String getFirstAvailable() {
        List<String> portNames = listAvailable();
        if(portNames != null && portNames.size() > 0) return portNames.get(0);
        return null;
    }


    public static List<String> listAvailable() {
/* Create Vector variable */
        final List<String> devicesDiscovered = new ArrayList();
        try {
            final Object inquiryCompletedEvent = new Object();
            /* Clear Vector variable */
            devicesDiscovered.clear();

            /* Create an object of DiscoveryListener */
            DiscoveryListener listener = new DiscoveryListener() {

                public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                    /* Get devices paired with system or in range(Without Pair) */
                    try {
                        log.info("BT.Device found: " + btDevice.getFriendlyName(true) + " - " + btDevice.getBluetoothAddress() + " ("+btDevice.isAuthenticated()+")");
                    } catch (IOException e) {
                    }
                    devicesDiscovered.add(btDevice.getBluetoothAddress());
                }

                public void inquiryCompleted(int discType) {
                    /* Notify thread when inquiry completed */
                    synchronized (inquiryCompletedEvent) {
                        inquiryCompletedEvent.notifyAll();
                    }
                }

                /* To find service on bluetooth */
                public void serviceSearchCompleted(int transID, int respCode) {
                }

                /* To find service on bluetooth */
                public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                }
            };

            synchronized (inquiryCompletedEvent) {
                /* Start device discovery */
                boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
                if (started) {
                    log.info("Searching for bluetooth devices...");
                    inquiryCompletedEvent.wait();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return devicesDiscovered;
    }


}
