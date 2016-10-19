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

package br.com.criativasoft.opendevice.webclient.io;

import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.CommandStatus;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.ResponseCommand;
import br.com.criativasoft.opendevice.core.json.CommandJacksonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.wasync.Decoder;
import org.atmosphere.wasync.Encoder;
import org.atmosphere.wasync.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * TODO: PENDING DOC
 *
 * @author Ricardo JL Rufino
 * @date 11/07/14.
 */
public class CommandEncoderDecoder implements Encoder<Command, String>, Decoder<String, Command> {

    private static Logger log = LoggerFactory.getLogger(CommandEncoderDecoder.class);

    private CommandJacksonMapper mapper;

    public ObjectMapper getMapper() {
        if(mapper == null){
            mapper = new CommandJacksonMapper();
        }
        return mapper.getMapper();
    }



    @Override
    public Command decode(Event e, String s) {

        if(log.isDebugEnabled()) log.debug( e + " -> " + s);

        if(e == Event.MESSAGE){

            if("X".equalsIgnoreCase(s)) return null; // ping send from WebSocketServer

            // Note: If change this logic if future, also change in DeviceConnection.js:186
            if(s.contains("Authorization Required") || s.contains("Unauthorized") || s.contains("Invalid AuthToken")) {
                log.info("Fail on authentication, response: " + s);
                ResponseCommand response = new ResponseCommand(CommandType.CONNECT_RESPONSE,  CommandStatus.UNAUTHORIZED);
                return response;
            }

            try {
                Command command = getMapper().readValue(s, Command.class);
                return command;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public String encode(Command cmd) {

        try {
            return getMapper().writeValueAsString(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }



}
