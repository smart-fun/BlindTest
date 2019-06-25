
#include "segments.h"

uint8_t chars[128];
uint8_t charBuffer[] = {0,0x5C, 1,0x5C, 2,0, 3,0x5C, 4,0x5C};

// private
void initChars();

int segments_init(I2C_HandleTypeDef * i2c, uint16_t deviceAddress) {

	initChars();

	deviceAddress <<= 1;
	uint32_t DELAY = 500;

	HAL_StatusTypeDef result = 0;
	for(int i=0; i<10; ++i) {
	  result = HAL_I2C_IsDeviceReady(i2c, deviceAddress, 3, 200);
	  if (result == HAL_OK) {
		  break;
	  }
	  HAL_Delay(DELAY);
	}

	if (result != HAL_OK) {
	  return result;
	}

	uint8_t sysOn[] = {0x20 | 1};	// writeCommand(SYSTEM_SETUP_REGISTER, SYSTEM_SETUP_ON);
	result = HAL_I2C_Master_Transmit(i2c, deviceAddress, sysOn, 1, 100);
	if (result != HAL_OK) {
	  return result;
	}

	HAL_Delay(DELAY);
	uint8_t dispOn[] = {0x80 | 1 | 0}; // writeCommand(DISPLAY_SETUP_REGISTER, DISPLAY_SETUP_ON | DISPLAY_SETUP_BLINK_NONE);
	result = HAL_I2C_Master_Transmit(i2c, deviceAddress, dispOn, 1, 100);
	if (result != HAL_OK) {
	  return result;
	}

	HAL_Delay(DELAY);
	uint8_t bright[] = {0xE0| 7};	// writeCommand(BRIGTHNESS_REGISTER, 7);
	result = HAL_I2C_Master_Transmit(i2c, deviceAddress, bright, 1, 100);

	return result;
}

void initChars() {
	for(int i=0; i<128; ++i) {
		chars[i] = 0x40;
	}
	chars['0'] = 0x3F;
	chars['1'] = 0x06;
	chars['2'] = 0x5B;
	chars['3'] = 0x4F;
	chars['4'] = 0x66;
	chars['5'] = 0x6D;
	chars['6'] = 0x7D;
	chars['7'] = 0x07;
	chars['8'] = 0x7F;
	chars['9'] = 0x6F;
}

int segments_print(I2C_HandleTypeDef * i2c, uint16_t deviceAddress, char * text) {
	deviceAddress <<= 1;
	for(int i=0; i<4; ++i) {
		if (i <strlen(text)) {
			char c = text[i];
			if (i<2) {
				charBuffer[i*2 + 1] = chars[c];
			} else {
				charBuffer[i*2 + 3] = chars[c];	// skip middle : character
			}
		}
	}
	return HAL_I2C_Master_Transmit(i2c, deviceAddress, charBuffer, 10, 100);
}

