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

import br.com.criativasoft.opendevice.middleware.model.rules.RuleSpec;
import br.com.criativasoft.opendevice.middleware.persistence.dao.RuleSpecDao;

import java.util.LinkedList;
import java.util.List;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 *         Date: 24/06/18
 */
public class RuleSpecDaoTest implements RuleSpecDao {

    private List<RuleSpec> ruleSpecs = new LinkedList<RuleSpec>();

    @Override
    public RuleSpec getById(long id) {
        return null;
    }

    @Override
    public void persist(RuleSpec entity) {
        ruleSpecs.add(entity);
    }

    @Override
    public RuleSpec update(RuleSpec entity) {
        return entity;
    }

    @Override
    public void delete(RuleSpec entity) {

    }

    @Override
    public void refresh(RuleSpec entity) {

    }

    @Override
    public List<RuleSpec> listAll() {
        return ruleSpecs;
    }

    @Override
    public List<RuleSpec> listAllByUser() {
        return ruleSpecs;
    }
}
