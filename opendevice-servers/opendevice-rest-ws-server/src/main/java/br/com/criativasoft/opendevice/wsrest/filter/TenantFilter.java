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

package br.com.criativasoft.opendevice.wsrest.filter;

import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import br.com.criativasoft.opendevice.restapi.auth.AccountPrincipal;
import br.com.criativasoft.opendevice.wsrest.io.WebUtils;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.ws.rs.ext.Provider;

/**
 *
 * @author Ricardo JL Rufino
 * @date 24/09/16
 */
@Provider
public class TenantFilter implements ContainerRequestFilter {

    private OpenDeviceConfig config;

    public TenantFilter(OpenDeviceConfig odevConfig) {
        this.config = odevConfig;
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {

        // Ignore Web Resources.
        String path = request.getPath();
        if(WebUtils.isWebResource(path)){
            return request;
        }

        if(config.isAuthRequired()){
            Subject subject = SecurityUtils.getSubject();
            subject.getSession(false);

            if(subject.isAuthenticated()){
                AccountPrincipal principal = (AccountPrincipal) subject.getPrincipal(); // return UUID from Account
                TenantProvider.setCurrentID(principal.getAccountUUID());
            }else{
                TenantProvider.setCurrentID(null);
            }
        }

        return request;
    }
}
