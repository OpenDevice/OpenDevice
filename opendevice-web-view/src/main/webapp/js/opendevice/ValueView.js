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

// Register new Type
$.extend(od.view.dashTypes,{
    DYNAMIC_VALUE: {
        id: "DYNAMIC_VALUE", // Java Enum : DashboardType
        name: "Dyn Value X",
        klass: "od.view.ValueView",
        multipleDevices: false,
        allowSensor : true,
        allowDevice : false,
        deviceTypes: [],
        fields: [
            // [Name, Required, Default]
            ["colors", false, null],
            ["thresholds", false, null]
        ]
    },
});

od.view.ValueView = od.view.DashItemView.extend(function() {

    var _this = this;

    var TEMPLATE="";
    TEMPLATE += "<div class=\"value-view\">";
    TEMPLATE += "    XXX";
    TEMPLATE += "<\/div>";

    this.render = function ($el) {

        this.el = $el;

        var $html = $(TEMPLATE);
        _this.el.append($html);

    };

    this.destroy = function () {
        this.super.destroy();
    }

    // ==========================================================================
    // Private
    // ==========================================================================


});

