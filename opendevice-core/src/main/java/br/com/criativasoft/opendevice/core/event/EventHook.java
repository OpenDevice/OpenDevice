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

package br.com.criativasoft.opendevice.core.event;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino
 * @date 23/08/15.
 */
public class EventHook {

    private long id;

    private String name;

    private String description;

    private List<Integer> deviceIDs = new ArrayList<Integer>();

    private String handler;

    /**
     * JavaScript, JavaScript-ServerSide , Python, Shell, JavaClass
     */
    private String type;

//    /**
//     * Time interval: 1ms, 2sec, 3min
//     */
//    private int timeInterval;
//
//    private PeriodType period;


    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDeviceIDs(List<Integer> deviceIDs) {
        this.deviceIDs = deviceIDs;
    }

    public List<Integer> getDeviceIDs() {
        return deviceIDs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
