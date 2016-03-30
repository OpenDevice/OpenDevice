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
 * FIXME: Usar o BASEDEVICEMANAGER / SERVICE PROVIDER.getService(")...
 *
 * @author Ricardo JL Rufino
 * @date 13/01/16
 */
public class Registry {

    private static DeviceCategoryRegistry deviceCategoryRegistry = new DeviceCategoryRegistry();

    public static DeviceCategoryRegistry getDeviceCategoryRegistry() {
        return deviceCategoryRegistry;
    }
}
