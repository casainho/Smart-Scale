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

/*
 * LCD input
 * Pin nr  | LPC2103 pin
 * ---------------------
 *  1  | P0.4
 *  2  | P0.5
 *  3  | P0.24
 *  4  | P0.25
 *  5  | P0.8
 *  6  | P0.9
 *  7  | P0.10
 *  8  | P0.11
 *  9  | P0.12
 * 10  | P0.13
 * 11  | P0.19
 * 12  | P0.20
 * 13  | P0.16 (EINT0 - external interrupt used. This is a signal from backplane 1)
 */

#define LCD_PIN_01 4
#define LCD_PIN_02 5
#define LCD_PIN_03 24
#define LCD_PIN_04 25
#define LCD_PIN_05 8
#define LCD_PIN_06 9
#define LCD_PIN_07 10
#define LCD_PIN_08 11
#define LCD_PIN_09 12
#define LCD_PIN_10 13
#define LCD_PIN_11 19
#define LCD_PIN_12 20
#define LCD_PIN_13 16

/* LCD input signals */
/*
 *              0.0 kg
              _   _
             | | | |
              -   -
             | | | |
              - . -  kg

          bp a:   1 1 1 . 1 1 1 . 1 0 0 . 1 0 0

          bp b:   1 1 1 . 1 1 1 . 0 1 0 . 0 1 0

          bp c:   1 1 1 . 0 1 1 . 1 0 0 . 0 0 0

               a          y a b
             f   b        f g c
               g          z e d
             e   c
               d
 */

#define mask_1st_digit_bpa 1536
#define mask_1st_digit_bpb 3584
#define mask_1st_digit_bpc mask_1st_digit_bpa
#define mask_2nd_digit_bpa 192
#define mask_2nd_digit_bpb 448
#define mask_2nd_digit_bpc mask_2nd_digit_bpa
#define mask_3rd_digit_bpa 24 /* 0b000000011000 */
#define mask_3rd_digit_bpb 56 /* 0b000000111000 */
#define mask_3rd_digit_bpc mask_3rd_digit_bpa /* 0b000000011000 */
#define mask_4th_digit_bpa 3
#define mask_4th_digit_bpb 7
#define mask_4th_digit_bpc mask_4th_digit_bpa

void ios_init (void);
unsigned char io_is_set (unsigned char io_number);
unsigned long int get_ios (void);
char number_to_digit (unsigned char *number, unsigned char *digit);
char get_weight (unsigned long int back_plane_a,
        unsigned long int back_plane_b,
        unsigned long int back_plane_c,
        float *weight);
unsigned long int format_back_plane (unsigned long int back_plane);
