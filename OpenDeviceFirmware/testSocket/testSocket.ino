#include <UIPEthernet.h>

uint8_t mac[6] = {0x00,0x01,0x02,0x03,0x04,0x05};
IPAddress myIP(192,168,0,204);
EthernetServer server = EthernetServer(8081);

void setup()
{
  Serial.begin(9600);

  Ethernet.setSSPIN(4,4); // Default is 10;
  Ethernet.begin(mac,myIP);

  server.begin();
}

void loop()
{

  Serial.println("Aguardando clente...");
  if (EthernetClient client = server.available()){
    
    Stream *st = (Stream*) &client; 
    
      if (client)
        {
          while(st->available() > 0){
       
              char c = st->read();
              Serial.write("READ:");
              Serial.write(c);
              
              client.write("READ:");
              client.write(c);
              client.write("\n\r");
              /// free(msg);
            }
        }
    }
    
    delay(1000);
}

