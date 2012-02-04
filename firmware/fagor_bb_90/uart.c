/*
 * Copyright (C) Jorge Pinto aka Casainho, 2012.
 *
 *   casainho [at] gmail [dot] com
 *     www.casainho.net
 *
 * Released under the GPL Licence, Version 3
 */

#include "lpc210x.h"
#include "serial_fifo.h"
#include "isrsupport.h"
#include "main.h"

void uart0_int_handler (void) __attribute__ ((interrupt("IRQ")));

fifo_t rxfifo;
fifo_t txfifo;
unsigned char rxbuffer[SERIAL_FIFO_SIZE];
unsigned char txbuffer[SERIAL_FIFO_SIZE];

volatile unsigned char uart_transmitting = 0;

void uart0_disable_THR(void)
{
  UART0_IER &= ~2;
}

void uart0_enable_THR(void)
{
  UART0_IER |= 2;
}

void uart_init(void)
{
  /* Init SerialFifos */
  fifo_init(&rxfifo, rxbuffer);
  fifo_init(&txfifo, txbuffer);
/****************************************************************************/

  /* Initialize Pin Select Block for TXD0 and RXD0 */
  PINSEL0=0x5;

  /* Enable FIFO's and reset them */
  UART0_FCR=0x7;

  /* Set DLAB and word length set to 8bits */
  UART0_LCR=0x83;

#ifdef XTAL_12000000HZ
  /* Baud rate set to 9600 */
  /* CPU clock = pheripherial (VPB) clock = 48000000Hz
   *
   * Required baud rate = VPB clock / (16 * Divisor value)
   * 9600 = 48000000 / (16 * Divisor value)
   * Divisor value = 312,5
   * */
  UART0_DLL=56;
  UART0_DLM=1;
  /* Clear DLAB */
  UART0_LCR=0x3;
/****************************************************************************/

#elif defined XTAL_14745600HZ
  /* Baud rate set to 9600 */
  /* CPU clock = peripheral (VPB) clock = 58982400Hz
   *
   * Required baud rate = VPB clock / (16 * Divisor value)
   * 9600 = 58982400 / (16 * Divisor value)
   * Divisor value = 384
   * */
  UART0_DLL=128;
  UART0_DLM=1;
  /* Clear DLAB */
  UART0_LCR=0x3;
/****************************************************************************/

#else
#ERROR XTAL frequ need to be defined
#endif

  /* Initialize VIC */
  VICINTSEL &= ~(1 << 6); /* UART0 selected as IRQ */
  VICINTEN |= (1 << 6); /* UART0 interrupt enabled */
  VICVECTCNTL0 = 0x26;
  VICVECTADDR0 = (unsigned long) uart0_int_handler; /* Address of the ISR */

  /* Enable UART0 specific interrupts */
  //UART0_IER = 3; // enable Receive Data Available interrupt
                 // enable Transmitting Hold Register get Empty
  UART0_IER = 1; // just RDA
}

void uart0_int_enable(void)
{
  enableIRQ();
}

void uart0_int_disable(void)
{
  disableIRQ();
}

void uart0_int_handler (void)
{
  unsigned long status = (UART0_IIR & 14) >> 1;
  unsigned char c1, c2 = 0, t;

  if (status == 2)
  {
    // RDA -- Receive Data Available
    fifo_put(&rxfifo, UART0_RBR);
  }
  else if (status == 1)
  {
    /*
     * THR -- Transmitting Hold Register get Empty
     * */

    /* If there is nothing to transmit, signal and exit */
    if (fifo_avail(&txfifo) == 0)
    {
      uart_transmitting = 0;

      /* Clear the interrupt flag */
      VICVECTADDR = 0xff;
      return;
    }

    /* Send data from fifo to THR, in 16 packet bytes if they are available */
    for (c1 = fifo_avail(&txfifo); c1 > 0; c1--)
    {
      if (c2 < 16 && c1 > 0)
      {
        fifo_get(&txfifo, &t);
        UART0_THR = t;
        c2++;
      }
      else
      {
        break;
      }
    }

    /* If there is nothing to transmit, signal */
    if (fifo_avail(&txfifo) == 0)
    {
      uart_transmitting = 0;
    }
  }
  else if (status == 3)
  {
    t = UART0_LSR;
  }

  /* Clear the interrupt flag */
  VICVECTADDR = 0xff;
}

void _tx_fifo_send(unsigned char byte)
{
  // block if fifo is full
  while (!(fifo_put(&txfifo, byte))) ;
}

// Atomic version
void tx_fifo_send(unsigned char byte)
{
#if 0
  uart0_int_disable();

  /* If UART is transmitting, put byte in tx_fifo...
   * UART transmitting interrupt will send later the byte from tx_fifo */
  if (uart_transmitting)
  {
    _tx_fifo_send(byte);
  }
  else
  { /* Send right now the byte. UART transmitting interrupt will also be started */
    UART0_THR = byte;
    uart_transmitting = 1;
  }

  uart0_int_enable();
#endif

  // No use of UART FIFO and interrupt
  while (!(UART0_LSR & (1 << 6))) ;
  UART0_THR = byte;
}

unsigned char _rx_fifo_data_available(void)
{
  return (unsigned char) fifo_avail(&rxfifo);
}

// Atomic version
unsigned char rx_fifo_data_available(void)
{
  unsigned char t;

  uart0_int_disable();
  t = _rx_fifo_data_available();
  uart0_int_enable();
  return t;
}

unsigned char _rx_fifo_receive(void)
{
  unsigned char byte;
  fifo_get(&rxfifo, &byte);

  return byte;
}

// Atomic version
unsigned char rx_fifo_receive(void)
{
  unsigned char t;
  uart0_int_disable();
  t = _rx_fifo_receive();
  uart0_int_enable();
  return t;
}

