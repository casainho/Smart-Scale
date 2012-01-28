/*
 * Smart Scale
 *
 * Copyright (C) Jorge Pinto aka Casainho, 2012.
 *
 *   casainho [at] gmail [dot] com
 *     www.casainho.net
 *
 * Released under the GPL Licence, Version 3
 */

#include "lpc210x.h"
#include "system.h"
#include "isrsupport.h"
#include "lcd.h"
#include "timers.h"
#include "ios.h"
#include "err.h"
#include "uart.h"
#include "serial_fifo.h"
#include "iap.h"
#include "main.h"
#include "comms.h"
#include "sersendf.h"

#ifndef NULL
#define NULL    0
#endif

#define ON 1
#define OFF 0

/* Global variables */
volatile unsigned long int           back_plane_a,
                                last_back_plane_a,
                                     back_plane_b,
                                last_back_plane_b,
                                     back_plane_c,
                                last_back_plane_c;

volatile unsigned short int timer1_counter = 0;
volatile unsigned char new_time = 0;

void power_switch (unsigned char state)
{
    if (state)
    {
        IODIR |= (1 << 26); /* Power control switch io pin as output */
        IOSET = (1 << 26); /* Turn on power for scale (the other
        switch controlled by hardware should be on until this time) */
    }

    else
    {
        IODIR &= ~(1 << 26); /* Power control switch io pin as input */
        IOCLR = (1 << 26); /* Turning off the power for scale */
    }
}

int main (void)
{
    /* Initialize variables */
    float weight = 0;
    volatile unsigned long millis_counter = millis();

	/* Initialize the system */
    system_init ();

    /* Initialize the IOs */
    ios_init ();

    /* Turn the power switch ON. Power should be ON on this stage because the
     * other hardware switch should be ON for at least 4 seconds. */
    power_switch (ON);

    /* Initialize the LCD */
    lcd_init ();

    uart_init();

    /* Initialize the Timer1 */
    timer1_init ();
    enableIRQ ();

    lcd_send_command (DD_RAM_ADDR); /* LCD set first row */
    lcd_send_string ("    ----- Kg    ");
    //sersendf("Smart Scale initialization... \n");

#if 0
    int c = 0;
    while (1)
    {
    	for (c = 0; c < 121; c++)
    	{
    		sersendf("a %d\n", c);
    		delay_ms(1000);
    	}
    }
#endif

	for (;;)
	{
		if (io_is_set(LCD_PIN_13)) /* There are data on original LCD */
		{
			/* Acquire the signals from the LCD input */
			back_plane_c = get_ios ();
			while (io_is_set(LCD_PIN_13)) ;

			timer1_counter = 18;
			while (timer1_counter) ;
			back_plane_b = get_ios ();

			timer1_counter = 35;
			while (timer1_counter) ;
			back_plane_a = get_ios ();

			/* Write weight value on LCD only if there was no error when
			 *                                                     reading it */
			if (!(get_weight (
								last_back_plane_a,
								last_back_plane_b,
								last_back_plane_c,
								&weight)))
			{
				lcd_send_command (DD_RAM_ADDR); /* LCD set first row */
				lcd_send_string ("    ");
				lcd_send_float (weight, 3, 1);
				lcd_send_string (" Kg    ");

                /* Update Android */
                sersendf("a %d\n", (int) (weight * 10)); /* multiply for 10 to avoid print float */
			}

			/* Save the backplanes */
			last_back_plane_a = back_plane_a;
			last_back_plane_b = back_plane_b;
			last_back_plane_c = back_plane_c;

			millis_counter = millis();
		}

		else /* There are no data on original LCD */
		{
			if ((millis_counter + 1000) < millis()) // scale end of weight ??
			{
			   /* If weight is at least 1kg and
				*  less then 150Kg(scale maximum limit = 150kg)... */
				if (weight >= 1 && weight <= 150)
				{
	                /* Send final value to Android (3 times) */
	                sersendf("b %d\n", (int) (weight * 10)); /* multiply for 10 to avoid print float */
				    delay_ms(100);
				}

			    lcd_send_command (DD_RAM_ADDR2); /* LCD set first row */
			    lcd_send_string ("Shutting down...");
			    delay_ms(1000);

				/* Power switch OFF, the system shuts down himself */
				power_switch (OFF);
				for (;;) ;/* Hang here but system should shut off himself */
			}
		}
	}
}
