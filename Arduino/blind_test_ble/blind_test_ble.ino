#include <bluefruit.h>
//#include <Adafruit_LittleFS.h>
//#include <InternalFileSystem.h>
#include "matrix88.h"
//#include <GyverMAX7219.h> // https://github.com/GyverLibs/GyverMAX7219
//#include "LedControl.h"

BLEUart bleuart; // uart over ble
MatrizLed pantalla;
//MAX7219 < 2, 1, 11, 13, 12> mtrx; // W, H, CS, Data, Clck
//MAX7219 < 2, 1, 11> mtrx; // W, H, CS, Data, Clck


/*
 Now we need a LedControl to work with.
 ***** These pin numbers will probably not work with your hardware *****
 pin 12 is connected to the DataIn 
 pin 11 is connected to the CLK 
 pin 10 is connected to LOAD 
 We have only a single MAX72XX.
 */
//LedControl lc=LedControl(13,12,11,2);

const byte interruptPin = 16;

void setup() {
  Serial.begin(115200);

#if CFG_DEBUG
  // Blocking wait for connection when debug mode is enabled via IDE
  while ( !Serial ) yield();
#endif

  pinMode(interruptPin, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(interruptPin), blink, CHANGE);

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

  /*lc.shutdown(0,false);
  lc.setIntensity(0,8);
  lc.clearDisplay(0);
  lc.shutdown(1,false);
  lc.setIntensity(1,8);
  lc.clearDisplay(1);
  */
}

uint8_t value = 0;
uint8_t buf[64];

uint8_t receive_data[64];
uint8_t receive_index;

void loop() {
  // put your main code here, to run repeatedly:

  delay(100);

//  buf[0] = value;
  //++value;
//  writeData(buf, 1);

  // uint8_t buf[64];
  // writeData(buf, size);

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

//  pantalla.borrar();
//  //pantalla.escribirFrase("Hi");
//  pantalla.escribirCifra(0, 0);
//  pantalla.escribirCifra(4, 1);
  
  //mtrx.clear();
  //mtrx.circle(3, 3, 2);
  //mtrx.line(0, 0, 15, 7);
  //mtrx.update();
/*
lc.clearDisplay(0);
lc.clearDisplay(1);

  lc.setLed(0,1,1,true);
  lc.setLed(0,2,2,true);
  lc.setLed(0,3,3,true);

  lc.setLed(1,1,1,true);
  lc.setLed(1,2,2,true);
  lc.setLed(1,3,3,true);
  */
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

void blink() {
  ++value;
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
    Serial.print("Receive Press");
    int address = (command == 'R') ? 0 : 1;
    uint8_t data = -1;
    uint8_t index = 1;
    uint64_t value = 0;
    while (data != 0) {
      data = receive_data[index];
      ++index;
      value *= 26;
      value += data;
    }
    //pantalla.borrar();
    // value is now 64 bits, to cut into 8x8 bits
    for(int col=0; col<8; ++col) {
      uint8_t colValue = value & 0xff;
      value >>= 8;
      for(int row=0; row<8; ++row) {
        boolean on = (colValue & 1);
        pantalla.setLed(address, row, col, on);
        colValue >>= 1;
      }
    }
  }
  receive_index = 0;
}
