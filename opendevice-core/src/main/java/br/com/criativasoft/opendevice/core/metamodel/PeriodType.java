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

public enum PeriodType{
        RECORDS(0),MINUTE(12), HOUR(11), DAY(5), WEEK(4), MONTH(2), YEAR(1);

        PeriodType(int value) {
            this.value = value;
        }

        int value;

        /** @return java.util.Calendar Value*/
        public int getValue() {
            return value;
        }
    }