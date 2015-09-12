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

package br.com.criativasoft.opendevice.core;

import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;

/**
 * Provider that is used for local applications without support muti-tenant
 * @author Ricardo JL Rufino
 * @date 29/08/15.
 */
public class LocalTenantProvider extends TenantProvider {

    @Override
    public void setTenantID(String appID) {
    }

    @Override
    public String getTenantID() {
        return OpenDeviceConfig.LOCAL_APP_ID;
    }
}
