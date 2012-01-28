/*
 * Copyright (C) Jorge Pinto aka Casainho, 2012.
 *
 *   casainho [at] gmail [dot] com
 *     www.casainho.net
 *
 * Released under the GPL Licence, Version 3
 */

#ifndef  _IAP_H
#define  _IAP_H

#include "main.h"

typedef enum
{
  PREPARE_SECTOR_FOR_WRITE        = 50,
  COPY_RAM_TO_FLASH               = 51,
  ERASE_SECTOR                    = 52,
  BLANK_CHECK_SECTOR              = 53,
  READ_PART_ID                    = 54,
  READ_BOOT_VER                   = 55,
  COMPARE                         = 56,
  REINVOKE_ISP                    = 57
} IAP_Command_Code;

#define CMD_SUCCESS 0

extern unsigned int lpc_flash_array[256];

void save_flash_array(unsigned int *lpc_flash_array);
void load_flash_array(unsigned int *lpc_flash_array);

#endif /* _IAP_H */
