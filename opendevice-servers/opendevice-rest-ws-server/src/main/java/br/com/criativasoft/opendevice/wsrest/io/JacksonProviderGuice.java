/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.wsrest.io;


import br.com.criativasoft.opendevice.core.json.CommandJacksonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Provider;

/**
 * Bind default mapper to {@link JacksonProvider}.
 * @author Ricardo JL Rufino
 * @date 05/11/16
 */
public class JacksonProviderGuice implements Provider<ObjectMapper> {

    private final ObjectMapper mapper;

    public JacksonProviderGuice() {
        mapper = new CommandJacksonMapper().getMapper();
//        SimpleModule module = new SimpleModule("ODev-Rest", new Version(0, 1, 0, "alpha"));
//        module.addSerializer(ErrorResponse.ErrorMessage.class, new HttpResponseSerializer());
//        mapper.registerModule(module);
    }


    @Override
    public ObjectMapper get() {

        return mapper;
    }
}
