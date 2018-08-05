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

package br.com.criativasoft.opendevice.middleware.rules;

import br.com.criativasoft.opendevice.middleware.jobs.AbstractAction;
import br.com.criativasoft.opendevice.middleware.model.rules.ActiveTimeConditionSpec;
import br.com.criativasoft.opendevice.middleware.model.rules.ConditionSpec;
import br.com.criativasoft.opendevice.middleware.model.rules.RuleEnums;
import br.com.criativasoft.opendevice.middleware.model.rules.RuleSpec;
import br.com.criativasoft.opendevice.middleware.rules.condition.AbstractCondition;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 03/11/16
 */
public abstract class AbstractRule<T extends RuleSpec>  {

    private T spec;

    private AbstractCondition condition;

    private AbstractAction action;

    public void setSpec(T spec){
        this.spec = spec;
    }

    public T getSpec() {
        return spec;
    }

    public void setCondition(AbstractCondition condition) {
        this.condition = condition;
    }

    public void setAction(AbstractAction action) {
        this.action = action;
    }

    public AbstractAction getAction() {
        return action;
    }

    public boolean evaluate() {

        boolean valid = check();

        if(condition != null){

            // Checks whether the condition has also been satisfied
            if(valid){
                return condition.evalRuleTrigged();
            }else{
                // Notifies condition that main rule not met
                condition.ruleNotTrigged();
                return false;
            }

        }else{
            return valid;
        }

    }

    /**
     * This check if action can be executed.
     * Sometimes it is not interesting to run the same alert/action several times
     * @return
     */
    public boolean allowExection() {

        // if is not a Timebased rule, execute allways
        if(!AbstractRule.isTimedRule(getSpec())){
            return true;
        }

        // Avoid fire multiple times in Timebased rule
        return spec.getStatus() != RuleEnums.ExecutionStatus.ACTIVE;
    }

    public static boolean isTimedRule(RuleSpec spec){
        ConditionSpec condition = spec.getCondition();
        return condition instanceof ActiveTimeConditionSpec;
    }

    public abstract boolean check();
}
