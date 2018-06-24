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

/*
 * Load Extensions and Call Angular bootstrap
 */

// Load dynamic plugins....
// FIXME: https://github.com/OpenDevice/OpenDevice/issues/129
function loadExtensions(extensions){
    var scripts = [];

    var ui_extenions = [];

    // This will be used in js/app.js to load extra modules.
    localStorage.setItem("odev.extensions", JSON.stringify(extensions));

    extensions.forEach(function(item, index){
        ui_extenions.push('ext.'+item.pathName);

        scripts.push($.getScript(item.loadScript)
            .done(function( script, textStatus ) {
                console.log( "Extensoin loaded: " + item.loadScript);
            })
            .fail(function( jqxhr, settings, exception ) {
                console.error("Extensoin load error", exception);
            }));
    });

    $.when.apply($, scripts).done(function() {
        angular.element(function() {
            console.log("Initializing App.. (extensions: " + JSON.stringify(ui_extenions)+")");
            new App();
            angular.bootstrap(document, ['opendevice']);
        });
    });

    // $.when(scripts).then(function(){
    //
    // });
}

$.get('/admin/viewExtensions', function(data){
    loadExtensions(data);
});