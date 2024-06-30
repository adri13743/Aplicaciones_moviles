// STUFF NEEDED FOR WIFI ***********
#include <WiFi.h>
const char* ssid     = "Dru13743";
const char* password = "137DRU43AdiOS";
//*********************************
// STUFF NEEDED FOR UDP ***********
#include <AsyncUDP.h>
AsyncUDP udp; //maneja multiples conuxiones udp
unsigned int localUdpPort = 9009;  // Puerto UDP local al que el ESP32 estará escuchando
char incomingPacket[255];          // Almacenar el paquete UDP recibido
char replyPacket1[] = "okko";  // Respuesta a enviar al cliente UDP
char replyPacket2[] = "notokko";  // Respuesta a enviar al cliente UDP
char contra[] = "a23GF23cc";
//*********************************
int relayPin = 25; //G25
unsigned long startMillis = 0;
unsigned long currentMillis = 0;
const unsigned long tiempoEspera = 3000;  // 3 segundos

void setup()
{
    Serial.begin(115200);
    delay(1000);
    WiFi.begin(ssid,password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(300);
    }
    Serial.println("WiFi connected.");
    Serial.println("IP address: ");
    Serial.println(WiFi.localIP());
    pinMode(relayPin, OUTPUT);
    digitalWrite(relayPin, 1); // RELE NO PASA CORRIENTE
    if (udp.listen(localUdpPort)) {
    Serial.println("Servidor UDP iniciado en el puerto " + String(localUdpPort));
    udp.onPacket([](AsyncUDPPacket packet) {
    // Leer el paquete UDP en el búfer
    int len = packet.length();
    packet.read(reinterpret_cast<uint8_t*>(incomingPacket), len);
    incomingPacket[len] = 0;  // Asegurarse de que el búfer de cadena tenga un terminador nulo
    // Imprimir la dirección IP y el puerto del cliente
    Serial.print("IP del cliente: ");
    Serial.print(packet.remoteIP());
    Serial.print(", Puerto del cliente: ");
    Serial.println(packet.remotePort());
    // Imprimir la contraseña recibida
    Serial.print("Contrasena recibida: ");
    Serial.println(incomingPacket);
    String cont = "h56NM56jj";
    int claveCesar = 33; // Puedes ajustar la clave según sea necesario
    String contras = cesarDecrypt(cont, claveCesar);
    Serial.println("Contras descifrada: " + contras);
    String contrasenaDescifrada = cesarDecrypt(incomingPacket, claveCesar);
    Serial.print("Contrasena descifrada: ");
    Serial.println(contrasenaDescifrada);
    if (contrasenaDescifrada.equals(contra)) {
        Serial.print("La contrasena es valida");
        digitalWrite(relayPin, 0);  // Activa el relé
        startMillis = millis();
        // No bloquear el bucle principal, esperar hasta que haya pasado el tiempo deseado
        while (millis() - startMillis < tiempoEspera) {
          // Aquí puedes realizar otras tareas si es necesario
        }
        digitalWrite(relayPin,1);  // desactiva el relé
        packet.printf(replyPacket1);
      } else {
        Serial.print("La contrasena no es valida");
        packet.printf(replyPacket2);
      }
    });
  }
}

// Función para descifrar un mensaje cifrado con el cifrado César
String cesarDecrypt(String input, int key) {
  String result = "";

  for (size_t i = 0; i < input.length(); i++) {
    char caracter = input.charAt(i);

    // Descifrar letras
    if (isAlpha(caracter)) {
      char base = isUpperCase(caracter) ? 'A' : 'a';
      int offset = (caracter - base - key + 26) % 26;
      if (offset < 0) {
        offset += 26;
      }
      result += (char)(offset + base);
    }
    // Descifrar números
    else if (isDigit(caracter)) {
      int offset = (caracter - '0' - key + 10) % 10;
      if (offset < 0) {
        offset += 10;
      }
      result += (char)(offset + '0');
    }
    // Mantener caracteres no alfabéticos sin cambios
    else {
      result += caracter;
    }
  }

  return result;
}








void loop(){
 
}
