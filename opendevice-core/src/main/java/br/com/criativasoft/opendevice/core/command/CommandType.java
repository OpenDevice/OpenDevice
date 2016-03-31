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

import br.com.criativasoft.opendevice.core.command.ext.IrCommand;
import br.com.criativasoft.opendevice.core.metamodel.EnumCode;

/**
 * Default command types of OpenDevice. Additional commands can be registered using {@link CommandRegistry}.
 * @author Ricardo JL Rufino
 * @date 04/09/2011 12:44:44
 */
public enum CommandType implements EnumCode {
    /** Indicates that the values are 0 or 1 (HIGH or LOW) */
    DIGITAL                 (1, DeviceCommand.class),
    ANALOG                  (2, DeviceCommand.class),
    NUMERIC                 (3, DeviceCommand.class),
    /** Commands sent directly to the pins (digitalWrite) */
    GPIO_DIGITAL            (4, null),
    /** Commands sent directly to the pins (analogWrite) */
    GPIO_ANALOG             (5, null),
    INFRA_RED               (6, IrCommand.class),

    /** Response to commands like: DIGITAL, ANALOG, INFRA RED */

    DEVICE_COMMAND_RESPONSE (10, ResponseCommand.class),
    COMMAND_RESPONSE        (11, null),
    SET_PROPERTY            (12, SetPropertyCommand.class),
    ACTION                  (13, ActionCommand.class),

    PING_REQUEST            (20, SimpleCommand.class),
    PING_RESPONSE           (21, ResponseCommand.class),
    DISCOVERY_REQUEST       (22, SimpleCommand.class),
    DISCOVERY_RESPONSE      (23, DiscoveryResponse.class),
    MEMORY_REPORT           (24, SimpleCommand.class), // Report the amount of memory (displays the current and maximum).
    CPU_TEMPERATURE_REPORT  (25, SimpleCommand.class),
    CPU_USAGE_REPORT        (26, SimpleCommand.class),

    GET_DEVICES             (30, GetDevicesRequest.class),
    GET_DEVICES_RESPONSE    (31, GetDevicesResponse.class),
    DEVICE_ADD 				(32, null), // NOT.IMPLEMENTED
    DEVICE_ADD_RESPONSE		(33, null), // NOT.IMPLEMENTED
    DEVICE_DEL 				(34, null), // NOT.IMPLEMENTED
    CLEAR_DEVICES 			(35, null), // NOT.IMPLEMENTED
    GET_CONNECTIONS 		(36, null), // NOT.IMPLEMENTED
    GET_CONNECTIONS_RESPONSE   (37, null), // NOT.IMPLEMENTED
    CONNECTION_ADD 			(38, AddConnection.class),
    CONNECTION_ADD_RESPONSE (39, AddConnectionResponse.class),
    CONNECTION_DEL 			(40, null), // NOT.IMPLEMENTED
    CLEAR_CONNECTIONS 		(41, null), // NOT.IMPLEMENTED

    USER_COMMAND(99, UserCommand.class);

    private int code;
    private Class<? extends Command> commandClass;

    /**
     * @param code - Device type code. MAX 127.
     */
    private CommandType(int code, Class<? extends Command> cmdClass) {
        this.code = (byte) code;
        this.commandClass = cmdClass;
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

        Class<? extends Command> commandClass = type.getCommandClass();

        return commandClass != null && commandClass == DeviceCommand.class;
    }

    public static boolean isSimpleCommand( CommandType type ) {
        if (type == null) return false;

        Class<? extends Command> commandClass = type.getCommandClass();

        return commandClass != null && commandClass == SimpleCommand.class;

    }

    public Class<? extends Command> getCommandClass() {
        return commandClass;
    }
}