/*
 * ******************************************************************************
 *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

// Register new Type
$.extend(od.view.dashTypes,{
    DIGITAL_CONTROLLER: {
        id: "DIGITAL_CONTROLLER", // Java Enum : DashboardType
        name: "Digital Controller",
        klass: "od.view.DigitalCtrlView",
        multipleDevices: true,
        allowSensor : false,
        allowDevice : true,
        deviceTypes: [od.DeviceType.DIGITAL],
        fields: [
            // [Name, Required, Default]
            // ["iconON", true, "lightbulb_on.png"],
            // ["iconOFF", true, "lightbulb_off.png"],
            ["textON", true, "ON"],
            ["textOFF", true, "OFF"],
        ]
    },
});

od.view.DigitalCtrlView = od.view.DashItemView.extend(function() {

    var _this = this;
    var deviceListeners = [];

    var HTML="";
    HTML += "<div class=\"device-view device-digital\">";
    HTML += "    <div class=\"device-view-icon\"><img src=\"/images/devices/lightbulb.png\"/><\/div>";
    HTML += "    <div class=\"device-view-content\">";
    HTML += "        <span class=\"device-view-title\">Device<\/span>";
    HTML += "        <span class=\"device-view-value\">OFF<\/span>";
    HTML += "    <\/div>";
    HTML += "<\/div>";

    this.render = function ($el) {

        this.el = $el;

        // create HTML

        var _this = this;

        this.model.monitoredDevices.forEach(function(deviceID){

            var device = ODev.get(deviceID);

            if(!device) console.error("Device with id: " + deviceID + " not found, chart: " + _this.model.title);

            if(device){

                var listener = device.onChange(onDeviceChange, _this);
                deviceListeners.push(listener);

                var $device = $(HTML);
                _this.el.append($device);
                $device.attr("data-deviceid", deviceID);
                $device.click(setValue);
                updateView.call(_this, $device, deviceID); // set values
            }
        });

    };

    this.destroy = function () {
        // Remove listeners from devices.
        this.model.monitoredDevices.forEach(function(deviceID, index) {
            var device = ODev.get(deviceID);
            if(device) device.removeListener(deviceListeners[index]);
        });

        this.super.destroy();
    }

    // ==========================================================================
    // Private
    // ==========================================================================

    function onDeviceChange(value, deviceID){
        var $device = $("[data-deviceid="+deviceID+"]", this.el);
        updateView.call(this, $device, deviceID); // use call(this, ) becouse is a OpenDevice inner event
    }

    function updateView($device, deviceID){
        var device = ODev.get(deviceID);
        $(".device-view-title", $device).text(device.title);

        var $value = $(".device-view-value", $device);
        $value.text(device.isON() ? this.model.viewOptions.textON : this.model.viewOptions.textOFF);

        $value.removeClass("on off");
        $value.addClass(device.isON() ? "on" : "off");

        var iconName = device.icon || "power.svg";
        var icon = device.isON() ? "on/" + iconName : "off/" + iconName;
        $("img", $device).attr('src', "/images/devices/" + icon);
    }

    function setValue(event){
        var deviceID = $(event.currentTarget).data("deviceid");
        OpenDevice.toggleValue(deviceID);
    }

});

