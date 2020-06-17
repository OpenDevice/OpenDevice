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

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceCategory;
import br.com.criativasoft.opendevice.core.model.DeviceType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO: Add docs.
 * // TODO: lembar de mudar o DeviceVO
 *
 * @author Ricardo JL Rufino
 * @date 13/01/16
 */
public class GenericDevice extends Device {

    private List<Property> properties = new ArrayList<Property>();

    private List<PropertyChangeListener> propertyListeners = new LinkedList<PropertyChangeListener>();
    private List<ActionCommandListener> actionListeners = new LinkedList<ActionCommandListener>();

    private DeviceConnection connection;

    // FIXME worng
    public GenericDevice(int uid, Object o, DeviceType digital, DeviceCategory generic) {
        super(uid, null, DeviceType.DIGITAL, DeviceCategory.GENERIC);
    }

    public void execute(String action, Object param) {
        ActionDef def = getCategory().getAction(action);
        if (def == null) throw new IllegalArgumentException("Action '" + action + "' not defined in " + getCategory());
        execute(def, param);
    }

    public void execute(ActionDef action, Object param) {
        List<Object> params = new ArrayList<Object>();
        if (param != null) params.add(param);
        notityActionExecution(action, params);
    }

    public void setProperty(String property, Object value) {
        PropertyDef def = getCategory().getProperty(property);
        setProperty(def.getCode(), value);
    }

    public void setProperty(int propertyCode, Object value) {

        Property property = getProperty(propertyCode);

        // not found, but exist in specification
        if (property == null) {
            PropertyDef def = getCategory().getProperty(propertyCode);
            if (def != null) {
                Property newProp = new Property();
                newProp.setDefinition(def);
                properties.add(newProp);
                // FIXME: persist, or notify.
            }
        }

        if (property != null) {
            if (value != null && value.equals(property.getValue())) {
                property.setValue(value);
                notityPropertyChange(property);
            }

        }

    }

    public Property getProperty(int code) {

        for (Property property : properties) {
            if (property.getDefinition().getCode() == code) {
                return property;
            }
        }

        return null;
    }

    public Property getProperty(PropertyDef def) {

        for (Property property : properties) {
            if (property.getDefinition().getCode() == def.getCode()) {
                return property;
            }
        }

        return null;
    }


    // FIXME: mover logica de notificação para BaseDeviceManager..  para evitar bloqueios
    public void notityPropertyChange(Property property) {
        for (PropertyChangeListener listener : propertyListeners) {
            listener.onPropertyChange(this, property);
        }
    }

    // FIXME: mover logica de notificação para BaseDeviceManager.. para evitar bloqueios
    public void notityActionExecution(ActionDef action, List<Object> params) {
        for (ActionCommandListener listener : actionListeners) {
            listener.onExecuteAction(this, action, params);
        }
    }

    @Override
    public GenericCategory getCategory() {
        return (GenericCategory) super.getCategory();
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void addProperty(Property property) {
        properties.add(property);
    }

    public void setConnection(DeviceConnection connection) {
        this.connection = connection;
    }

    public DeviceConnection getConnection() {
        return connection;
    }

    public boolean addListener(PropertyChangeListener propertyChangeListener) {
        return propertyListeners.add(propertyChangeListener);
    }

    public boolean removeListener(PropertyChangeListener o) {
        return propertyListeners.remove(o);
    }

    public boolean addListener(ActionCommandListener actionCommandListener) {
        return actionListeners.add(actionCommandListener);
    }

    public boolean removeListener(ActionCommandListener o) {
        return actionListeners.remove(o);
    }

    public List<PropertyChangeListener> getPropertyListeners() {
        return propertyListeners;
    }

    public List<ActionCommandListener> getActionListeners() {
        return actionListeners;
    }
}
