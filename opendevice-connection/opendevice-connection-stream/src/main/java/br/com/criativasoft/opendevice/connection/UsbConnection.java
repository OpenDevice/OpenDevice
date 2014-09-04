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
import jssc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class UsbConnection extends AbstractStreamConnection implements SerialPortEventListener  {
	
	protected static final Logger log = LoggerFactory.getLogger(UsbConnection.class);
	
	// ===============================================
	//  Constants
	// ===============================================
 
	/** The port we're normally going to use. */
	private static final String PORT_NAMES[] = { 
		"/dev/tty.usbserial-A9007UX1", // Mac																	// X
		"/dev/ttyUSB0", // Linux
		"/dev/ttyACM0", // Linux (Uno)
		"COM3", // Windows
	};


    public static final String BAUDRATES = "9600,300,1200,2400,4800,14400,19200,28800,38400,57600,115200";

	/** Default bits per second for COM port. */
	public static int BAUDRATE = 9600;
	
	/** Tempo necessário para o arduino inicializar a USB  */
	// TODO: Verificar se isso é necessário no windows/mac (pois o auto reset só ocorre no windows.)
	private static int ARDUINO_BOOT_TIME = 1200;
	
	// ===============================================
	//  Properties
	// ===============================================
	
	private SerialPort serialPort;
	
	private String portName; // If NULL will use PORT_NAMES to locate defaultport.
	
	
	/**
	 * Create connection to Arduino.
	 * @param portName - port to connect (Ex.: "COM3", "/dev/ttyUSB0" )
	 */
	public UsbConnection(String portName)  {
        if(portName == null) throw new IllegalArgumentException("Serial port name is NULL ! (It's busy , not have access or not found)");
		this.portName = portName;
	}

	public static List<String> listAvaiblePortNames() {
		String[] portNames = SerialPortList.getPortNames();
		return Arrays.asList(portNames);
	}


    /**
     * Returns the first available port
     * @return If none is available returns NULL
     */
    public static String getFirstAvailable() {
        String[] portNames = SerialPortList.getPortNames();
        if(portNames != null && portNames.length > 0) return portNames[0];
        return null;
    }


	/* (non-Javadoc)
	 * @see br.com.criativasoft.arduinoconnection.ArduinoConnection#connect()
	 */
	@Override
	public synchronized void connect() throws ConnectionException {
		
		if(portName != null){
			serialPort = new SerialPort(portName);
		}else{ // TODO: automatic discovery
//			portId  = findPort();
//			if (portId == null) {
//				throw new PortNotFoundException();
//			}
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort.openPort();
			
			// set port parameters
			serialPort.setParams(BAUDRATE, SerialPort.DATABITS_8,SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |  SerialPort.FLOWCONTROL_RTSCTS_OUT);
			
			// open the streams
//			input = serialPort.getInputStream();
//			output = serialPort.getOutputStream();
			
			// add event listeners
			int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR + SerialPort.MASK_ERR + SerialPort.MASK_RING + SerialPort.MASK_BREAK;//Prepare mask
			serialPort.addEventListener(this, mask);
			// serialPort.notifyOnDataAvailable(true);
			
			setStatus(ConnectionStatus.CONNECTED);
			
			log.info("Connected ! " + serialPort.getPortName());
			
			// Aguardar um tempo para o boot do arduino.
			Thread.sleep(ARDUINO_BOOT_TIME);
			
		} catch (Exception e) {
			new ConnectionException(e);
		}
	}

	/* (non-Javadoc)
	 * @see br.com.criativasoft.arduinoconnection.ArduinoConnection#disconnect()
	 */
	@Override
	public synchronized void disconnect() throws ConnectionException {
		
		log.debug("Disconnect SerialPort: " + portName);
			
		if (serialPort != null) {
			
			try {
				serialPort.closePort();
				super.disconnect();
				// FIXME: verificar caso a USB sera removido ou desligada abruptamente
			} catch (SerialPortException e) {
				throw new ConnectionException(e);
			} catch (IOException e) {
				throw new ConnectionException(e);
			}
			setStatus(ConnectionStatus.DISCONNECTED);
		}
	}
	
	
	@Override
	public void write(byte[] value) throws IOException {
		if(!isConnected()) return;
		
		try {
			serialPort.writeBytes(value);
		} catch (SerialPortException e) {
			throw new ConnectionException(e);
		}
	}
	
	public synchronized void serialEvent(SerialPortEvent event) {
		
		if(log.isTraceEnabled()){
			log.trace("SerialEvent[" + event.getEventType() + " on " + event.getPortName()+"]");
		}
		
		if (event.isRXCHAR() && hasListeners()) {// If data is available
			byte[] data;
			try {
				data = serialPort.readBytes(event.getEventValue());
				getStreamReader().processPacketRead(data);
			} catch (SerialPortException e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException(e);
			}
			
		} else if(event.isCTS()){//If CTS line has changed state
            if(event.getEventValue() == 1){//If line is ON
                log.debug("SerialEvent - CTS = ON");
            }
            else {
                log.debug("SerialEvent - CTS = OFF");
            }
        }
        else if(event.isBREAK()){///If DSR line has changed state
            log.debug("SerialEvent - isBREAK");
        }
        else if(event.isERR()){///If DSR line has changed state
            log.debug("SerialEvent - isERR");
        }
        else if(event.isRING()){///If DSR line has changed state
            log.debug("SerialEvent - isRING");
        }
		
	}
	
}
