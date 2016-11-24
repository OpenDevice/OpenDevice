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

package br.com.criativasoft.opendevice.middleware.persistence.dao.jpa;

import br.com.criativasoft.opendevice.middleware.model.jobs.JobSpec;
import br.com.criativasoft.opendevice.middleware.persistence.dao.JobSpecDao;

/**
 * @author Ricardo JL Rufino
 * @date 01/11/16
 */
public class JobSpecJPA extends GenericJpa<JobSpec>  implements JobSpecDao {

    protected JobSpecJPA() {
        super(JobSpec.class);
    }

    @Override
    public void persist(JobSpec entity) {
        entity.setAccount(getCurrentAccount());
        super.persist(entity);
    }

    @Override
    public JobSpec update(JobSpec entity) {
        entity.setAccount(getCurrentAccount());
        return super.update(entity);
    }
}
