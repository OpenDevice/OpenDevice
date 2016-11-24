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

package br.com.criativasoft.opendevice.middleware.jobs;

import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.middleware.model.jobs.JobSpec;
import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;

/**
 *
 * @author Ricardo JL Rufino
 * @date 05/11/16
 */
public class ActionJob extends Job {

    @Override
    public void doRun() throws JobInterruptException {

        AbstractAction action = getJobContext().get("action");
        JobSpec spec = getJobContext().get("spec");

        TenantProvider.setCurrentID(spec.getAccount().getUuid());

        action.execute();

    }
}
