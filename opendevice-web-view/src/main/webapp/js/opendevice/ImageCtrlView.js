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

    IMAGE_CONTROLLER: {
        id: "IMAGE_CONTROLLER", // Java Enum : DashboardType
        name: "Video/Image",
        klass: "od.view.ImageCtrlView",
        multipleDevices: false,
        allowSensor : false,
        allowDevice : false,
        deviceTypes: [],
        fields: [
            // [Name, Required, Default]
            ["subtype", true, null],
            ["path", true, null]
        ]
    },
});

od.view.ImageCtrlView = od.view.DashItemView.extend(function() {

    var _this = this;

    var HTML_IMG="";
    HTML_IMG += "<div class=\"image-view\">";
    // HTML_IMG += "    <div class=\"corners\"><\/div>";
    HTML_IMG += "    <img border=\"0\" src=\"\" \/>";
    HTML_IMG += "<\/div>";

    this.render = function ($el) {

        this.el = $el;

        // create HTML
        if(this.model.viewOptions.subtype == "IMAGE"){
            var $html = $(HTML_IMG);
            $html.find("img").attr("src", this.model.viewOptions.path);
            _this.el.append($html);
        }
        if(this.model.subtype == "video"){
            // var $html = $(HTML_IMG);
            // $html.find("img").attr("src", this.model.path);
            // _this.el.append($html);
        }

    };

    this.destroy = function () {
        this.super.destroy();
    }

    // ==========================================================================
    // Private
    // ==========================================================================


});

