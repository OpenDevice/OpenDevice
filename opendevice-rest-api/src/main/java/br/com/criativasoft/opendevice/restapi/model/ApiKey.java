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

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.UUID;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 10/09/16
 */
@Entity
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String key;

    /**
     * Name of Application
     */
    private String appName;

    @ManyToOne(fetch= FetchType.EAGER)
    @JsonIgnore
//    @JsonBackReference
    private UserAccount account;

    public ApiKey() {

    }

    public ApiKey(String appName, String key) {
        this.key = key;
        this.appName = appName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public UserAccount getAccount() {
        return account;
    }

    public void setAccount(UserAccount account) {
        this.account = account;
    }

    @PrePersist
    protected void generateKey(){
        if(this.key == null) this.key = UUID.randomUUID().toString();
    }

}
