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

import br.com.criativasoft.opendevice.restapi.model.User;
import br.com.criativasoft.opendevice.restapi.model.UserAccount;
import br.com.criativasoft.opendevice.restapi.model.dao.UserDao;

import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author Ricardo JL Rufino
 * @date 24/09/16
 */
public class UserJPA extends GenericJpa<User> implements UserDao {

    public UserJPA() {
        super(User.class);
    }

    @Override
    public User getUser(String username) {
        TypedQuery<User> query = em().createQuery("select x from User x where x.username = :p1", User.class);
        query.setParameter("p1", username);
        query.setMaxResults(1);

        List<User> list = query.getResultList();

        if(list.isEmpty()) return null;

        return list.iterator().next();
    }

    @Override
    public void delete(UserAccount userAccount) {
        em().remove(userAccount);
    }
}
