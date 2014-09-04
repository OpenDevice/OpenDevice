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
import br.com.criativasoft.opendevice.connection.serialize.DefaultSteamReader;
import br.com.criativasoft.opendevice.core.command.amarino.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

/**
 * Parser to convert byte[] into {@link Command}
 * @author Ricardo JL Rufino
 * @date 18/06/2014
 */
public class CommandStreamReader extends DefaultSteamReader {

    private Logger log = LoggerFactory.getLogger(CommandStreamReader.class);

    @Override
    protected boolean checkEndOfMessage(byte lastByte,ByteArrayOutputStream readBuffer) {
        return lastByte == MessageBuilder.ACK_FLAG;
    }

    public void processPacketRead(byte read[]){

        if(log.isTraceEnabled()) {
            log.trace("processPacketRead: " + new String(read) + ", size: " + read.length);
        }

        for (int i = 0; i < read.length; i++) {

            // Reads up to find an EOL.
            if (checkEndOfMessage(read[i], inputBuffer)) {
                byte[] array = inputBuffer.toByteArray();
                Message event = parse(array);
                notifyOnDataRead(event);
                inputBuffer.reset();

            }else{
                if(read[i] != MessageBuilder.ACK_FLAG && read[i] != MessageBuilder.START_FLAG)
                    inputBuffer.write(read[i]);
            }

        }
    }

}
