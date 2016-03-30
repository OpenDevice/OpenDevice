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

package br.com.criativasoft.opendevice.core.model.test;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 11/01/16
 */
public class Property {

    // private long id;

    private PropertyDef definition;

    private Object value;

    public Property() {

    }

    public Property(PropertyDef definition, Object value) {
        this.definition = definition;
        this.value = value;
    }

    public PropertyDef getDefinition() {
        return definition;
    }

    public void setDefinition(PropertyDef definition) {
        this.definition = definition;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getName(){
        return definition.getName();
    }
}
