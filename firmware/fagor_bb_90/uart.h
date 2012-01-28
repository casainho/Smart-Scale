#ifndef UART_H
#define UART_H

void uart_init(void);
void tx_fifo_send(unsigned char byte);
unsigned char rx_fifo_data_available(void);
unsigned char rx_fifo_receive(void);
void tx_fifo_writestr(unsigned char *data);

#endif
