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

package br.com.criativasoft.opendevice.wsrest.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.config.managed.Encoder;
import org.atmosphere.jersey.Broadcastable;

import java.io.IOException;

/**
 * Atmosphere JSON Response Encoder..
 *
 * @author Ricardo JL Rufino
 * @date 09/07/14.
 */
public class ResponseEncoder implements Encoder<Broadcastable, String> {

    private final ObjectMapper mapper;

    public ResponseEncoder() {
        mapper = new ObjectMapper();
    }

    @Override
    public String encode(Broadcastable s) {
        try {
            return mapper.writeValueAsString(s);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
