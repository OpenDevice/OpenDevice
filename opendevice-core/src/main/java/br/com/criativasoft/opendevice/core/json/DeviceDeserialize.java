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

package br.com.criativasoft.opendevice.core.json;

import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 19/10/16
 */
public class DeviceDeserialize extends JsonDeserializer<Device> {
    private static final Logger log = LoggerFactory.getLogger(DeviceDeserialize.class);

    @Override
    public Device deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        ObjectNode root = (ObjectNode) mapper.readTree(jp);

        JsonNode jsonType = root.get("type");

        DeviceType type = DeviceType.getByCode(jsonType.intValue());

        Class<? extends Device> beanClass = null;

        if (type != null) {
            beanClass = type.getKlass();
        }

        if (beanClass == null) {
            log.debug("Using default class for device type = " + type);
            beanClass = Device.class;
        }

        return mapper.readValue(root.toString(), beanClass);
    }
}
