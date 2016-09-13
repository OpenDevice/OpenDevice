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

import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import com.google.inject.Provider;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 09/09/16
 */
public class OpenDeviceConfigProvider implements Provider<OpenDeviceConfig> {

    @Override
    public OpenDeviceConfig get() {
        return OpenDeviceConfig.get();
    }
}
