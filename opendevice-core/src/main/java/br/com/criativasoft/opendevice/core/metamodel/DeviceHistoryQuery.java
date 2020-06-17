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

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 05/05/15.
 */
public class DeviceHistoryQuery {

    private long deviceID;
    private int deviceUID;
    private int periodValue;
    private PeriodType periodType;
    private AggregationType aggregation;
    private int maxResults;
    private int pageNumber;
    private OrderType order;

    @JsonFormat(pattern = "dd/MM/yy HH:mm", timezone = "America/Bahia")
    private Date periodEnd;

    public DeviceHistoryQuery() {

    }

    public DeviceHistoryQuery(long deviceID, PeriodType periodType, int periodValue) {
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

    public void setDeviceID(long deviceID) {
        this.deviceID = deviceID;
    }

    public long getDeviceID() {
        return deviceID;
    }

    public void setAggregation(AggregationType aggregation) {
        this.aggregation = aggregation;
    }

    public AggregationType getAggregation() {
        return aggregation;
    }

    public void setDeviceUID(int deviceUID) {
        this.deviceUID = deviceUID;
    }

    public int getDeviceUID() {
        return deviceUID;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public int getMaxResults(int defaultValue) {
        if (maxResults <= 0) return defaultValue;
        return maxResults;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setOrder(OrderType order) {
        this.order = order;
    }

    public OrderType getOrder() {
        return order;
    }
}
