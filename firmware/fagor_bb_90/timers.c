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

#include "lpc210x.h"

void timer1_int_handler (void)   __attribute__ ((interrupt("IRQ")));

extern unsigned short int timer1_counter;
unsigned long millis_ticks = 0;

void timer1_init (void)
{
    /* Initialize VIC */
    VICINTSEL &= ~(1 << 5); /* Timer 1 selected as IRQ */
    VICINTEN |= (1 << 5); /* Timer 1 interrupt enabled */
    VICVECTCNTL1 = 0x25;
    VICVECTADDR1 = (unsigned long) timer1_int_handler; /* Address of the ISR */

    /* Timer/Counter 1 power/clock enable */
    PCONP |= (1 << 2);

    /* Initialize Timer 1 */
    TIMER1_TCR = 0;
    TIMER1_TC = 0; /* Counter register: Clear counter */
    TIMER1_PR = 0; /* Prescaler register: Clear prescaler */
    TIMER1_PC = 0; /* Prescaler counter register: Clear prescaler counter */

    /* Match register 0:
     * Fclk = 58982400Hz; 100us => 0,0001/(1/58982400); 100us => ~ 5900 */
    TIMER1_MR0 = 5900;
    TIMER1_MCR = 3; /* Reset and interrupt on match */

    /* Start timer */
    TIMER1_TCR = 1;
}

/* This interrupt handler happens every 100us */
void timer1_int_handler (void)
{
    /* Clear the interrupt flag */
    TIMER1_IR = 1;
    VICVECTADDR = 0xff;

    if (timer1_counter > 0)
        timer1_counter--;

    millis_ticks++;
}

/* Atomic */
unsigned long millis(void)
{
  unsigned long t;
  VICINTEN &= ~(1 << 5); /* Timer1 interrupt disabled */
  t = millis_ticks/10;
  VICINTEN |= (1 << 5); /* Timer1 interrupt enabled */
  return t;
}

void delay_ms(unsigned long ms)
{
  long start = millis();
  while (millis() - start < ms)
    ;
}

/* Atomic */
long micros(void)
{
  return TIMER1_TC/10;
}

/* Always with ~2ms offset. delay_us(1) will be a delay of 3us */
void delay_us(unsigned long us)
{
  unsigned long start = micros();

  while (micros() - start < us)
    ;
}
