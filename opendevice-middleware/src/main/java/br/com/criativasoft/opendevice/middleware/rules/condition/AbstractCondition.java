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

package br.com.criativasoft.opendevice.middleware.rules.condition;

import br.com.criativasoft.opendevice.middleware.model.rules.ConditionSpec;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 03/11/16
 */
public abstract class AbstractCondition<T extends ConditionSpec> {

    private T spec;

    public void setSpec(T spec){
        this.spec = spec;
    }

    public T getSpec() {
        return spec;
    }

    public T spec() {
        return spec;
    }

    /**
     * This function is called when the main Rule returns True
     * @return Returns true if the condition was satisfied
     */
    public abstract boolean evalRuleTrigged();

    /**
     * This function is called when the Rule was not satisfied (return False). <br/>
     * Implementations can use this function to reset the counters for example
     */
    public abstract void ruleNotTrigged();
}
