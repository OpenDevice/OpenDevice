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

package br.com.criativasoft.opendevice.core.util;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 15/01/16
 */
public class TypeConverter {

    private TypeConverter() {
    }

    public static <T> T convert(Class<T> klass, Object value) {

        if (value == null) return null;

        if (klass.isAssignableFrom(value.getClass())) {
            return (T) value;
        }

        if (value instanceof String) {

            if (klass == Integer.class) {
                return (T) new Integer(value.toString());
            }

            if (klass == Double.class) {
                return (T) new Double(value.toString());
            }

            if (klass == Boolean.class) {
                return (T) new Boolean(value.toString());
            }
        }

        if (value instanceof Integer && klass == Boolean.class) {
            return (T) new Boolean((1 == ((Integer) value).intValue()));

        }

        return null;
    }
}
