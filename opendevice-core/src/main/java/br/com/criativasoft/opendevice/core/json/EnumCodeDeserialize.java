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

package br.com.criativasoft.opendevice.core.json;

import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.metamodel.EnumCode;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Jackson Converter to serialize enum as integer
 *
 * @author Ricardo JL Rufino
 * @date 08/07/14.
 */
public abstract class EnumCodeDeserialize<T extends EnumCode> extends JsonDeserializer<T> {

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        int code = jsonParser.getIntValue();
        return getByCode(code);
    }

    public abstract T getByCode(int code);


    static class CommandTypeDeserialize extends EnumCodeDeserialize<CommandType>{
        @Override
        public CommandType getByCode(int code) {
            return CommandType.getByCode(code);
        }
    }
}
