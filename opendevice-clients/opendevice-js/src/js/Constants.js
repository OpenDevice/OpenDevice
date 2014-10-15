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

var od = od || {};

// Like OpenDevice JAVA-API
od.DeviceType = {
    ANALOG:1,
    DIGITAL:2
};

// Like OpenDevice JAVA-API
od.DeviceCategory = {
    LAMP:1,
    FAN:2,
    GENERIC:3,
    POWER_SOURCE : 4,
    GENERIC_SENSOR: 50,
    IR_SENSOR: 51
};

od.CommandType = {
    ON_OFF:1,
    ANALOG:2,
    ANALOG_REPORT:3,
    GPIO_DIGITAL:4, // Controle a nivel logico do PINO (diferente do ON_OFF que pode ligar/desligar varios pinos)
    GPIO_ANALOG:5, // Controle de baixo nivel

    /** Response to commands like: ON_OFF, POWER_LEVEL, INFRA RED  */
    DEVICE_COMMAND_RESPONSE : 10, // Responsta para comandos como: ON_OFF, POWER_LEVEL, INFRA_RED

    PING_REQUEST : 20, // Verificar se esta ativo
    PING_RESPONSE : 21, // Resposta para o Ping
    MEMORY_REPORT : 22, // Relatorio de quantidade de memória (repora o atual e o máximo).
    CPU_TEMPERATURE_REPORT : 23,
    CPU_USAGE_REPORT:24,
    GET_DEVICES : 30,
    GET_DEVICES_RESPONSE : 31,

    isDeviceCommand : function(type){
        switch (type) {
            case this.ON_OFF:
                return true;
            case this.ANALOG:
                return true;
            case this.ANALOG_REPORT:
                return true;
            default:
                break;
        }
        return false;
    }
};


od.Event = {
    DEVICE_LIST_UPDATE : "DEVICE_LIST_UPDATE",
    DEVICE_CHANGED : "DEVICE_CHANGED",
    CONNECTION_CHANGE : "CONNECTION_CHANGE",
    CONNECTED : "CONNECTION_CHANGE_CONNECTED",
    DISCONNECTED : "CONNECTION_CHANGE_DISCONNECTED"
};