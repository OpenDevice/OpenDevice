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

import java.util.UUID;


// @XmlRootElement
//@JsonFilter("PropertyFilterMixIn")
public class ResponseCommand extends Command {

    private static final long serialVersionUID = 1891204618345540528L;

    public ResponseCommand(CommandType type) {
        super(type);
    }

    public ResponseCommand() {
        super(CommandType.DEVICE_COMMAND_RESPONSE);
    }

    public ResponseCommand(CommandStatus status) {
        this(CommandType.DEVICE_COMMAND_RESPONSE, status, null);
    }

    public ResponseCommand(CommandStatus status, String connectionUUID) {
        this(CommandType.DEVICE_COMMAND_RESPONSE, status, connectionUUID);
    }

    public ResponseCommand(CommandType type, CommandStatus status) {
        this(type, status, null);
    }

    public ResponseCommand(CommandType type, CommandStatus status, String connectionUUID) {
        super(type, UUID.randomUUID().toString(), connectionUUID);
        this.setStatus(status);
    }

    @Override
    public String toString() {
        return "ResponseCommand[type=" + getType() + ", status=" + getStatus() + "]";
    }
}
