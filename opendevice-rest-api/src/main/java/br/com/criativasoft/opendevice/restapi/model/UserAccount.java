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

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * A 'user' defines the relationship between user and your list of accounts and </br>
 * permissions that the user has to the linked accounts.
 * @author Ricardo JL Rufino
 * @date 24/09/16
 */
@Entity
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne(fetch= FetchType.EAGER)
    private User user;

    @ManyToOne(fetch= FetchType.EAGER)
    private Account owner;

    private AccountType type;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    @JsonManagedReference
    private Set<ApiKey> keys = new HashSet<ApiKey>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Set<ApiKey> getKeys() {
        return keys;
    }

    public void setKeys(Set<ApiKey> keys) {
        this.keys = keys;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Account getOwner() {
        return owner;
    }

    public void setOwner(Account account) {
        this.owner = account;
    }
}
