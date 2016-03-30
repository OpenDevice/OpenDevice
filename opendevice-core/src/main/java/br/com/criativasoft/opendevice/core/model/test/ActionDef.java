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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 13/01/16
 */
public class ActionDef {

    private String name;

    private int code; // for low-level protocol

    public ActionDef(String name, int code) {
        this.name = name;
        this.code = code;
    }

    private List<String> paramNames = new ArrayList<String>();

    private List<PropertyType> paramTypes = new ArrayList<PropertyType>();

    /**
     * Set parameters names for this action. </br>
     * Shortcut for: #setParamNames
     * @param params
     * @return
     */
    private ActionDef params(String ...params){
        this.paramNames.addAll(Arrays.asList(params));
        return this;
    }

    /**
     * Set parameters types for this action. </br>
     * Shortcut for: #setParamTypes
     * @param types
     * @return
     */
    private ActionDef types(PropertyType ...types){
        this.paramTypes.addAll(Arrays.asList(types));
        return this;
    }

    /**
     * @see #params(String...)
     * @param paramNames
     */
    public void setParamNames(List<String> paramNames) {
        this.paramNames = paramNames;
    }

    /**
     * @see #types(PropertyType...)
     * @param paramTypes
     */
    public void setParamTypes(List<PropertyType> paramTypes) {
        this.paramTypes = paramTypes;
    }

    public String getName() {
        return name;
    }

    public List<String> getParamNames() {
        return paramNames;
    }

    public List<PropertyType> getParamTypes() {
        return paramTypes;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o instanceof String && o.toString().equals(this.name)) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActionDef actionDef = (ActionDef) o;

        if (code != actionDef.code) return false;
        return name.equals(actionDef.name);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + code;
        return result;
    }
}
