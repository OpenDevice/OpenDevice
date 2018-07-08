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

import br.com.criativasoft.opendevice.middleware.model.Firmware;
import br.com.criativasoft.opendevice.middleware.persistence.dao.FirmwareDao;

import javax.persistence.TypedQuery;
import java.util.UUID;

/**
 * @author Ricardo JL Rufino
 *         Date: 08/07/18
 */
public class FirmwareJPA extends GenericTenantJpa<Firmware> implements FirmwareDao {

    public FirmwareJPA() {
        super(Firmware.class);
    }

    @Override
    public void persist(Firmware entity) {
        if(entity.getUuid() == null){
            entity.setUuid(UUID.randomUUID().toString());
        }
        super.persist(entity);
    }

    @Override
    public Firmware findByUUID(String uuid) {
        TypedQuery<Firmware> query = em().createQuery("from "+ persistentClass.getSimpleName() +" where uuid = :UUID", persistentClass);
        query.setParameter("UUID", uuid);
        query.setMaxResults(1);
        return query.getSingleResult();
    }

}
