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
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.json.CommandJacksonMapper;
import br.com.criativasoft.opendevice.core.metamodel.CommandVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.wasync.Decoder;
import org.atmosphere.wasync.Encoder;
import org.atmosphere.wasync.Event;

import java.io.IOException;

/**
 * TODO: PENDING DOC
 *
 * @autor Ricardo JL Rufino
 * @date 11/07/14.
 */
public class CommandEncoderDecoder implements Encoder<Command, String>, Decoder<String, Command> {

    private CommandJacksonMapper mapper;


    public ObjectMapper getMapper() {
        if(mapper == null){
            mapper = new CommandJacksonMapper();
        }
        return mapper.getMapper();
    }



    @Override
    public Command decode(Event e, String s) {

        System.out.println("e = " + e + " -> " + s);

//        if(e == Event.MESSAGE){
//            try {
//                return getMapper().readValue(s, Command.class);
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//        }

        return null;
    }

    @Override
    public String encode(Command cmd) {

        CommandVO vo = new CommandVO();
        DeviceCommand deviceCommand = (DeviceCommand) cmd;
        vo.setDeviceID(deviceCommand.getDeviceID());
        vo.setValue(""+deviceCommand.getValue());
        vo.setType(deviceCommand.getType().getCode());

        try {
            return getMapper().writeValueAsString(vo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }



}
