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

package br.com.criativasoft.opendevice.connection.serialize;

import br.com.criativasoft.opendevice.connection.AbstractStreamConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.SimpleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Class responsible for reading the information of an InputStream , the parser of the information is done by {@link MessageSerializer}
 * @author Ricardo JL Rufino
 * @date 18/06/2014
 */
public class DefaultSteamReader implements Runnable, Cloneable, StreamReader {
	
	private static Logger log = LoggerFactory.getLogger(StreamSerializer.class);
	
	protected InputStream input;
	
	protected AbstractStreamConnection connection;
	
	/** Buffer for data read*/
	protected ByteArrayOutputStream inputBuffer = new ByteArrayOutputStream();
	
	private byte endOfMessageToken = AbstractStreamConnection.TOKEN_LF;
	
	private Thread readingThread = null;
	
	public DefaultSteamReader() {
	}
	
	public AbstractStreamConnection getConnection() {
		return connection;
	}
	
	public void setInput(InputStream input) {
		this.input = input;
	}
	
	public void setConnection(AbstractStreamConnection connection) {
		this.connection = connection;
	}

	
	protected boolean checkEndOfMessage(byte lastByte, ByteArrayOutputStream readBuffer){
		return getEndOfMessageToken() == lastByte;
	}
	
	/** Define um token a seu usado como EOM ( End of Message )  */
	public void setEndOfMessageToken(byte endOfMessageToken) {
		this.endOfMessageToken = endOfMessageToken;
	}

	public void setEndOfMessageToken(char endOfMessageToken) {
		this.endOfMessageToken = (byte) endOfMessageToken;
	}
	
	public byte getEndOfMessageToken() {
		return endOfMessageToken;
	}
	
	/** Start thead to chek if data is avaible. <br/>
	 * Required only if the encapsulated connection does not provide any mechanism for callback or listener
	 */
	public void startReading(){
		if(readingThread == null || ! readingThread.isAlive()){
			readingThread = new Thread(this);
            readingThread.setName("Conn:" +connection.getConnectionURI());
            readingThread.setDaemon(true);
			readingThread.start();
		}
	}
	
	@Override
	public void run() {
		while(connection.isConnected()){
			checkDataAvailable();
		}
	}
	

	/**
	 * Método chamado pela connection informando que deve ser lido os dados da serial
	 */
	public void checkDataAvailable() {

		synchronized (input) {
			try {

                int bytes = 0;

                byte data[] = new byte[1024];

                while(true){

                    int read = input.read(data, bytes, data.length - bytes);

                    if(read == -1) return; // EOL

                    int len = bytes + read;

                    for (int i = bytes; i < len; i++) {

                        if(checkEndOfMessage(data[i], inputBuffer)){

                            processPacketRead(data, len);

                            return;

                        }

                    }

                    bytes = len;

                }

			} catch (Exception e) {
				if(e.getMessage() != null && e.getMessage().contains("closed")){
					try {
						log.info("Connection closed, forcing disconnect !");
						this.connection.disconnect();
					} catch (ConnectionException e1) {
						throw new RuntimeException(e);
					}
				}else{
					throw new RuntimeException(e);
				}
			}			
		}

	}
	
	public void processPacketRead(byte read[], int length){
        if(log.isTraceEnabled()) {
            log.trace("processPacketRead: " + new String(read) + " (size: " + read.length + ")");
        }
		
		for (int i = 0; i < length; i++) {
			// Quebra de linha, processar comandos
			if (checkEndOfMessage(read[i], inputBuffer)) {
				byte[] array = inputBuffer.toByteArray();
				
				Message message = parse(array);
				notifyOnDataRead(message);
				
				inputBuffer.reset();
				
			// Read until EOL.
		    // Ex: LF (Line feed, '\n', 0x0A, 10 in decimal) or CR (Carriage return, '\r', 0x0D, 13 in decimal)
			}else{
				inputBuffer.write(read[i]);
			}

		}
	}
	
	protected Message parse(final byte data[]){
        MessageSerializer serializer = connection.getSerializer();
        if(serializer != null){
            return serializer.parse(data);
        }else{
            return new SimpleMessage(data);
        }
	}
	
	protected void notifyOnDataRead(final Message message){
		connection.notifyListeners(message);
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		DefaultSteamReader clone = new DefaultSteamReader();
		clone.setEndOfMessageToken(endOfMessageToken);
		return clone;
	}
	
}
