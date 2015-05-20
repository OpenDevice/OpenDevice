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

package br.com.criativasoft.opendevice.core.metamodel;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 05/05/15.
 */
public class DeviceHistoryQuery {


    private int deviceID;
    private int periodValue;
    private PeriodType periodType;
    private AggregationType aggregation;

    public DeviceHistoryQuery(){

    }


    public DeviceHistoryQuery(int deviceID, PeriodType periodType, int periodValue) {
        this.periodType = periodType;
        this.deviceID = deviceID;
        this.periodValue = periodValue;
    }

    public int getPeriodValue() {
        return periodValue;
    }

    public void setPeriodValue(int periodValue) {
        this.periodValue = periodValue;
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public void setDeviceID(int deviceID) {
        this.deviceID = deviceID;
    }

    public int getDeviceID() {
        return deviceID;
    }

    public void setAggregation(AggregationType aggregation) {
        this.aggregation = aggregation;
    }

    public AggregationType getAggregation() {
        return aggregation;
    }
}
