/**
 * Created by ricardo on 06/07/14.
 */

'use strict';

var app = angular.module('opendevice.controllers', []);




/**
 * DashboardController
 * @connection - OpenDevice Connection, injected using 'ConnectionFactory.js'
 */
app.controller('DashboardController', ['$scope','$timeout', 'Dashboard', function ($scope, $timeout, Dashboard) {

    // Alias
    var DCategory = od.DeviceCategory;
    var DType = od.DeviceType;
    var manager = OpenDevice.manager;
    var DashItemView = od.DashItemView;

    var audioContext;
    var audioPlay;

    var $dashboards;
    var $layoutManager;

    $scope.model = {
        status : '',
        devices: [ ],
        dashboardItems: [ ],
        dashboard : { id : 1}
    };

    // Load Dashboard Items
    Dashboard.items({id : $scope.model.dashboard.id, paramtroX: 2}, function(values){

        angular.forEach(values, function(item, index) {

            item.layout = JSON.parse(item.layout); // convert from String to Array

            $scope.model.dashboardItems.push(new DashItemView(item));

        });

    });


    $scope.init = function(){

        $(function(){

            $dashboards = $('.dashboards');

            //Chart.defaults.global.responsive = true;
            Chart.defaults.global.maintainAspectRatio = false;

        });

        try {
            // Fix up for prefixing
            window.AudioContext = window.AudioContext||window.webkitAudioContext;
            audioContext = new AudioContext();
        }catch(e) {
            alert('Web Audio API is not supported in this browser');
        }

//        manager.on(DEvent.DEVICE_LIST_UPDATE, function(data){
//            $scope.model.devices = data;
//        });

        manager.on(od.Event.CONNECTED, function(data){

            var devices = manager.getDevices();
            var list = devices.filter(function(obj) {
                return obj.type == od.DeviceType.DIGITAL;
            });

            $scope.model.devices = list;
            $scope.$apply();
        });

        manager.on(od.Event.DEVICE_CHANGED, function(data){
            var device = findDevice(data.id);
            if(device){
                console.log("Controller.DEVICE_CHANGED");
                playSound(device);

                $timeout(function(){
                    $scope.$apply(); // sync view
                });
            }
        });

        OpenDevice.connect();

    }

    $scope.send = function(data){

        //$scope.model.devices = manager.getDevices();
        $scope.$apply();

    };

    /**
     * Function called by the View when a button is clicked
     */
    $scope.toggleValue = function(id){
        manager.toggleValue(id);
    };

    /**
     * Send value to all devices.
     * @param value
     */
    $scope.sendToAll = function(value){

        var devices = manager.getDevices();
        for(var i = 0; i < devices.length; i++){
            if(!devices[i].sensor)
                devices[i].setValue(value);
        }

    };

    $scope.addNewDash = function(){
        this.model['dashboardItems'].push( new DashItemView({ configMode : true, title : 'Grafico 3 ', type : 'LINE_CHART', layout : [1,3,1,1], metrics : [{ deviceID : 2 , type : 'realtime'  }]}));
        // NOTE: this will fire: renderCompleteDashItem, to setup appropriate configs
    };

    $scope.saveDash = function(index){

        var item = $scope.model.dashboardItems[index];

        item.configMode = false;

        $timeout(function(){
            item.init($dashboards, index);
        }, 1);

    };

    $scope.removeItem = function(index){

        console.log("removendo item:", index);
        var item = $scope.model.dashboardItems[index];

        item.destroy();

        this.model['dashboardItems'].splice( index, 1 );

        $layoutManager.remove_widget($('li', $dashboards).eq(index));

    };

    $scope.renderCompleteDashItem = function(container) {

        var $container = angular.element(container);

        var $items = $container.find('li');

        // Wait to render/css...
        $timeout(function(){

            // Check if layout manager has initialized
            if(!$layoutManager){
                configureLayoutManager();
            }

            console.log('renderCompleteDashItem : length:' + $items.length);

            angular.forEach($scope.model.dashboardItems, function(item, index) {

                if(!item.initialized){

                    var $item = $($items.get(index));

                    if( ! $item.data('inLayoutManager')){ // new added by user

                        console.log('new added by user');

                        $item.data('inLayoutManager', true);

                        $layoutManager.add_widget($item, 1, 1);
                    }

                    if(!item.configMode) {
                        console.log('Initializing: ', item);
                        item.init(container, index);
                    }

                }
            });
        }, 100);

    };

    /** Get Icon for device */
    $scope.getIcon = function(id){
        var device = findDevice(id);
        var cname = "";

        if(device.category == DCategory.LAMP){
            cname += "ic-lightbulb-";
        }

        if(device.category == DCategory.POWER_SOURCE){
            cname += "ic-battery-";
        }

        if(device.sensor){
            cname += "ic-sensor-";
        }

        if(device.value == 1){
            cname += "on";
        }else{
            cname += "off";
        }

        return cname;
    }

    /**
     * Find device by ID
     * @param {Number} id
     * @returns {*}
     */
    function findDevice(deviceID){
        var devices = $scope.model.devices;
        if(devices){
            for(var i = 0; i < devices.length; i++){
                if(devices[i].id == deviceID){
                    return devices[i];
                }
            }
        } else{
            console.warn("Devices not loaded or empty !");
        }

        return null;
    }

    function playSound(device){

        if(audioContext && device.type == od.DeviceType.DIGITAL){
            audioPlay = audioContext.createOscillator();
            audioPlay.type = 3;
            if(device.value == 0){
                audioPlay.frequency.value = 700;
            }else{
                audioPlay.frequency.value = 800;
            }
            audioPlay.connect(audioContext.destination);

            var now = audioContext.currentTime;

            if(audioPlay && audioPlay.noteOn){
                audioPlay.noteOn( now );
                audioPlay.noteOff( now + 0.05 ); // "beep" (in seconds)
            }else{
                console.error("audioPlay not working !");
            }


        }
    }


    function configureLayoutManager(){

        var  gridConf = {
            widget_margins: [5, 5],
            max_cols: 6,
            min_cols: 5,
            //max_rows : 3,
            //extra_cols : 0,
            //max_size_x : 6,
            serialize_params: function($w, wgd) {
                return { id : $w.data('itemid'), layout : "["+wgd.row+","+wgd.col+","+wgd.size_x+","+wgd.size_y+"]" }
            },
            resize: {
                enabled: true,
                // @function: OnResize
                stop : function (e, ui, $widget){
                    var dashView = $widget.scope().item;
                    dashView.onResize();

                    var item = {
                        dashID : $scope.model.dashboard.id,
                        id : dashView.id,
                        layout : "["+ $widget.data('row') + "," + $widget.data('col')+ "," + $widget.data('sizex')+ "," + $widget.data('sizey') + "]"
                    };

                    // Save on database.
                    Dashboard.updateLayout(item);
                }
            },
            draggable : {
                stop : function (e, ui){

                    var gridster = $("ul", $dashboards).data('gridster');

                    var changed = gridster.serialize_changed( );

                    console.log("changed:", changed)

                    angular.forEach(changed, function(item, index) {
                        item.dashID = $scope.model.dashboard.id;
                        // Save on database.
                        Dashboard.updateLayout(item);
                    });

                    gridster.$changed = $([]);

                }
            }
        };

        var blockWidth = $dashboards.width();
        blockWidth = (blockWidth / gridConf.min_cols).toFixed() - 10;
        gridConf.widget_base_dimensions = [blockWidth, 150];

        $layoutManager = $(".dashboards > ul").gridster(gridConf).data('gridster');

        $('li', $dashboards).each(function(){
            $(this).data('inLayoutManager', true);
        });
    }

}]);
