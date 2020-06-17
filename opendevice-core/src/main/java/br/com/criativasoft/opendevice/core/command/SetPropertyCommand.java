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

package br.com.criativasoft.opendevice.core.command;

import br.com.criativasoft.opendevice.core.util.TypeConverter;

/**
 * @author Ricardo JL Rufino
 * @date 11/01/16
 */
public class SetPropertyCommand extends Command {

    private int deviceID;

    private String property;

    private Object value;

    public SetPropertyCommand() {
        super(CommandType.SET_PROPERTY);
    }

    public SetPropertyCommand(int deviceID, String property, Object value) {
        super(CommandType.SET_PROPERTY);
        this.deviceID = deviceID;
        this.property = property;
        this.value = value;
    }

    public String getProperty() {
        return property;
    }

    public Object getValue() {
        return value;
    }

    public <T> T getValue(Class<T> klass) {
        return (T) TypeConverter.convert(klass, value);
    }

    public int getDeviceID() {
        return deviceID;
    }
}
