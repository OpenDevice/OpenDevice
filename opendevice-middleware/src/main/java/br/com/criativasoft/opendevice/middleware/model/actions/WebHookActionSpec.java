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

package br.com.criativasoft.opendevice.middleware.model.actions;

import br.com.criativasoft.opendevice.middleware.rules.HandledBy;
import br.com.criativasoft.opendevice.middleware.rules.action.WebHookAction;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.persistence.Entity;

/**
 * @author Ricardo JL Rufino
 * @date 02/11/16
 */
@Entity
@JsonTypeName(value = "webhook")
@HandledBy(WebHookAction.class)
public class WebHookActionSpec extends ActionSpec {

    private String url;

    private String parameters;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

}
