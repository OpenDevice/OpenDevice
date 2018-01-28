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

package br.com.criativasoft.opendevice.middleware.rules.action;

import br.com.criativasoft.opendevice.middleware.jobs.AbstractAction;
import br.com.criativasoft.opendevice.middleware.model.actions.ActionSpec;
import br.com.criativasoft.opendevice.middleware.rules.HandledBy;

/**
 * Instantiate the class responsible for implementing ActionSpec logic.
 * To work you need the annotation: @HandledBy(ControlAction.class) in the Spec class, and
 * add to list of @JsonSubTypes in {@link ActionSpec}
 *
 * @author Ricardo JL Rufino
 * @date 04/11/16
 */
public class ActionFactory {

    public AbstractAction create(ActionSpec spec){
        AbstractAction action = null;

        if (spec.getClass().isAnnotationPresent(HandledBy.class)) {
            HandledBy annotation = spec.getClass().getAnnotation(HandledBy.class);
            Class value = annotation.value();
            try {
                action = (AbstractAction) value.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (action == null) throw new IllegalStateException("Implementation for Action : " + spec + ", not found !");

        action.setSpec(spec);

        return action;
    }
}
