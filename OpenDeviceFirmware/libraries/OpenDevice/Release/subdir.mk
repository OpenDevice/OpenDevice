################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../.ino.cpp \
../ConnectionENC28J60.cpp \
../DeviceConnection.cpp \
../DeviceManager.cpp \
../OpenDevice.cpp 

CPP_DEPS += \
./.ino.cpp.d \
./ConnectionENC28J60.cpp.d \
./DeviceConnection.cpp.d \
./DeviceManager.cpp.d \
./OpenDevice.cpp.d 

LINK_OBJ += \
./.ino.cpp.o \
./ConnectionENC28J60.cpp.o \
./DeviceConnection.cpp.o \
./DeviceManager.cpp.o \
./OpenDevice.cpp.o 


# Each subdirectory must supply rules for building sources it contributes
.ino.cpp.o: ../.ino.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

ConnectionENC28J60.cpp.o: ../ConnectionENC28J60.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

DeviceConnection.cpp.o: ../DeviceConnection.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

DeviceManager.cpp.o: ../DeviceManager.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '

OpenDevice.cpp.o: ../OpenDevice.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '


