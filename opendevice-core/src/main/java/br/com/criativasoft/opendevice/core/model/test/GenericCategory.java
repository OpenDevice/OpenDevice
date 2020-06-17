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

import br.com.criativasoft.opendevice.core.model.DeviceCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 13/01/16
 */
public abstract class GenericCategory extends DeviceCategory {

    private List<PropertyDef> properties = new ArrayList<PropertyDef>();

    private List<ActionDef> actions = new ArrayList<ActionDef>();

    public abstract void loadProperties();

    public PropertyDef add(String name, String group) {
        int code = nextCode();
        PropertyDef def = new PropertyDef(name, code).group(group);
        properties.add(def);
        return def;
    }

    public PropertyDef add(String name) {
        return add(name, PropertyDef.GROUP_GENERAL);
    }

    public ActionDef action(String name) {
        int code = nextActionCode();
        ActionDef def = new ActionDef(name, code);
        actions.add(def);
        return def;
    }

    public int nextCode() {
        return actions.size() + 1;
    }

    public int nextActionCode() {
        return actions.size() + 1;
    }

    public PropertyDef getProperty(int code) {
        for (PropertyDef property : properties) {
            if (property.getCode() == code) {
                return property;
            }
        }

        return null;
    }

    public PropertyDef getProperty(String property) {
        for (PropertyDef def : properties) {
            if (def.getName().equals(property)) {
                return def;
            }
        }

        return null;
    }

    public ActionDef getAction(String action) {
        for (ActionDef def : actions) {
            if (def.getName().equals(action)) {
                return def;
            }
        }

        return null;
    }

    public List<PropertyDef> getProperties() {
        return properties;
    }

    public List<ActionDef> getActions() {
        return actions;
    }
}
