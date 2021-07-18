#include <bluefruit.h>
#include "matrix88.h"

BLEUart bleuart; // uart over ble
MatrizLed pantalla;

const byte interruptPinRed = 15;
const byte interruptPinYellow = 16;

void setup() {
  Serial.begin(115200);

#if CFG_DEBUG
  // Blocking wait for connection when debug mode is enabled via IDE
  while ( !Serial ) yield();
#endif

  pinMode(interruptPinRed, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(interruptPinRed), onRedPressed, FALLING);
  pinMode(interruptPinYellow, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(interruptPinYellow), onYellowPressed, FALLING);

  Bluefruit.autoConnLed(true);
  Bluefruit.configPrphBandwidth(BANDWIDTH_MAX);

  Bluefruit.begin();
  Bluefruit.setTxPower(4);    // Check bluefruit.h for supported values
  Bluefruit.setName("Blind Test");
  Bluefruit.Periph.setConnectCallback(connect_callback);
  Bluefruit.Periph.setDisconnectCallback(disconnect_callback);

  bleuart.begin();

  startAdv();

  Serial.println("Blind Test Advertising Started");

  pantalla.begin(13, 12, 11, 2); // dataPin, clkPin, csPin, numero de matrices de 8x8
  pantalla.rotar(false);

  pantalla.borrar();
  //  //pantalla.escribirFrase("Hi");
  pantalla.escribirCifra(4, 0);
  pantalla.escribirCifra(2, 1);

}

uint8_t value = 0;
uint8_t buf[64];

uint8_t receive_data[64];
uint8_t receive_index;

boolean redPressed = false;
boolean yellowPressed = false;

void loop() {
  // put your main code here, to run repeatedly:

  delay(50);

  while ( bleuart.available() )
  {
    uint8_t data = readData();
    receive_data[receive_index] = data;
    ++receive_index;
    if (receive_index >= 64) {
      receive_index = 0;
    }
    if (data == 0) {
      onDataReceived();
    }
  }

  if (redPressed) {
    sendRedPressed();
    delay(100);
    redPressed = false;
  }
  if (yellowPressed) {
    sendYellowPressed();
    delay(100);
    yellowPressed = false;
  }

}

void startAdv(void)
{
  // Advertising packet
  Bluefruit.Advertising.addFlags(BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE);
  Bluefruit.Advertising.addTxPower();

  // Include bleuart 128-bit uuid
  Bluefruit.Advertising.addService(bleuart);

  // Secondary Scan Response packet (optional)
  // Since there is no room for 'Name' in Advertising packet
  Bluefruit.ScanResponse.addName();

  /* Start Advertising
     - Enable auto advertising if disconnected
     - Interval:  fast mode = 20 ms, slow mode = 152.5 ms
     - Timeout for fast mode is 30 seconds
     - Start(timeout) with timeout = 0 will advertise forever (until connected)

     For recommended advertising interval
     https://developer.apple.com/library/content/qa/qa1931/_index.html
  */
  Bluefruit.Advertising.restartOnDisconnect(true);
  Bluefruit.Advertising.setInterval(32, 244);    // in unit of 0.625 ms
  Bluefruit.Advertising.setFastTimeout(30);      // number of seconds in fast mode
  Bluefruit.Advertising.start(0);                // 0 = Don't stop advertising after n seconds
}

void onRedPressed() {
  if (!redPressed && !yellowPressed) {
    redPressed = true;
  }
}

void onYellowPressed() {
  if (!redPressed && !yellowPressed) {
    yellowPressed = true;
  }
}

void sendRedPressed() {
  uint8_t buf[2];
  buf[0] = 'r';
  buf[1]= 0;
  writeData(buf, 2);
}

void sendYellowPressed() {
  uint8_t buf[2];
  buf[0] = 'y';
  buf[1]= 0;
  writeData(buf, 2);
}

void writeData(uint8_t buf[], uint8_t size) {

  bleuart.write( buf, size );

}

uint8_t readData() {
  return (uint8_t) bleuart.read();
}

// callback invoked when central connects
void connect_callback(uint16_t conn_handle)
{
  // Get the reference to current connection
  BLEConnection* connection = Bluefruit.Connection(conn_handle);

  char central_name[32] = { 0 };
  connection->getPeerName(central_name, sizeof(central_name));

  Serial.print("Connected to ");
  Serial.println(central_name);
}

/**
   Callback invoked when a connection is dropped
   @param conn_handle connection where this event happens
   @param reason is a BLE_HCI_STATUS_CODE which can be found in ble_hci.h
*/
void disconnect_callback(uint16_t conn_handle, uint8_t reason)
{
  (void) conn_handle;
  (void) reason;

  Serial.println();
  Serial.print("Disconnected, reason = 0x"); Serial.println(reason, HEX);
}

void onDataReceived() {
  uint8_t command = receive_data[0];
  if ((command == 'R') || (command == 'Y')) {
    Serial.println("Receive Press");
    int address = (command == 'R') ? 0 : 1;

    uint8_t data = -1;
    uint8_t index = 1;
    int x = 0;
    int y = 7;
    while (data != 0) {
      data = receive_data[index];
      ++index;
      int temp = data & 0x7F; // remove high bit
      for (int b = 6; b >= 0; --b) {
        boolean on = ((temp >> b) & 1);
        pantalla.setLed(address, x, y, on);
//        if (on) {
//          Serial.print("pixel ");
//          Serial.print(x);
//          Serial.print(" ");
//          Serial.println(y);
//        }
        ++x;
        if (x > 7) {
          x = 0;
          --y;
        }
      }
    }
  }

  receive_index = 0;
}

void print_uint64_t(uint64_t num) {

  char rev[128];
  char *p = rev + 1;

  while (num > 0) {
    *p++ = '0' + ( num % 10);
    num /= 10;
  }
  p--;
  /*Print the number which is now in reverse*/
  while (p > rev) {
    Serial.print(*p--);
  }
}
