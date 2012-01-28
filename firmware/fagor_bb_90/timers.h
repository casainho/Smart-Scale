/*
 * SDCard Bathroom Scale
 *
 * Copyright (C) Jorge Pinto aka Casainho, 2009.
 *
 *   casainho [at] gmail [dot] com
 *     www.casainho.net
 *
 * Released under the GPL Licence, Version 3
 */

extern volatile unsigned short int timer1_counter;

void timer1_init (void);
unsigned long millis(void);
void delay_ms(unsigned long ms);
unsigned long micros(void);
void delay_us(unsigned long us);
