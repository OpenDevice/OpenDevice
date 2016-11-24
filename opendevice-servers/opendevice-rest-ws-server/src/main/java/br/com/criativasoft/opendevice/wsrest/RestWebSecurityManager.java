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

package br.com.criativasoft.opendevice.wsrest;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.subject.support.WebDelegatingSubject;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 24/09/16
 */
public class RestWebSecurityManager extends DefaultWebSecurityManager {

    private static final Logger log = LoggerFactory.getLogger(RestWebSecurityManager.class);

    public RestWebSecurityManager(Collection<Realm> realms) {
        super(realms);
        setRememberMeManager(null); // disable for now
    }

    public RestWebSecurityManager(Realm singleRealm) {
        super(singleRealm);
        setRememberMeManager(null); // disable for now
    }

    public RestWebSecurityManager() {
        super();
    }

    @Override
    protected Subject createSubject(AuthenticationToken token, AuthenticationInfo info, Subject existing) {
        SubjectContext context = createSubjectContext();
        context.setAuthenticated(true);
        context.setAuthenticationToken(token);
        context.setAuthenticationInfo(info);
        if (existing != null) {
            // FIX Avoid session creation if previous Subject is disabled.
            // org.apache.shiro.subject.SubjectContext.isSessionCreationEnabled()
            if(existing instanceof WebDelegatingSubject) {
                context.setSessionCreationEnabled(WebUtils._isSessionCreationEnabled(this));
                context.setSecurityManager(((WebDelegatingSubject) existing).getSecurityManager());
            }
            context.setSubject(existing);
        }
        return createSubject(context);
    }

    @Override
    protected Session resolveContextSession(SubjectContext context) throws InvalidSessionException {
        SessionKey key = getSessionKey(context);
        if (key != null && key.getSessionId() != null) { // FIXED: check internal sessionID
            try {
                return getSession(key);
            }catch (UnknownSessionException ex){
                log.info(ex.getMessage());
            }

        }
        return null;
    }
}
