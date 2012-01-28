CC      = arm-none-eabi-gcc
LD      = arm-none-eabi-ld -v
AR      = arm-none-eabi-ar
AS      = arm-none-eabi-as
CP      = arm-none-eabi-objcopy
OD		= arm-none-eabi-objdump
SIZE		= arm-none-eabi-size

THUMB    =
THUMB_IW =

CFLAGS = -Wall -Wcast-align -Wcast-qual -Wimplicit
CFLAGS += -Wpointer-arith -Wswitch
CFLAGS += -Wredundant-decls -Wreturn-type -Wshadow -Wunused 
CFLAGS  += -mcpu=arm7tdmi $(THUMB_IW) -I./ -c -fno-common -g
O0 = -O0
OS = -O0 #-Os
AFLAGS  = -ahls -mapcs-32 -g -o crt.o
LDFLAGS  = -Map main.map -T linker_script-flash_memory.cmd
CPFLAGS = -O binary
ODFLAGS	= -x --syms


clean:
	-rm crt.lst crt.o main.o main.out main.map main.dmp main.bin \
	system.o timers.o lcd.o isrsupport.o ios.o err.o uart.o comms.o iap.o serial_fifo.o sermsg.o sersendf.o crc.o

all: main.out
	@ echo "...copying"
	$(CP) $(CPFLAGS) main.out main.bin
	$(OD) $(ODFLAGS) main.out > main.dmp
	$(SIZE) main.out

main.out: crt.o main.o system.o timers.o lcd.o isrsupport.o ios.o \
	err.o uart.o comms.o iap.o serial_fifo.o sermsg.o sersendf.o crc.o linker_script-flash_memory.cmd
	@ echo "..linking"
	$(LD) $(LDFLAGS) -o main.out crt.o main.o system.o timers.o lcd.o ios.o \
	err.o isrsupport.o uart.o comms.o iap.o serial_fifo.o sermsg.o sersendf.o crc.o libm.a libc.a libgcc.a

crt.o: crt.s
	$(AS) $(AFLAGS) crt.s > crt.lst

main.o: main.c
	$(CC) $(THUMB) $(CFLAGS) main.c
	
system.o: system.c
	$(CC) $(THUMB) $(CFLAGS) $(OS) system.c
	
timers.o: timers.c
	$(CC) $(CFLAGS) timers.c
	
lcd.o: lcd.c
	$(CC) $(THUMB) $(CFLAGS) $(O0) lcd.c
	
isrsupport.o: isrsupport.c
	$(CC) $(CFLAGS) $(OS) isrsupport.c

ios.o: ios.c
	$(CC) $(THUMB) $(CFLAGS) ios.c

err.o: err.c
	$(CC) $(THUMB) $(CFLAGS) $(OS) err.c
	
uart.o: uart.c
	$(CC) $(THUMB) $(CFLAGS) $(OS) uart.c	

comms.o: comms.c
	$(CC) $(THUMB) $(CFLAGS) $(OS) comms.c
	
iap.o: iap.c
	$(CC) $(THUMB) $(CFLAGS) $(OS) iap.c
	
serial_fifo.o: serial_fifo.c
	$(CC) $(THUMB) $(CFLAGS) $(OS) serial_fifo.c
	
sermsg.o: sermsg.c
	$(CC) $(THUMB) $(CFLAGS) $(OS) sermsg.c
	
sersendf.o: sersendf.c
	$(CC) $(THUMB) $(CFLAGS) $(OS) sersendf.c

crc.o: crc.c
	$(CC) $(THUMB) $(CFLAGS) $(OS) crc.c