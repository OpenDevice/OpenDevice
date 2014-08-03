/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.core.json;

import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.command.ResponseCommand;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: PENDING DOC
 *
 * @autor Ricardo JL Rufino
 * @date 27/07/14.
 */
public class CommandDeserialize extends StdDeserializer<Command> {

    private Map<CommandType, Class<? extends Command>> registry = new HashMap<CommandType, Class<? extends Command>>();

    protected CommandDeserialize() {
        super(Command.class);

        // Default Types.
        registry.put(CommandType.DEVICE_COMMAND_RESPONSE, ResponseCommand.class);
    }

    @Override
    public Command deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        ObjectNode root = (ObjectNode) mapper.readTree(jp);
        Class<? extends Command> commandClass = null;

        JsonNode jsonType = root.get("type");
        int type = jsonType.intValue();

        CommandType commandType = CommandType.getByCode(type);

        if(commandType == null) throw new IllegalArgumentException("type of command must be provided !");

        if(DeviceCommand.isCompatible(commandType)){
            return mapper.readValue(root.toString(), DeviceCommand.class);
        }else{

            Class<? extends Command> cmdClass = registry.get(commandType);

            if(cmdClass == null) {
                throw new IllegalArgumentException("Command type not supported!! You need configure in CommandDeserialize");
            }

            return mapper.readValue(root.toString(), cmdClass);

        }

    }

}
