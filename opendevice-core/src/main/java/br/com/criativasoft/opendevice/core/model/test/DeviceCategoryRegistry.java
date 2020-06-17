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
 * @author Ricardo JL Rufino
 * @date 13/01/16
 */
public class DeviceCategoryRegistry {

    private List<DeviceCategory> categories = new ArrayList<DeviceCategory>();

    public DeviceCategoryRegistry() {

        DeviceCategory[] values = DeviceCategory.DEFAULT_VALUES;
        for (DeviceCategory value : values) {
            add(value);
        }

    }

    public void add(DeviceCategory category) {
        this.categories.add(category);
    }

    public void add(Class<? extends DeviceCategory> klass) {
        try {
            DeviceCategory category = klass.newInstance();
            if (category instanceof GenericCategory) {
                ((GenericCategory) category).loadProperties();
            }
            add(category);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public List<DeviceCategory> getCategories() {
        return categories;
    }

    public DeviceCategory getCategory(Class<? extends DeviceCategory> klass) {

        for (DeviceCategory category : categories) {
            if (category.getClass() == klass) {
                return category;
            }
        }

        return null;

    }

    public DeviceCategory getCategory(int code) {

        for (DeviceCategory category : categories) {
            if (category.getCode() == code) {
                return category;
            }
        }

        return null;

    }


}
