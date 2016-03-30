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

public class UsbConnection extends AbstractStreamConnection implements IUsbConnection, SerialPortEventListener  {
	
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

    private boolean automaticPort = false;
	
    /**
     * Create a usb connection with physical device on first available port (@{link #getFirstAvailable})
     */
    public UsbConnection()  {
       this(null);
    }
	
	/**
	 * Create a usb connection with physical device
	 * @param portName - port to connect (Ex.: "COM3", "/dev/ttyUSB0" )
	 */
	public UsbConnection(String portName)  {
		this.deviceURI = portName;
	}


	public static List<String> listAvailable() {
        return listAvailablePortNames();
	}

    @Deprecated
	public static List<String> listAvailablePortNames() {
		String[] portNames = SerialPortList.getPortNames();
		return Arrays.asList(portNames);
	}

    /**
     * Returns the first available port
     * @return If none is available returns NULL
     */
    public static String getFirstAvailable() {
        String[] portNames = SerialPortList.getPortNames();
        if(portNames != null && portNames.length > 0){
            for (String port : portNames){
                if(!port.contains("rfcomm0")){
                    return port;
                }
            }
            return null;
        }
        return null;
    }


	@Override
	public synchronized void connect() throws ConnectionException {
		
		if(deviceURI != null){
			serialPort = new SerialPort(deviceURI);
		}else{

            deviceURI = getFirstAvailable();

            if(deviceURI == null) throw new ConnectionException("Serial port name is NULL ! (It's busy , not have access or not found)");

            serialPort = new SerialPort(deviceURI);

            automaticPort = true;
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort.openPort();
			
			// set port parameters
			serialPort.setParams(BAUDRATE, SerialPort.DATABITS_8,SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			
                        // NOTE: Not working with arduino.nano
			// serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |  SerialPort.FLOWCONTROL_RTSCTS_OUT);
			
			// add event listeners
			int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR + SerialPort.MASK_ERR + SerialPort.MASK_RING + SerialPort.MASK_BREAK;//Prepare mask
			serialPort.setEventsMask(mask);
                        serialPort.addEventListener(this);
			// serialPort.notifyOnDataAvailable(true);


			// Wait a while to boot the Arduino.
            // NOTE: Perhaps this is only required for Arduino
			Thread.sleep(ARDUINO_BOOT_TIME);

            log.info("Connected ! " + serialPort.getPortName());

            setStatus(ConnectionStatus.CONNECTED);

//          NOTE: No need, because the implementation of reading is already done in serialEvent
//            if(reader instanceof DefaultSteamReader){
//                ((DefaultSteamReader)reader).startReading();
//            }
			
		} catch (Exception e) {
			new ConnectionException(e);
		}
	}

    @Override
    public synchronized void disconnect() throws ConnectionException {

        log.debug("Disconnect SerialPort: " + deviceURI);

        if (serialPort != null) {

            try {
                serialPort.closePort();
                // FIXME: verificar caso a USB sera removido ou desligada abruptamente
            } catch (SerialPortException e) {
                throw new ConnectionException(e);
            } finally {
                super.disconnect();
            }

            if(automaticPort) deviceURI = null;
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
	
       @Override
    public synchronized void serialEvent(SerialPortEvent event) {

        if (log.isTraceEnabled()) {
            log.trace("SerialEvent '" + event.getEventType() + "' on [" + event.getPortName() + "]");
        }

        if (event.isRXCHAR() && hasListeners()) {// If data is available
            byte[] data;
            try {
                data = serialPort.readBytes(event.getEventValue());
                getStreamReader().processPacketRead(data, data.length);
            } catch (SerialPortException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }

        } else if (event.isCTS()) {//If CTS line has changed state
            if (event.getEventValue() == 1) {//If line is ON
                log.debug("SerialEvent - CTS = ON");
            } else {
                log.debug("SerialEvent - CTS = OFF");
            }
        } else if (event.isBREAK()) {///If DSR line has changed state
            log.debug("SerialEvent - isBREAK");
        } else if (event.isERR()) {///If DSR line has changed state
            log.debug("SerialEvent - isERR");
            try {
                disconnect();
            } catch (ConnectionException ex) {
                log.error(ex.getMessage(), ex);
            }
        } else if (event.isRING()) {///If DSR line has changed state
            log.debug("SerialEvent - isRING :" + event.getEventValue());
        }

    }

    @Override
    public String toString() {
        return "UsbConnection["+getConnectionURI()+"]";
    }
}
