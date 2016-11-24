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

package br.com.criativasoft.opendevice.middleware.model.rules;

import br.com.criativasoft.opendevice.core.model.Device;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 *
 * @author Ricardo JL Rufino
 * @date 31/10/16
 */
@Entity
@JsonTypeName(value = "threshold")
public class ThresholdRuleSpec extends RuleSpec {

    private RuleEnums.ThresholdType type;

    private RuleEnums.ThresholdTarget target;

    private long value;

    private long valueMax; // optional

    @OneToOne
    @JsonIdentityReference(alwaysAsId = true)
    private Device targetDevice;  // optional

}
