/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.core.command;

/**
 * Reponse for {@link AddConnection}, Format: /type/id/status/ip
 *
 * @author Ricardo JL Rufino
 * @date 02/11/15
 */
public class AddConnectionResponse extends ResponseCommand implements ExtendedCommand {

    private String ip;

    public AddConnectionResponse() {
        super(CommandType.CONNECTION_ADD_RESPONSE);
    }

    public String getIP() {
        return ip;
    }

    @Override
    public void deserializeExtraData(String extradata) {

        String data[] = extradata.split(Command.DELIMITER);

        setStatus(CommandStatus.getByCode(Integer.parseInt(data[0])));

        if (getStatus() == CommandStatus.SUCCESS) {
            ip = data[1];
        }

    }

    @Override
    public String serializeExtraData() {
        throw new IllegalStateException("Not Implemented"); // At this time , this is not required. !
    }
}
