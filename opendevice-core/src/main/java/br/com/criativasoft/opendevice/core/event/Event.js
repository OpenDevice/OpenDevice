// Sensor de presença ativado
// Quando alguem passar na sala a noite.

if (device.isON() && Time.between(19, 6)) {

    var light = getDevice('casa::LuzSala1');

    // reset timer
    if (light.isON()) {

        Tasks.reset(device);

    } else {

        light.on();

        Tasks.every(1000, function () {
            var light2 = getDevice('casa::LuzFeedback');
            light2.toggle();
        });

        Tasks.after(env['PRESENCE_DELAY'], SECONDS, function () {
            light.setValue(0);
        });

    }

}