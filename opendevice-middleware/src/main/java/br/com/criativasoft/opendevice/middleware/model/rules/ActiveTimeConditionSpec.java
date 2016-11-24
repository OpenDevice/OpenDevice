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

package br.com.criativasoft.opendevice.middleware.model.rules;

import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.persistence.Entity;

/**
 * @author Ricardo JL Rufino
 * @date 02/11/16
 */
@Entity
@JsonTypeName(value = "activeTime")
public class ActiveTimeConditionSpec extends ConditionSpec {

    private int time;

    private RuleEnums.IntervalType intervalType;

    public RuleEnums.IntervalType getIntervalType() {
        return intervalType;
    }

    public void setIntervalType(RuleEnums.IntervalType intervalType) {
        this.intervalType = intervalType;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
