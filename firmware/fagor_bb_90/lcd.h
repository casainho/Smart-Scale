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


#define		CLR_DISP	   0x00000001
#define		DISP_ON		   0x0000000C
#define		DISP_OFF	   0x00000008
#define		CUR_HOME       0x00000002
#define		CUR_OFF 	   0x0000000C
#define     CUR_ON_UNDER   0x0000000E
#define     CUR_ON_BLINK   0x0000000F
#define     CUR_LEFT       0x00000010
#define     CUR_RIGHT      0x00000014
#define		CUR_UP  	   0x00000080
#define		CUR_DOWN	   0x000000C0
#define     ENTER          0x000000C0
#define		DD_RAM_ADDR    0x00000080
#define		DD_RAM_ADDR2   0x000000C0

/* Delay constant for use with LCD communications */
#define 	LCD_CTRL_K_DLY 20

void e_pulse (void);
void lcd_init (void);
void lcd_send_command (unsigned char byte);
void lcd_smartup (void);
void lcd_smartdown(void);
void lcd_send_char (unsigned char byte);
void lcd_send_int (long number, unsigned char number_of_digits);
void lcd_send_str (unsigned char *string);
void lcd_send_float (double number, unsigned char number_of_digits, \
        unsigned char number_of_floats);
void lcd_send_string (const char *string);


