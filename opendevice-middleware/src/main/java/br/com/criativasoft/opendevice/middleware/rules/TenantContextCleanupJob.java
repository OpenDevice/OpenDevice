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

package br.com.criativasoft.opendevice.middleware.rules;

import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.middleware.MainTenantProvider;
import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;

/**
 * Fire cleanup in MainTenantProvider
 * @author Ricardo JL Rufino
 *         Date: 18/08/18
 * @see br.com.criativasoft.opendevice.middleware.jobs.JobManager#createDefaultJobs()
 */
public class TenantContextCleanupJob extends Job {

    @Override
    public void doRun() throws JobInterruptException {

        MainTenantProvider tenantProvider = (MainTenantProvider) TenantProvider.getTenantProvider();
        tenantProvider.cleanUp();

    }
}