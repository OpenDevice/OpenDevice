################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
/media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/libraries/SoftwareSerial/SoftwareSerial.cpp 

CPP_DEPS += \
./Libraries/SoftwareSerial/SoftwareSerial.cpp.d 

LINK_OBJ += \
./Libraries/SoftwareSerial/SoftwareSerial.cpp.o 


# Each subdirectory must supply rules for building sources it contributes
Libraries/SoftwareSerial/SoftwareSerial.cpp.o: /media/Dados/Programacao/arduino-1.5.6-r2/hardware/arduino/avr/libraries/SoftwareSerial/SoftwareSerial.cpp
	@echo 'Building file: $<'
	@echo 'Starting C++ compile'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-g++" -c -g -Os -fno-exceptions -ffunction-sections -fdata-sections -MMD -mmcu=atmega328p -DF_CPU=16000000L -DARDUINO=156-r2 -DARDUINO_AVR_UNO -DARDUINO_ARCH_AVR    -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/IRremote -I/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/UIPEthernet -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -D__IN_ECLIPSE__=1 -x c++ "$<"  -o  "$@"   -Wall
	@echo 'Finished building: $<'
	@echo ' '


