/*
 *
 *  * ******************************************************************************
 *  *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  *  All rights reserved. This program and the accompanying materials
 *  *  are made available under the terms of the Eclipse Public License v1.0
 *  *  which accompanies this distribution, and is available at
 *  *  http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  *  Contributors:
 *  *  Ricardo JL Rufino - Initial API and Implementation
 *  * *****************************************************************************
 *
 */

package br.com.criativasoft.opendevice.core.model;

/**
 * OpenDevice configuration.
 *
 * @author Ricardo JL Rufino on 23/03/15.
 */
public class OpenDeviceConfig {

    public static final String LOCAL_APP_ID = "*";

    public static final int DEFAULT_PORT = 8181;

    private static OpenDeviceConfig INSTANCE;

    private int port = DEFAULT_PORT;

    private boolean tenantsEnabled = false;

    private boolean broadcastInputs = true;

    private boolean databaseEnabled = false;

    private String databasePath = "/data";

    private boolean mqttEnabled = true;

    private boolean authRequired = false;

    private String certificateFile;

    private String certificateKey;

    private String certificatePass;

    private OpenDeviceConfig(){
    }

    /**
     * Checks whether the support for multi-tenant is active. This setting should only be enabled when using the platform as a service.
     * @return if multi-tenant is active
     */
    public boolean isTenantsEnabled() {
        return tenantsEnabled;
    }

    public void setTenantsEnabled(boolean tenantsEnabled) {
        this.tenantsEnabled = tenantsEnabled;
    }

    public static OpenDeviceConfig get(){
        if(INSTANCE == null){
            INSTANCE = new OpenDeviceConfig();
        }

        return INSTANCE;
    }

    /**
     * Sets whether to make the broadcast of a command {@link br.com.criativasoft.opendevice.core.command.DeviceCommand} received by an incoming connection to the other incoming connections
     * @param broadcastInputs (Default = true)
     */
    public void setBroadcastInputs(boolean broadcastInputs) {
        this.broadcastInputs = broadcastInputs;
    }

    public boolean isBroadcastInputs() {
        return broadcastInputs;
    }

    public void setDatabaseEnabled(boolean databaseEnabled) {
        this.databaseEnabled = databaseEnabled;
    }

    public boolean isDatabaseEnabled() {
        return databaseEnabled;
    }

    /**
     * Set certificate file to enable SSL support over (MQTT, HTTP, REST, WebSocket)
     */
    public void setCertificateFile(String certificateFile) {
        this.certificateFile = certificateFile;
    }

    public void setCertificateKey(String certificateKey) {
        this.certificateKey = certificateKey;
    }

    public void setCertificatePass(String certificatePass) {
        this.certificatePass = certificatePass;
    }

    public String getCertificateFile() {
        return certificateFile;
    }

    public String getCertificateKey() {
        return certificateKey;
    }

    public String getCertificatePass() {
        return certificatePass;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setAuthRequired(boolean authRequired) {
        this.authRequired = authRequired;
    }

    public boolean isAuthRequired() {
        return authRequired;
    }

    public void setMqttEnabled(boolean mqttEnabled) {
        this.mqttEnabled = mqttEnabled;
    }

    public boolean isMqttEnabled() {
        return mqttEnabled;
    }

    public void setDatabasePath(String databasePath) {
        this.databasePath = databasePath;
    }

    public String getDatabasePath() {
        return databasePath;
    }
}
