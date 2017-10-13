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
// NOTE: Small/Simple controllers is here

var pkg = angular.module('opendevice.controllers');

pkg.controller('PageController', function ( $http, $scope) {

    // Alias / Imports
    var _this = this;
    var _public = this;

    // Public
    // ==========================

    // NProgress.configure({ minimum: 0.5 });
    //
    // $rootScope.$on('$routeChangeStart', function(next, current) {
    //     NProgress.start();
    // });
    //
    // $rootScope.$on('$routeChangeSuccess', function(next, current) {
    //     NProgress.done();
    // });


    $scope.$on("$routeChangeSuccess", function () {

        // Activate SideBar on Route Change
        var hrefs = window.location.hash; //html5: true
        angular.forEach($('ul.sidebar-menu a'), function (a) {
            a = angular.element(a);
            if (hrefs == a.attr('href')) {
                a.parent().addClass('active');
            } else {
                a.parent().removeClass('active');
            };
        });

        if(Utils.isMobile()){
            $('body').removeClass("sidebar-open");
        }


    });

    $scope.$on('$viewContentLoaded', function(){
        // Update title
        var $title = $("#page-title");
        $title.html("");
        $title.append($("span.page-title"));
    });

    $(function () {

        // Restore last page acessed, on session timeout Or if passed from external website.
        if(sessionStorage.getItem("LastPath")){
            window.location = sessionStorage.getItem("LastPath");
            sessionStorage.removeItem("LastPath");
        }

        // $('ul.sidebar-menu li').click( function() {
        //     $(this).addClass('active').siblings().removeClass('active');
        // });
    });


    _public.logout = function(){
        sessionStorage.setItem("logged", false);
        ODev.logout(function(){
            window.location = "/login.html?logout=true";
        });
    };

    /**
     * Replace Header ou another tag
     * @param target
     * @param el
     */
    _public.replace = function(target, el) {
        $(target).html("");
        $(target).append($(el));
    };

});