var led = new Device(1, DIGITAL);

addOutput(bluetooth("00:13:03:14:19:07"));

addInput(rest(8181));

connect();

