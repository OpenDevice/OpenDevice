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

import br.com.criativasoft.opendevice.core.ODev;
import br.com.criativasoft.opendevice.middleware.jobs.JobManager;
import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;

/**
 * @author Ricardo JL Rufino
 * @date 06/11/16
 * @see JobManager#createDefaultJobs()
 */
public class RuleManagerJob extends Job {

    @Override
    public void doRun() throws JobInterruptException {

        ODev.getDeviceManager().transactionBegin();

        RuleManager instance = getJobContext().get("instance");

        instance.eval();

        ODev.getDeviceManager().transactionEnd();

    }
}
