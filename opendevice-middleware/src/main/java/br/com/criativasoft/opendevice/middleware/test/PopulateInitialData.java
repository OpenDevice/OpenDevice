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

package br.com.criativasoft.opendevice.middleware.test;

import br.com.criativasoft.opendevice.core.util.DefaultPasswordGenerator;
import br.com.criativasoft.opendevice.restapi.model.*;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.HashingPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ricardo JL Rufino on 06/05/15.
 */
public class PopulateInitialData {

    private static final Logger log = LoggerFactory.getLogger(PopulateInitialData.class);

    private EntityManager em;

    private List<Account> accounts;

    public PopulateInitialData(EntityManager em) {
        this.em = em;
    }

    public void initDatabase(){
        accounts = createUsers();
    }

    private List<Account> createUsers() {
        List<Account> list = new ArrayList<Account>();
        list.add(saveUser("admin", "admin", AccountType.CLOUD_MANAGER, new String(new DefaultPasswordGenerator().generatePassword())));
        return list;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    private Account saveUser(String u, String p, AccountType type, String key) {
        HashingPasswordService service = new DefaultPasswordService();

        log.info("Saving user: "+ u);
        User user = new User();
        user.setUsername(u);
        user.setPassword(service.encryptPassword(p));
        em.persist(user);

        Account account = new Account();
        account.setUuid(key);
        em.persist(account);

        UserAccount uaccount = new UserAccount();
        uaccount.setType(type);
        uaccount.setUser(user);
        uaccount.setOwner(account);
        em.persist(uaccount);

        ApiKey apiKey = new ApiKey();
        apiKey.setAccount(uaccount);
        apiKey.setAppName("ApplicationID");
        apiKey.setKey(account.getUuid());
        uaccount.getKeys().add(apiKey);
        em.persist(apiKey);
        em.persist(account);

        log.info("AccountUID :"  + account.getUuid());

        return account;
    }

}
