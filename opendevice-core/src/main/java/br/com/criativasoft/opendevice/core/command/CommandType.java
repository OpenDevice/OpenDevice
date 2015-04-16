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

import br.com.criativasoft.opendevice.core.metamodel.EnumCode;

/**
 * Represento um tipo de Comando
 * @author Ricardo JL Rufino
 * @date 04/09/2011 12:44:44
 */
public enum CommandType implements EnumCode {
    /** Indicates that the values are 0 or 1 (HIGH or LOW) */
    DIGITAL(1),
    ANALOG(2),
    ANALOG_REPORT(3),
    /** Commands sent directly to the pins (digitalWrite) */
    GPIO_DIGITAL(4),
    /** Commands sent directly to the pins (analogWrite) */
    GPIO_ANALOG(5),
    PWM(6),
    INFRA_RED(7),
    
    /** Response to commands like: DIGITAL, POWER_LEVEL, INFRA RED */
    DEVICE_COMMAND_RESPONSE(10),

    PING(20),
    PING_RESPONSE(21),
    MEMORY_REPORT(22), // Report the amount of memory (displays the current and maximum).
    CPU_TEMPERATURE_REPORT(23),
    CPU_USAGE_REPORT(24),

    GET_DEVICES(30),
    GET_DEVICES_RESPONSE(31),
    USER_COMMAND(99),
    ;

    private int code;

    /**
     * @param code - Device type code. MAX 127.
     */
    private CommandType(int code) {
        this.code = (byte) code;
    }

    public int getCode() {
        return code;
    }

    public static CommandType getByCode( int code ) {
        CommandType[] values = CommandType.values();
        for (CommandType commandType : values) {
            if (commandType.getCode() == code) {
                return commandType;
            }
        }

        return null;
    }

    public static final boolean isDeviceCommand( CommandType type ) {

        if (type == null) return false;

        switch (type) {
        case DIGITAL:
            return true;
        case ANALOG:
            return true;
        case ANALOG_REPORT:
            return true;
        default:
            break;
        }

        return false;
    }

    public static boolean isSimpleCommand( CommandType type ) {
        if (type == null) return false;

        switch (type) {
        case PING:
            return true;
        case PING_RESPONSE:
            return true;
        default:
            break;
        }

        return false;
    }

}