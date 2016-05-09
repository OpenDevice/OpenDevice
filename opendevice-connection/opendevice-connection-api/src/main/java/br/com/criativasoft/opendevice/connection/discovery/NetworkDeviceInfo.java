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

package br.com.criativasoft.opendevice.connection.discovery;

import java.io.Serializable;

/**
 * @author Ricardo JL Rufino
 * @date 05/11/15
 */
public class NetworkDeviceInfo implements Serializable {


    private String name;

    private String ip;

    private int type;

    private int deviceLength;

    private int port;

    public NetworkDeviceInfo() {

    }

    public NetworkDeviceInfo(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setDeviceLength(int deviceLength) {
        this.deviceLength = deviceLength;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getDeviceLength() {
        return deviceLength;
    }

    public int getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NetworkDeviceInfo that = (NetworkDeviceInfo) o;

        if (!name.equals(that.name)) return false;
        return !(ip != null ? !ip.equals(that.ip) : that.ip != null);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NetworkDeviceInfo[name="+getName()+", ip="+getIp()+", port="+getPort()+"]";
    }
}
