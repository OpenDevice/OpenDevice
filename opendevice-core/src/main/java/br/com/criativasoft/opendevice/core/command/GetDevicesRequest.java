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
import br.com.criativasoft.opendevice.connection.message.Request;

/**
 * TODO: PENDING DOC
 *
 * @author ricardo
 * @date 28/06/14.
 */
public class GetDevicesRequest extends SimpleCommand implements Request {

    public static final int FILTER_BY_ID = 1;

    private int filter = -1;
    private Object filterValue;
    private boolean forceSync = false;

    public GetDevicesRequest(long filterByValue) {
        super(CommandType.GET_DEVICES, filterByValue);
    }

    /**
     * @param filter Filter type, like {@link #FILTER_BY_ID}
     * @param filterValue
     */
    public GetDevicesRequest(int filter, Object filterValue) {
        this(filter, filterValue, false);
    }

    public GetDevicesRequest(int filter, Object filterValue, boolean forceSync) {
        super(CommandType.GET_DEVICES, 0);
        this.filter = filter;
        this.filterValue = filterValue;
        this.forceSync = forceSync;
    }

    public GetDevicesRequest() {
        super(CommandType.GET_DEVICES, 0);
    }

    @Override
    public Class<? extends Message> getResponseType() {
        return GetDevicesResponse.class;
    }


    public int getFilter() {
        return filter;
    }

    public Object getFilterValue() {
        return filterValue;
    }

    public void setForceSync(boolean forceSync) {
        this.forceSync = forceSync;
    }

    public boolean isForceSync() {
        return forceSync;
    }

}

