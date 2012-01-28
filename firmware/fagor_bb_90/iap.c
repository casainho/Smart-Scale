/*
 * Copyright (C) Jorge Pinto aka Casainho, 2012.
 *
 *   casainho [at] gmail [dot] com
 *     www.casainho.net
 *
 * Released under the GPL Licence, Version 3
 */

#include "iap.h"
#include "isrsupport.h"
#include "main.h"

#define IAP_ADDRESS 0x7FFFFFF1
#define LAST_SECTOR_ADDRESS 0x00007000
#define LAST_SECTOR_ADDRESS_VALUE(x)   (*((volatile unsigned long *) (0x00007000+(x*4))))

static unsigned param_table[5];
static unsigned result_table[5];

unsigned int lpc_flash_array[256];

void iap_entry(unsigned param_tab[], unsigned result_tab[])
{
  void (*iap)(unsigned [], unsigned []);

  iap = (void (*)(unsigned [], unsigned []))IAP_ADDRESS;
  iap(param_tab,result_tab);
}

void write_data(unsigned cclk, unsigned dst, unsigned * flash_data_buf, unsigned count)
{
  disableIRQ();
  param_table[0] = COPY_RAM_TO_FLASH;
  param_table[1] = dst;
  param_table[2] = (unsigned)flash_data_buf;
  param_table[3] = count;
  param_table[4] = cclk;
  iap_entry(param_table,result_table);
  enableIRQ();
}

void erase_sector(unsigned start_sector, unsigned end_sector, unsigned cclk)
{
  disableIRQ();
  param_table[0] = ERASE_SECTOR;
  param_table[1] = start_sector;
  param_table[2] = end_sector;
  param_table[3] = cclk;
  iap_entry(param_table,result_table);
  enableIRQ();
}

void prepare_sector(unsigned start_sector,unsigned end_sector)
{
  disableIRQ();
  param_table[0] = PREPARE_SECTOR_FOR_WRITE;
  param_table[1] = start_sector;
  param_table[2] = end_sector;
  iap_entry(param_table,result_table);
  enableIRQ();
}

void save_flash_array(unsigned int *array)
{
  unsigned char result;
  prepare_sector(7, 7);
  result = result_table[0];
  if (result != CMD_SUCCESS)
  {
    while (1) ;
  }

#ifdef XTAL_12000000HZ
  erase_sector(7, 7, 48000);
#elif defined XTAL_14745600HZ
  erase_sector(7, 7, 53236);
#endif
  result = result_table[0];
  if (result != CMD_SUCCESS)
  {
    while (1) ;
  }

  prepare_sector(7, 7);
  result = result_table[0];
  if (result != CMD_SUCCESS)
  {
    while (1) ;
  }

#ifdef XTAL_12000000HZ
  write_data(48000, LAST_SECTOR_ADDRESS, array, 256);
#elif defined XTAL_14745600HZ
  write_data(53236, LAST_SECTOR_ADDRESS, lpc_flash_array, 256);
#endif
  result = result_table[0];
  if (result != CMD_SUCCESS)
  {
    while (1) ;
  }
}

void load_flash_array(unsigned int *array)
{
  array[0] = LAST_SECTOR_ADDRESS_VALUE(0); //Bluetooh setup module name
}
