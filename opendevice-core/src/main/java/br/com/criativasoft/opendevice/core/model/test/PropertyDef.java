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

import java.util.List;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 11/01/16
 */
public class PropertyDef {

    public static String GROUP_GENERAL = "General";

    public enum Mode{
        READ_WRITE, WRITE_ONLY, READ_ONLY
    }

    private String name;

    private String group = GROUP_GENERAL;

    private Mode mode = Mode.READ_WRITE;

    private int code; // for low-level protocol

    private PropertyType type; // full name java type

    private List<String> allowedValues;

    private String allowedTypes; // range, list, spinner

    private boolean required = false;

    public PropertyDef(String name, int code) {
        this.name = name;
        this.code = code;
        this.type = PropertyType.STRING;
    }

    public PropertyDef(String name, Mode mode, int code, PropertyType type) {
        this.name = name;
        this.mode = mode;
        this.code = code;
        this.type = type;
    }

    public PropertyDef name(String name) {
        this.name = name;
        return this;
    }

    public PropertyDef group(String group) {
        this.group = group;
        return this;
    }

    public PropertyDef mode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public PropertyDef code(int code) {
        this.code = code;
        return this;
    }

    public PropertyDef type(PropertyType type) {
        this.type = type;
        return this;
    }

    public PropertyDef allowedValues(List<String> allowedValues) {
        this.allowedValues = allowedValues;
        return this;
    }

    public PropertyDef allowedTypes(String allowedTypes) {
        this.allowedTypes = allowedTypes;
        return this;
    }

    public PropertyDef required(boolean required) {
        this.required = required;
        return this;
    }

    public String getName() {
        return name;
    }

    public Mode getMode() {
        return mode;
    }

    public int getCode() {
        return code;
    }

    public PropertyType getType() {
        return type;
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }

    public String getAllowedTypes() {
        return allowedTypes;
    }

    public boolean isRequired() {
        return required;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return "PropertyDef[name:"+name+", type:"+type.name()+"]";
    }
}
