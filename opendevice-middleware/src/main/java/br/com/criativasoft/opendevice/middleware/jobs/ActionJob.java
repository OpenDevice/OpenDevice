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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ricardo JL Rufino
 * @date 05/11/16
 */
public class ActionJob extends Job {

    private static final Logger log  = LoggerFactory.getLogger(ActionJob.class);

    @Override
    public void doRun() throws JobInterruptException {

        AbstractAction action = getJobContext().get("action");
        JobSpec spec = getJobContext().get("spec");

        if(spec.isEnabled()){
            TenantProvider.setCurrentID(spec.getAccount().getUuid());
            action.execute();
        }else{
            log.warn("Trying execute a not enabled JOB, the job was supposed to be deleted ..;");
        }

    }
}
