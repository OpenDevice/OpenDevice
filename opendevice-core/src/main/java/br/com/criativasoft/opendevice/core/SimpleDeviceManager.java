/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.core;

import br.com.criativasoft.opendevice.core.dao.memory.DeviceMemoryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: PENDING DOC
 *
 * @autor Ricardo JL Rufino
 * @date 24/08/14.
 */
public class SimpleDeviceManager extends BaseDeviceManager {

    private static final Logger log = LoggerFactory.getLogger(SimpleDeviceManager.class);

    public SimpleDeviceManager(){
        setDeviceDao(new DeviceMemoryDao());
    }


}
