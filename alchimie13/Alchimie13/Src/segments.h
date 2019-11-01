#ifndef __SEGMENTS_H
#define __SEGMENTS_H

#include "stm32wbxx_hal.h"

int segments_init(I2C_HandleTypeDef * i2c, uint16_t deviceAddress);
int segments_print(I2C_HandleTypeDef * i2c, uint16_t deviceAddress, char * text);

#endif // __SEGMENTS_H
