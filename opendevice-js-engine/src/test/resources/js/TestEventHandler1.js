/**
 * @name EventHookTest
 * @devices 1
 * @description TestCase for EventHookTest
 * @type JavaScript
 */

// Sensor de presença ativado
// Quando alguem passar na sala a noite.

print("RUNNING CODE for: " + testdesc);

if(device.uid == 1){
    if(device.isON()){

        var device2 = findDevice(2);

        print("1=ON -- Device2 val: " + device2.value + ", device2: " +device2.name);

        device2.name = "LedJS 2";
        device2.on();

    }
}