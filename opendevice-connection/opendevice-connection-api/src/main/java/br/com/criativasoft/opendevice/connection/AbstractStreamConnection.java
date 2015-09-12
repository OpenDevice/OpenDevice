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
import br.com.criativasoft.opendevice.connection.message.ByteMessage;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.SimpleMessage;
import br.com.criativasoft.opendevice.connection.serialize.DefaultSteamReader;
import br.com.criativasoft.opendevice.connection.serialize.StreamReader;
import br.com.criativasoft.opendevice.connection.serialize.StreamSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// TODO: Dizer que o protocole de recebimento de dados deve ser delegado ao serialReader.
public abstract class AbstractStreamConnection extends AbstractConnection implements StreamConnection{

	protected static final Logger log = LoggerFactory.getLogger(AbstractStreamConnection.class);
	
	/** Buffered input stream from the port */
	protected InputStream input;
	
	/** The output stream to the port */
	protected OutputStream output;
	
	protected StreamReader reader;

    protected String deviceURI;
	
	public AbstractStreamConnection() {
		this.setSerializer(new StreamSerializer());
		this.setStreamReader(new DefaultSteamReader());
	}
	
	@Override
	public void setStreamReader(StreamReader reader) {
		this.reader = reader;
		this.reader.setConnection(this);
	}

    @Override
    public void setConnectionURI(String uri) {
        this.deviceURI = uri;
    }

    @Override
    public String getConnectionURI() {
        return deviceURI;
    }

    @Override
	public StreamReader getStreamReader() {
		return this.reader;
	}
	
	public InputStream getInput() {
		return input;
	}
	
	public OutputStream getOutput() {
		return output;
	}
	
	public void setInput(InputStream input) {
		this.input = input;
	}
	
	public void setOutput(OutputStream output) {
		this.output = output;
	}
	

	@Override
	public void write(byte value) throws IOException  {
		send(new SimpleMessage(value));
	}

	@Override
	public void write(int value) throws IOException {
		send(new SimpleMessage(value));
	}
	
	@Override
	public void write(int[] value) throws IOException {
		send(new SimpleMessage(value));
	}
	
	@Override
	public void write(long value) throws IOException {
		send(new SimpleMessage(value));
	}
	
	@Override
	public void write(long[] value) throws IOException {
		send(new SimpleMessage(value));
	}
	
	@Override
	public void writeln(String value) throws IOException  {
		byte[] buffer = new byte[value.length() + 2];
		byte[] bytes = value.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			buffer[i] = (byte) bytes[i];
		}
		buffer[bytes.length] = '\r';
		buffer[bytes.length+1] = '\n';
		write(buffer);
	}
	
	@Override
	public void write(String value) throws IOException  {
		send(new SimpleMessage(value));
	}
	
	@Override
	public void write(byte[] value) throws IOException  {
		if(!isConnected()){
            log.warn("connection closed !");
            return;
        }

		output.write(value);
		output.flush();	
	}
	
	@Override
	public void send(Message message) throws IOException {

        if(message instanceof ByteMessage){
            write( ((ByteMessage) message).getBytes() );
        }
        else if(getSerializer() != null){
			write((byte[])getSerializer().serialize(message));
		}
	}
	
	protected void closeStreams() throws IOException{
		
		try{
			if(input != null) input.close();
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}
		
		try{
			if(output != null) output.close();
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}		
		
	}
	
	@Override
	public void disconnect() throws ConnectionException {
		try {
			closeStreams();
			getListeners().clear();
			setStatus(ConnectionStatus.DISCONNECTED);
		} catch (IOException e) {
			throw new ConnectionException(e);
		}
		
	}
}
