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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Parser to convert byte[] into {@link Command}
 *
 * @author Ricardo JL Rufino
 * @date 18/06/2014
 */
public class CommandStreamReader extends DefaultSteamReader {

    private Logger log = LoggerFactory.getLogger(CommandStreamReader.class);
    boolean processing = false;

    @Override
    protected boolean checkEndOfMessage(byte lastByte, ByteArrayOutputStream readBuffer) {
        return lastByte == Command.ACK_FLAG || lastByte == '\n';
    }

    public void processPacketRead(byte read[], int length) {

        if (log.isTraceEnabled()) {
            log.trace("processPacketRead: " + new String(Arrays.copyOf(read, length - 1)) + ", size: " + length);
        }

        for (int i = 0; i < length; i++) {

            // NOTE: Start bit is equals to the SEPARATOR
            if (read[i] == Command.START_FLAG && !processing) {
                processing = true;
            } else if (checkEndOfMessage(read[i], inputBuffer)) {

                byte[] array = inputBuffer.toByteArray();

                Message event = parse(array);
                if (event != null) {
                    notifyOnDataRead(event);
                }
                inputBuffer.reset();
                processing = false;

            } else if (processing) {
                inputBuffer.write(read[i]);
            }

        }
    }

}
