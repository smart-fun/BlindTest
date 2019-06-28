/*
 * leds.c
 *
 *  Created on: 27 juin 2019
 *      Author: Arnaud
 */

#include "leds.h"

static uint8_t led_data[2 + (NUM_LEDS * 12)];

void setLedColor(int led, int red, int green, int blue) {
	int offset = 2 + (led * 12);

	int color = (green << 16) + (red << 8) + blue;
	int highQuartet = 1;
	uint8_t byte = 0;
	for(int bit= 0; bit<24; ++bit) {
		int value = (color >> 23) & 1;	// 0 or 1

		if (highQuartet) {
			// set high quartet part
			byte = (value == 0) ? 0x80 : 0xC0;
		} else {
			// add low quartet part, and set buffer
			byte += (value == 0) ? 0x08 : 0x0C;
			led_data[offset++] = byte;
		}
		highQuartet = 1 - highQuartet;	// invert true/false

		color = color << 1;
	}
}

void setAllLedsColor(int red, int green, int blue) {
	for(int led=0; led<NUM_LEDS; ++ led) {
		setLedColor(led, red, green, blue);
	}
}

void resetLeds() {
	for(int i=0; i<NUM_LEDS; ++i) {
		setLedColor(i, 0,0,0);
	}
}

void updateLeds(SPI_HandleTypeDef * spi) {
	HAL_SPI_Transmit_DMA(spi, led_data, sizeof(led_data));
	HAL_Delay(1);
}

void rotateLeds() {
	uint8_t tmp[12];

	// save 1st led values
	uint8_t * src = led_data + 2;
	for(int i=0; i<12; ++i) {
		tmp[i] = *src;
		++src;
	}

	// scroll all leds to previous one
	for(int led=0; led<NUM_LEDS-1; ++led) {
		for(int byte=0; byte<12; ++byte) {
			led_data[2 + (led*12) + byte] = led_data[2 + ((led+1)*12) + byte];
		}
	}

	// copy save led to last led
	uint8_t * dst = led_data + 2 + ((NUM_LEDS-1) * 12);
	for(int i=0; i<12; ++i) {
		*dst = tmp[i];
		++dst;
	}

}

