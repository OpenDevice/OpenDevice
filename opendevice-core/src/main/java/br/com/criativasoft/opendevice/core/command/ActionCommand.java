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

import java.util.Arrays;
import java.util.List;

/**
 * FIXME: conflito de logica com o USAR ACTOIN, no novo protocolo: ver CommandType
 * @author Ricardo JL Rufino
 * @date 11/01/16
 */
public class ActionCommand extends Command {

    private int deviceID;

    private String action;

    private List<Object> params;

    public ActionCommand() {
        super(CommandType.ACTION);
    }

    public ActionCommand(int deviceID, String action, List<Object> params) {
        super(CommandType.ACTION);
        this.deviceID = deviceID;
        this.action = action;
        this.params = params;
    }

    public ActionCommand(int deviceID, String action, Object... params) {
        super(CommandType.ACTION);
        this.deviceID = deviceID;
        this.action = action;
        this.params = Arrays.asList(params);
    }

    public String getAction() {
        return action;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    public <T> T getParam(int index, Class<T> klass) {
        if(params == null) return null;
        return TypeConverter.convert(klass, params.get(index));
    }


    public int getDeviceID() {
        return deviceID;
    }
}
