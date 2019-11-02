/*
 * leds.h
 *
 *  Created on: 27 juin 2019
 *      Author: Arnaud
 */

#ifndef LEDS_H_
#define LEDS_H_

#include "stm32wbxx_hal.h"

#define NUM_LEDS (24)

void resetLeds();
void setLedColor(int led, int red, int green, int blue);
void setAllLedsColor(int red, int green, int blue);
void updateLeds(SPI_HandleTypeDef * spi);
void rotateLeds();

void leds_playNote(int voice);
void leds_scrollNote();


#endif /* LEDS_H_ */
