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

package br.com.criativasoft.opendevice.middleware.model.jobs;

import br.com.criativasoft.opendevice.middleware.model.actions.ActionSpec;
import br.com.criativasoft.opendevice.middleware.model.rules.RuleEnums;
import br.com.criativasoft.opendevice.restapi.model.Account;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.persistence.*;
import java.util.Date;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 06/11/16
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(CronJobSpec.class),
})
public class JobSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(length = 125, nullable = false)
    private String description;

    private boolean enabled;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastExecution;

    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    private Date endDate;

    private String executionMessage;

    private RuleEnums.ExecutionStatus status = RuleEnums.ExecutionStatus.INACTIVE;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name="accountID", updatable = false)
    @JsonIgnore
    private Account account;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="actionID")
    private ActionSpec action;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Date getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(Date lastExecution) {
        this.lastExecution = lastExecution;
    }

    public String getExecutionMessage() {
        return executionMessage;
    }

    public void setExecutionMessage(String executionMessage) {
        this.executionMessage = executionMessage;
    }

    public RuleEnums.ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(RuleEnums.ExecutionStatus status) {
        this.status = status;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public ActionSpec getAction() {
        return action;
    }

    public void setAction(ActionSpec action) {
        this.action = action;
    }
}
