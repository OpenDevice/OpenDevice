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

import br.com.criativasoft.opendevice.wsrest.io.WebUtils;
import br.com.criativasoft.opendevice.wsrest.resource.AuthRest;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.subject.WebSubject;
import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereInterceptorAdapter;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.FrameworkConfig;
import org.atmosphere.interceptor.ShiroInterceptor;
import org.atmosphere.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

/**
 * Shiro Interceptor, this will build/find Subject from request information and disable session for Rest
 *
 * Shiro Interceptor, it creates a request attribute (subject) that contains the true Subject.
 * For more information about why don't use directly SecurityUtils.getSubject
 * http://jfarcand.wordpress.com/2011/07/13/quick-tip-using-apache-shiro-with-your-atmospheres-websocketcomet-app/
 * @author Ricardo JL Rufino
 * @date 23/09/16
 */
public class NewShiroInterceptor extends AtmosphereInterceptorAdapter {


    private static final Logger logger = LoggerFactory.getLogger(ShiroInterceptor.class);

    @Override
    public Action inspect(AtmosphereResource r) {

        if (Utils.webSocketMessage(r)) return Action.CONTINUE;

        String pathInfo = r.getRequest().getPathInfo();

        // Ignore Web Resources.
        if(WebUtils.isWebResource(pathInfo)){
            return Action.CONTINUE;
        }

        if (r.getRequest().localAttributes().containsKey(FrameworkConfig.SECURITY_SUBJECT) == false) {
            try {

                // Create/find Subject using Request (and cookies) to restore state/session
                Subject currentUser = new Builder(r.getRequest(), r.getResponse()).buildWebSubject();
                ThreadContext.bind(currentUser);

                // Store to use in WebSocket / Broadcast response.
                r.getRequest().setAttribute(FrameworkConfig.SECURITY_SUBJECT, currentUser);

            } catch (UnavailableSecurityManagerException ex) {
                logger.info("Shiro Web Security : {}", ex.getMessage());
            } catch (java.lang.IllegalStateException ex) {
                logger.info("Shiro Web Environment : {}", ex.getMessage());
            }
        }

        return Action.CONTINUE;
    }

    public static String getSessionCookie(ServletRequest request){

        if(request instanceof HttpServletRequest){
            HttpServletRequest wrapper = (HttpServletRequest) request;
            Cookie[] cookies = wrapper.getCookies();
            if(cookies != null){
                for (Cookie cookie : cookies) {
                    if(cookie.getName().equals(AuthRest.SESSION_ID)){
                        return cookie.getValue();
                    }
                }
            }
        }

        return null;
    }

    public static class Builder extends WebSubject.Builder{

        public Builder(ServletRequest request, ServletResponse response) {
            super(request, response);

            getSubjectContext().setSecurityManager(SecurityUtils.getSecurityManager());

            // Disable session creation for Rest (make stateless)
            if(request instanceof HttpServletRequest){
                String header = ((HttpServletRequest) request).getHeader(HttpHeaders.AUTHORIZATION);
                if (header != null && (header.startsWith("Bearer ") || header.startsWith("Basic "))) {
                    getSubjectContext().setSessionCreationEnabled(false);
                    request.setAttribute(DefaultSubjectContext.SESSION_CREATION_ENABLED, false);
                }
            }

            // Use cookie to resestore session from DefaultSessionManager
            String sessionCookie = getSessionCookie(request);
            if(sessionCookie != null){
                getSubjectContext().setSessionId(sessionCookie);
            }



        }

    }


}
