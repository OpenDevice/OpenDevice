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

import br.com.criativasoft.opendevice.restapi.model.*;
import br.com.criativasoft.opendevice.restapi.model.dao.UserDao;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.crypto.hash.DefaultHashService;

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
    public User createUser(Account account, String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        //Encrypt
        DefaultPasswordService service = new DefaultPasswordService();
        DefaultHashService hashService = (DefaultHashService) service.getHashService();
        hashService.setHashIterations(1);
        user.setPassword(service.encryptPassword(user.getPassword()));

        UserAccount userAccount = new UserAccount();
        userAccount.setOwner(account);
        account.getUserAccounts().add(userAccount);
        if(account.getId() <= 0 ) userAccount.setType(AccountType.ACCOUNT_MANAGER);
        else userAccount.setType(AccountType.USER);

        userAccount.setUser(user);

        ApiKey key = new ApiKey();
        key.setKey(account.getUuid());
        key.setAppName("ApplicationID");
        key.setAccount(userAccount);
        userAccount.getKeys().add(key);

        user.getAccounts().add(userAccount);

        persist(user);

        return user;
    }

    @Override
    public void delete(UserAccount userAccount) {
        em().remove(userAccount);
    }
}
