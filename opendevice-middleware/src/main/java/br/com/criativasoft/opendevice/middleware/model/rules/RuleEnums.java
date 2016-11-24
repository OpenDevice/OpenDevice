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

/**
 * @author Ricardo JL Rufino
 * @date 31/10/16
 */
public class RuleEnums {

//    enum RuleType {
//        STATE,
//    }

    public enum DeviceState {
        ON(1),
        OFF(0);

        int value;

        DeviceState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum ThresholdType {
        EQUALS,
        NOT_EQUALS,
        GREATER_THAN,
        LESS_THAN,
        BETWEEN;
    }

    public enum ThresholdTarget {
        VALUE,
        OTHER_DEVICE;
    }

    public enum IntervalType {
        SECOND,
        MINUTE,
        HOUR
    }

    public enum ExecutionStatus {
        INACTIVE,
        ACTIVE,
        FAIL
    }

}
