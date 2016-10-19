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

import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.model.DeviceCategory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 19/10/16
 */
public class DeviceCategoryDeserialize  extends JsonDeserializer<DeviceCategory> {
    @Override
    public DeviceCategory deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        int code = jp.getIntValue();

        return BaseDeviceManager.getInstance().getCategory(code);
    }
}
