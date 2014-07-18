################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include subdir.mk
-include arduino/core/avr-libc/subdir.mk
-include arduino/core/subdir.mk
-include Libraries/SoftwareSerial/subdir.mk
-include objects.mk

ifneq ($(MAKECMDGOALS),clean)
ifneq ($(strip $(C++_DEPS)),)
-include $(C++_DEPS)
endif
ifneq ($(strip $(ASM_DEPS)),)
-include $(ASM_DEPS)
endif
ifneq ($(strip $(C_DEPS)),)
-include $(C_DEPS)
endif
ifneq ($(strip $(CC_DEPS)),)
-include $(CC_DEPS)
endif
ifneq ($(strip $(CPP_DEPS)),)
-include $(CPP_DEPS)
endif
ifneq ($(strip $(CXX_DEPS)),)
-include $(CXX_DEPS)
endif
ifneq ($(strip $(C_UPPER_DEPS)),)
-include $(C_UPPER_DEPS)
endif
ifneq ($(strip $(S_UPPER_DEPS)),)
-include $(S_UPPER_DEPS)
endif
endif

-include ../makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 
AR += \
/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/OpenDevice/Release/arduino.ar \

ELF += \
OpenDevice.elf \

EEP += \
OpenDevice.eep \

FLASH_IMAGE += \
OpenDevice.hex \


# All Target
all: OpenDevice

# Tool invocations
OpenDevice: $(FLASH_IMAGE)
	@echo 'Building target: $@'
	@echo 'Printing size:'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-size" -A "/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/OpenDevice/Release/OpenDevice.elf"
	@echo 'Finished building target: $@'
	@echo ' '

/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/OpenDevice/Release/arduino.ar: $(AR_OBJ)
	@echo 'Starting archiver'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-ar" rcs "/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/OpenDevice/Release/arduino.ar"   $(AR_OBJ)
	@echo 'Finished building: $@'
	@echo ' '

OpenDevice.elf: $(LINK_OBJ) $(AR)
	@echo 'Starting combiner'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-gcc" -Os -Wl,--gc-sections -mmcu=atmega328p -o "/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/OpenDevice/Release/OpenDevice.elf"   $(LINK_OBJ) $(AR)  "/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/OpenDevice/Release/arduino.ar" "-L/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/OpenDevice/Release" -lm
	@echo 'Finished building: $@'
	@echo ' '

OpenDevice.eep: $(ELF)
	@echo 'Create eeprom image'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-objcopy" -O ihex -j .eeprom --set-section-flags=.eeprom=alloc,load --no-change-warnings --change-section-lma .eeprom=0 "/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/OpenDevice/Release/OpenDevice.elf" "/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/OpenDevice/Release/OpenDevice.eep"
	@echo 'Finished building: $@'
	@echo ' '

OpenDevice.hex: $(EEP)
	@echo 'Create Flash image (ihex format)'
	"/media/Dados/Programacao/arduino-1.5.6-r2/hardware/tools/avr/bin/avr-objcopy" -O ihex -R .eeprom "/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/OpenDevice/Release/OpenDevice.elf" "/media/Dados/Codigos/Java/Projetos/OpenDevice/OpenDeviceFirmware/libraries/OpenDevice/Release/OpenDevice.hex"
	@echo 'Finished building: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(C_DEPS)$(AR)$(FLASH_IMAGE)$(EEP)$(CXX_DEPS)$(S_UPPER_DEPS)$(C++_DEPS)$(ELF)$(ASM_DEPS)$(CC_DEPS)$(AR_OBJ)$(CPP_DEPS)$(LINK_OBJ)$(C_UPPER_DEPS)$(SIZEDUMMY) OpenDevice
	-@echo ' '

.PHONY: all clean dependents
.SECONDARY:

-include ../makefile.targets
