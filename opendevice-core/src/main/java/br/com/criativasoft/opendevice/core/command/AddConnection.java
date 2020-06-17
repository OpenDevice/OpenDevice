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

import br.com.criativasoft.opendevice.core.connection.ConnectionType;

/**
 * @author Ricardo JL Rufino
 * @date 02/11/15
 */
public class AddConnection extends ConnectionCommand implements ExtendedCommand {

    private ConnectionType type;

    private String uri;

    private String password;

    public AddConnection(ConnectionType type, String uri, String password) {
        super(CommandType.CONNECTION_ADD);
        setTimeout(30000);
        this.type = type;
        this.uri = uri;
        this.password = password;
    }

    public ConnectionType getConnectionType() {
        return type;
    }

    public String getUri() {
        return uri;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void deserializeExtraData(String extradata) {

    }

    /**
     * Format: /type/id/ctype/uri/passwd
     */
    @Override
    public String serializeExtraData() {
        return String.valueOf(getConnectionType().getCode()) + Command.DELIMITER + uri + Command.DELIMITER + password;
    }
}
