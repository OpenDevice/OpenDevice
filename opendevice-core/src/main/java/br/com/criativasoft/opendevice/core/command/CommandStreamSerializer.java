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

import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.SimpleMessage;
import br.com.criativasoft.opendevice.connection.serialize.MessageSerializer;
import br.com.criativasoft.opendevice.core.command.ext.IrCommand;
import br.com.criativasoft.opendevice.core.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class CommandStreamSerializer implements MessageSerializer {

    private static Logger log = LoggerFactory.getLogger(CommandStreamSerializer.class);

    private boolean sendTerminator = true;

    public void setSendTerminator(boolean sendTerminator) {
        this.sendTerminator = sendTerminator;
    }

    @Override
    public Message parse(byte[] pkg) {

        String cmd = new String(pkg);

        if (cmd.length() == 0) {
            log.trace("empty message received !");
            return null;
        }

        if (cmd.startsWith("DB:")) return new SimpleMessage(cmd);

        // Easy for split (start == delimitar)
        if (cmd.startsWith(Command.DELIMITER)) {
            cmd = cmd.replaceFirst("/", "");
        }

        // Remove ACK_FLAG if exist
        if (cmd.endsWith(String.valueOf(Command.ACK_FLAG))) {
            cmd = cmd.replaceFirst(String.valueOf(Command.ACK_FLAG), "");
        }

        String[] split = cmd.split(Command.DELIMITER);

        int ctype = Integer.parseInt(split[0]);
        int id = Integer.parseInt(split[1]);

        Command command = null;

        CommandType type = CommandType.getByCode(ctype);

        // Check from Plugins/Extensions
        if (type == null) {
            command = CommandRegistry.getCommand(ctype);
            assert command != null;
            type = command.getType();
        }

        if (DeviceCommand.isCompatible(type)) {

            int deviceID = Integer.parseInt(split[2]);
            double value = Double.parseDouble(split[3]);
            command = new DeviceCommand(type, deviceID, value);

        } else if (SimpleCommand.isCompatible(type)) {

            long value = Long.parseLong(split[3]);

            command = new SimpleCommand(type, value);
            // Simple Response
        } else if (ResponseCommand.class.equals(type.getCommandClass())) {

            int value = Integer.parseInt(split[3]);

            command = new ResponseCommand(type, CommandStatus.getByCode(value));


            // Some clases that extend SimpleCommand
        } else if (SimpleCommand.class.isAssignableFrom(type.getCommandClass())) {

            try {

                Constructor<? extends Command> constructor = type.getCommandClass().getConstructor(long.class);

                long value = Long.parseLong((split.length == 4 ? split[3] : "0"));

                command = constructor.newInstance(value);

            } catch (Exception e) {
            }

            // Format: INFRA_RED;ID;VALUE;IR_PROTOCOL;LENGTH?;BYTE1...BYTEX?
            // If IR_PROTOCOL is RAW, 'LENGTH' and BYTE ARRAY EXIST.
        } else if (type == CommandType.INFRA_RED) {

            int deviceID = Integer.parseInt(split[2]);
            long value = Long.parseLong(split[3]);
            command = new IrCommand(deviceID, value);

        } else if (type == CommandType.DEVICE_COMMAND_RESPONSE) {

            int status = Integer.parseInt(split[3]);    // Command.value

            command = new ResponseCommand(CommandStatus.getByCode(status));

        } else if (type == CommandType.GET_DEVICES) { // Returned list of devices.

            command = new GetDevicesRequest();

            // Format: GET_DEVICES_RESPONSE;ID;Length;[ID, PIN, VALUE, TARGET, SENSOR?, TYPE];[ID,PIN,VALUE,...];[ID,PIN,VALUE,...]

        } else if (type == CommandType.GET_DEVICES_RESPONSE) { // Returned list of devices.
            String reqID = split[1];
            List<Device> devices = new LinkedList<Device>();
            command = new GetDevicesResponse(devices, reqID);


        } else if (type == CommandType.USER_COMMAND) { // Returned list of devices.

            command = new UserCommand(split[2]);

            // Custom Response (this must be in te end.)
        } else if (ResponseCommand.class.isAssignableFrom(type.getCommandClass())) {

            try {

                command = type.getCommandClass().newInstance();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if (command == null) throw new CommandException("Can't parse command type : " + type + ", cmd: " + cmd);

        if (command instanceof ExtendedCommand) {
            ExtendedCommand extendedCommand = (ExtendedCommand) command;

            // Skip type and id
            int indexOf = cmd.indexOf(Command.DELIMITER, 1);
            indexOf = cmd.indexOf(Command.DELIMITER, indexOf + 1);

            if (cmd.length() > indexOf + 1) {
                String extradata = cmd.substring(indexOf + 1);
                if (extradata.length() > 0) {
                    extendedCommand.deserializeExtraData(extradata);
                }
            }

        }

        command.setTrackingID(id);
        command.setTimestamp(new Date());

        return command;

    }

    @Override
    public byte[] serialize(Message message) {

        Command command = (Command) message;
        StringBuilder sb = new StringBuilder();
        sb.append(Command.START_FLAG);
        sb.append(command.getType().getCode());
        sb.append(Command.DELIMITER_FLAG);
        sb.append((command.getUid() != null ? command.getTrackingID() : 0));

        if (CommandType.isDeviceCommand(command.getType())) {
            sb.append(Command.DELIMITER_FLAG);
            sb.append(((DeviceCommand) command).getDeviceID());
            sb.append(Command.DELIMITER_FLAG);
            sb.append(((DeviceCommand) command).getValue());
        } else if (command instanceof SimpleCommand) {
            sb.append(Command.DELIMITER_FLAG);
            sb.append(((SimpleCommand) command).getValue());
        } else if (command instanceof ExtendedCommand) {
            ExtendedCommand extra = (ExtendedCommand) command;
            String extraData = extra.serializeExtraData();
            if (extraData != null) {
                sb.append(Command.DELIMITER_FLAG);
                sb.append(extraData);
            }
        } else {
            sb.append(0);
            sb.append(Command.DELIMITER_FLAG);
            sb.append(0);
        }

        if (sendTerminator) sb.append(Command.ACK_FLAG);

        if (log.isTraceEnabled()) log.trace("serializing: " + sb);

        return sb.toString().getBytes();
    }


}
