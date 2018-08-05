/*
 *
 *  * ******************************************************************************
 *  *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  *  All rights reserved. This program and the accompanying materials
 *  *  are made available under the terms of the Eclipse Public License v1.0
 *  *  which accompanies this distribution, and is available at
 *  *  http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  *  Contributors:
 *  *  Ricardo JL Rufino - Initial API and Implementation
 *  * *****************************************************************************
 *
 */

package br.com.criativasoft.opendevice.wsrest.filter;

import org.atmosphere.cpr.*;
import org.atmosphere.interceptor.InvokationOrder;
import org.atmosphere.util.Utils;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 05/10/14.
 */
public class CrossOriginInterceptor extends AtmosphereInterceptorAdapter {

    private final String EXPOSE_HEADERS = "X-Atmosphere-tracking-id, " + HeaderConfig.X_HEARTBEAT_SERVER;

    private boolean enableAccessControl = true;

    @Override
    public void configure(AtmosphereConfig config) {
        String ac = config.getInitParameter(ApplicationConfig.DROP_ACCESS_CONTROL_ALLOW_ORIGIN_HEADER);
        if (ac != null) {
            enableAccessControl = Boolean.parseBoolean(ac);
        }
    }

    @Override
    public Action inspect(AtmosphereResource r) {

        if (Utils.webSocketMessage(r)) return Action.CONTINUE;

        if (!enableAccessControl) return Action.CONTINUE;

        AtmosphereRequest req = r.getRequest();
        AtmosphereResponse res = r.getResponse();

        if (req.getHeader("Origin") != null && res.getHeader("Access-Control-Allow-Origin") == null) {
            res.addHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
            res.addHeader("Access-Control-Expose-Headers", EXPOSE_HEADERS);
            res.setHeader("Access-Control-Allow-Credentials", "true");
        }

        if ("OPTIONS".equals(req.getMethod())) {
            res.setHeader("Access-Control-Allow-Methods", "GET,HEAD,OPTIONS,POST,PUT");
            res.setHeader("Access-Control-Allow-Headers",
                    "Origin, Content-Type, Authorization, AuthToken, X-AppID, X-Atmosphere-Framework,  "
                            + EXPOSE_HEADERS
                            + ", X-Atmosphere-Transport, X-Atmosphere-TrackMessageSize, X-atmo-protocol");
            res.setHeader("Access-Control-Max-Age", "-1");

            return Action.SKIP_ATMOSPHEREHANDLER;
        }

        return Action.CONTINUE;
    }

    public boolean enableAccessControl() {
        return enableAccessControl;
    }

    @Override
    public PRIORITY priority() {
        return InvokationOrder.FIRST_BEFORE_DEFAULT;
    }

    @Override
    public String toString() {
        return "CrossOriginInterceptor Support";
    }
}