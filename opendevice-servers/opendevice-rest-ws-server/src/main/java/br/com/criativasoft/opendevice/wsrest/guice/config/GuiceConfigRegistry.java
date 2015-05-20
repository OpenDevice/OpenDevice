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

package br.com.criativasoft.opendevice.wsrest.guice.config;

import com.google.inject.Module;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 14/05/15.
 */
public class GuiceConfigRegistry {

    private static Class<? extends Module> configClass = GuiceModule.class;


    public static void setConfigClass(Class<? extends Module> configClass) {
        GuiceConfigRegistry.configClass = configClass;
    }

    public static Class<? extends Module> getConfigClass() {
        return configClass;
    }
}
