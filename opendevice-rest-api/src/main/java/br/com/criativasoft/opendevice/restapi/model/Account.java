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

package br.com.criativasoft.opendevice.restapi.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author Ricardo JL Rufino
 * @date 10/09/16
 */
@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(unique = true)
    private String uuid;

    @OneToMany(mappedBy= "owner")
    private Set<UserAccount> userAccounts = new HashSet<UserAccount>();

    public Account() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @PrePersist
    protected void generateUUID(){
        if(this.uuid == null)  this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public String toString() {
        return "Account["+uuid+"]";
    }

    public Set<UserAccount> getUserAccounts() {
        return userAccounts;
    }

    public void setUserAccounts(Set<UserAccount> userAccounts) {
        this.userAccounts = userAccounts;
    }
}

