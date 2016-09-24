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

    @Column(unique = true)
    private String username;

    private String password;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch=FetchType.EAGER)
    @JsonManagedReference
    private Set<ApiKey> keys = new HashSet<ApiKey>();

    public Account() {
    }

    public Account(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<ApiKey> getKeys() {
        return keys;
    }

    public void setKeys(Set<ApiKey> keys) {
        this.keys = keys;
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
    public void generateUUID(){
        this.uuid = UUID.randomUUID().toString();
    }


    @Override
    public String toString() {
        return "Account["+username+", "+uuid+"]";
    }
}

