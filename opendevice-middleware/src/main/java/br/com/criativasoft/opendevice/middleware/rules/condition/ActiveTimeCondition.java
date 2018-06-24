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

import br.com.criativasoft.opendevice.middleware.model.rules.ActiveTimeConditionSpec;
import br.com.criativasoft.opendevice.middleware.model.rules.RuleEnums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ricardo JL Rufino
 * @date 03/11/16
 */
public class ActiveTimeCondition extends AbstractCondition<ActiveTimeConditionSpec>  {

    private long fistTrigger;

    private static final Logger log = LoggerFactory.getLogger(ActiveTimeCondition.class);

    @Override
    public boolean evalRuleTrigged(){

        if(fistTrigger == 0){
            fistTrigger = System.currentTimeMillis();
        }
        else{

            long interval = System.currentTimeMillis() - fistTrigger;

            // We use 1 second scale
            if(interval < 1000) return false;

            long intervalSec = (interval / 1000);

            long targetSec = spec().getTime() * getMultp(spec().getIntervalType());

            if(intervalSec >= targetSec){
                log.debug("ACTIVE || Elapsed Time : " + interval + "ms, of: " + getSpec().getTime() + "/" + getSpec().getIntervalType());
                return true;
            }

        }

        return false;
    }

    @Override
    public void ruleNotTrigged(){

        fistTrigger = 0; // reset timer

    }


    public long getMultp(RuleEnums.IntervalType type){

        switch (type) {
            case SECOND:
                return 1;
            case MINUTE:
                return 60;
            case HOUR:
                return 60 * 24;
        }

        throw  new IllegalStateException("IntervalType not implemented = " + type);
    }


}
