################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/CDC.cpp \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/HID.cpp \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/HardwareSerial.cpp \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/HardwareSerial0.cpp \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/HardwareSerial1.cpp \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/HardwareSerial2.cpp \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/HardwareSerial3.cpp \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/IPAddress.cpp \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/Print.cpp \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/Stream.cpp \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/Tone.cpp \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/USBCore.cpp \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/WMath.cpp \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/WString.cpp \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/main.cpp \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/new.cpp 

C_SRCS += \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/WInterrupts.c \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/hooks.c \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/wiring.c \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/wiring_analog.c \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/wiring_digital.c \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/wiring_pulse.c \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/wiring_shift.c 

C_DEPS += \
./arduino/WInterrupts.c.d \
./arduino/hooks.c.d \
./arduino/wiring.c.d \
./arduino/wiring_analog.c.d \
./arduino/wiring_digital.c.d \
./arduino/wiring_pulse.c.d \
./arduino/wiring_shift.c.d 

AR_OBJ += \
./arduino/CDC.cpp.o \
./arduino/HID.cpp.o \
./arduino/HardwareSerial.cpp.o \
./arduino/HardwareSerial0.cpp.o \
./arduino/HardwareSerial1.cpp.o \
./arduino/HardwareSerial2.cpp.o \
./arduino/HardwareSerial3.cpp.o \
./arduino/IPAddress.cpp.o \
./arduino/Print.cpp.o \
./arduino/Stream.cpp.o \
./arduino/Tone.cpp.o \
./arduino/USBCore.cpp.o \
./arduino/WInterrupts.c.o \
./arduino/WMath.cpp.o \
./arduino/WString.cpp.o \
./arduino/hooks.c.o \
./arduino/main.cpp.o \
./arduino/new.cpp.o \
./arduino/wiring.c.o \
./arduino/wiring_analog.c.o \
./arduino/wiring_digital.c.o \
./arduino/wiring_pulse.c.o \
./arduino/wiring_shift.c.o 

CPP_DEPS += \
./arduino/CDC.cpp.d \
./arduino/HID.cpp.d \
./arduino/HardwareSerial.cpp.d \
./arduino/HardwareSerial0.cpp.d \
./arduino/HardwareSerial1.cpp.d \
./arduino/HardwareSerial2.cpp.d \
./arduino/HardwareSerial3.cpp.d \
./arduino/IPAddress.cpp.d \
./arduino/Print.cpp.d \
./arduino/Stream.cpp.d \
./arduino/Tone.cpp.d \
./arduino/USBCore.cpp.d \
./arduino/WMath.cpp.d \
./arduino/WString.cpp.d \
./arduino/main.cpp.d \
./arduino/new.cpp.d 


# Each subdirectory must supply rules for building sources it contributes
arduino/CDC.cpp.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/CDC.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/HID.cpp.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/HID.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/HardwareSerial.cpp.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/HardwareSerial.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/HardwareSerial0.cpp.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/HardwareSerial0.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/HardwareSerial1.cpp.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/HardwareSerial1.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/HardwareSerial2.cpp.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/HardwareSerial2.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/HardwareSerial3.cpp.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/HardwareSerial3.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/IPAddress.cpp.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/IPAddress.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/Print.cpp.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/Print.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/Stream.cpp.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/Stream.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/Tone.cpp.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/Tone.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/USBCore.cpp.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/USBCore.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/WInterrupts.c.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/WInterrupts.c
	@echo 'Building file: $<'
	@echo 'Starting C compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-gcc" -c -g -Os -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/WMath.cpp.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/WMath.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/WString.cpp.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/WString.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/hooks.c.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/hooks.c
	@echo 'Building file: $<'
	@echo 'Starting C compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-gcc" -c -g -Os -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/main.cpp.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/main.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/new.cpp.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/new.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/wiring.c.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/wiring.c
	@echo 'Building file: $<'
	@echo 'Starting C compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-gcc" -c -g -Os -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/wiring_analog.c.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/wiring_analog.c
	@echo 'Building file: $<'
	@echo 'Starting C compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-gcc" -c -g -Os -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/wiring_digital.c.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/wiring_digital.c
	@echo 'Building file: $<'
	@echo 'Starting C compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-gcc" -c -g -Os -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/wiring_pulse.c.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/wiring_pulse.c
	@echo 'Building file: $<'
	@echo 'Starting C compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-gcc" -c -g -Os -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

arduino/wiring_shift.c.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/cores/arduino/wiring_shift.c
	@echo 'Building file: $<'
	@echo 'Starting C compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-gcc" -c -g -Os -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '


