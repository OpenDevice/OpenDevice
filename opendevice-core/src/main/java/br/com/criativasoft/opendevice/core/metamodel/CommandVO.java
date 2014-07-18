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

package br.com.criativasoft.opendevice.core.metamodel;

/**
 * Plain representation of {@link br.com.criativasoft.opendevice.core.command.Command} class to be serialized
 * @autor Ricardo JL Rufino
 * @date 11/07/14.
 */
public class CommandVO {


    public CommandVO() {
    }

    public CommandVO(int type, int deviceID, String value, String connectionUUID) {
        this.type = type;
        this.deviceID = deviceID;
        this.value = value;
        this.connectionUUID = connectionUUID;
    }

    public CommandVO(int type, int deviceID, String value) {
        this.type = type;
        this.deviceID = deviceID;
        this.value = value;
    }

    private int type;

    private int deviceID;

    private String value;

    private String connectionUUID;

    public void setConnectionUUID(String connectionUUID) {
        this.connectionUUID = connectionUUID;
    }

    public String getConnectionUUID() {
        return connectionUUID;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public int getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(int deviceID) {
        this.deviceID = deviceID;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
