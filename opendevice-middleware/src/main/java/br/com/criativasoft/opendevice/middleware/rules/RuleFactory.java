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

import br.com.criativasoft.opendevice.middleware.rules.action.ActionFactory;
import br.com.criativasoft.opendevice.middleware.model.actions.ActionSpec;
import br.com.criativasoft.opendevice.middleware.model.rules.ConditionSpec;
import br.com.criativasoft.opendevice.middleware.model.rules.RuleSpec;
import br.com.criativasoft.opendevice.middleware.model.rules.ActiveTimeConditionSpec;
import br.com.criativasoft.opendevice.middleware.model.rules.StateRuleSpec;
import br.com.criativasoft.opendevice.middleware.rules.condition.AbstractCondition;
import br.com.criativasoft.opendevice.middleware.rules.condition.ActiveTimeCondition;

/**
 * @author Ricardo JL Rufino
 * @date 04/11/16
 */
public class RuleFactory extends ActionFactory {

    public AbstractRule create(RuleSpec spec){

        AbstractRule rule = null;

        if(spec instanceof StateRuleSpec){
            rule = new StateRule();
            rule.setSpec((StateRuleSpec) spec);
        }

        if(rule == null) throw new IllegalStateException("Implementation for Rule : " + spec + ", not found !");


        ConditionSpec specCondition = spec.getCondition();
        if(specCondition != null){
            rule.setCondition(create(specCondition));
        }

        ActionSpec actionSpec = spec.getAction();

        if(actionSpec != null){
            rule.setAction(create(actionSpec));
        }

        return rule;
    }

    public AbstractCondition create(ConditionSpec spec){

        AbstractCondition condition = null;

        if(spec instanceof ActiveTimeConditionSpec){
            condition = new ActiveTimeCondition();
        }

        if (condition == null) throw new IllegalStateException("Implementation for Condition : " + spec + ", not found !");

        condition.setSpec(spec);

        return condition;
    }
}
