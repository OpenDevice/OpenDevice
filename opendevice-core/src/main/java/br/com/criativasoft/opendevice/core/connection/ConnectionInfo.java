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

package br.com.criativasoft.opendevice.core.connection;

import java.util.Date;

/**
 * @author Ricardo JL Rufino
 * @date 18/10/16
 */
public class ConnectionInfo {


    private String uuid;

    private String type;

    private String host;

    private Date fistConnection;

    private Date lastConnection;

    public ConnectionInfo() {
    }

    public ConnectionInfo(String type, String host) {
        this.type = type;
        this.host = host;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }


    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    private String applicationID;

    public Date getFistConnection() {
        return fistConnection;
    }

    public void setFistConnection(Date fistConnection) {
        this.fistConnection = fistConnection;
    }

    public Date getLastConnection() {
        return lastConnection;
    }

    public void setLastConnection(Date lastConnection) {
        this.lastConnection = lastConnection;
    }
}
