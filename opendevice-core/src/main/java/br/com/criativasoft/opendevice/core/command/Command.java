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

package br.com.criativasoft.opendevice.core.command;

import br.com.criativasoft.opendevice.connection.message.Message;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.UUID;

/**
 * @author Ricardo JL Rufino
 * @date 04/09/2011 13:48:57
 */
@XmlRootElement
public abstract class Command implements Message{
	
	private static final long serialVersionUID = 676280722282919715L;

	private long id; // DataBase ID.
	private String uid; // Logic level user ID.
	private String connectionUUID; // id of connection/channel that requested the command
    private String clientID; // id of client (for Multitenancy support)
	
	private CommandType type;
	private Date timestamp;
	private CommandStatus status = CommandStatus.CREATED;
	
	public Command(CommandType type) {
		this(type, UUID.randomUUID().toString(), null);
	}

	public Command(CommandType type, String uid, String connectionUUID) {
		super();
		this.uid = uid;
		this.connectionUUID = connectionUUID;
		this.type = type;
		this.setTimestamp(new Date());
	}


	/** Internal command ID */
	public long getId() {
		return id;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public CommandType getType() {
		return type;
	}
	
	public String getUid() {
		return uid;
	}
	
	public void setConnectionUUID(String connectionUUID) {
		this.connectionUUID = connectionUUID;
	}

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getClientID() {
        return clientID;
    }

    /**
	 * Can be the id of client that requested the command
	 */
	public String getConnectionUUID() {
		return connectionUUID;
	}
	
	public void setStatus(CommandStatus status) {
		this.status = status;
	}
	
	public CommandStatus getStatus() {
		return status;
	}


	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "["+hashCode()+"]";
	}
	
}
