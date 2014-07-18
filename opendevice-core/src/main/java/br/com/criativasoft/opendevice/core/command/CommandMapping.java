/*
 * ******************************************************************************
 *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.core.command;

import java.util.HashSet;
import java.util.Set;

import br.com.criativasoft.opendevice.core.model.Device;

public class CommandMapping {
	
	private long id;
	
	private String key;
	
	private Set<CommandMappingStore> mappigns = new HashSet<CommandMappingStore>();

	/**
	 * Define uma chave para indentificar esse mapeamento a nível de aplicação.
	 * @param key
	 */
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Set<CommandMappingStore> getMappigns() {
		return mappigns;
	}

	public void setMappigns(Set<CommandMappingStore> mappigns) {
		this.mappigns = mappigns;
	}
	
	public void add(Command command, Device device){
		this.add(new CommandMappingStore(command, device));
	}
	
	public void add(CommandMappingStore mappingStore){
		getMappigns().add(mappingStore);
	}
	
	public boolean isEmpty(){
		return this.getMappigns().isEmpty();
	}
	
	/**
	 * Check if has command where data( {@link Command#getData()} ) are equals.
	 * @param command
	 * @see #findCommand(Command)
	 */
	public boolean hasCommand(Command command){
		Device findDevice = findDevice(command);
		return findDevice != null;
	}
	
	/**
	 * Find first Device in mappin.<br/>
	 * Where Command data( {@link Command#getData()} ) are equals.
	 * @param command
	 */
	public Device findDevice(Command command){
		CommandMappingStore mapping = findMapping(command);
		return (mapping != null ? mapping.getDevice() : null);
	}
	
	/**
	 * Find first mappin where Command data( {@link Command#getData()} ) are equals.
	 * @param command
	 */
	public CommandMappingStore findMapping(Command command){
		
		for (CommandMappingStore mappingStore : mappigns) {
			
			Command currentCommand = mappingStore.getCommand();
			
			// TODO: Atualmente está funcionando apenas para o device command
			if(currentCommand != null && currentCommand instanceof DeviceCommand && command instanceof DeviceCommand){
				
				DeviceCommand deviceCommand = (DeviceCommand) currentCommand;
				DeviceCommand currentDeviceCommand = (DeviceCommand) command;
			
				if(currentDeviceCommand.getValue() == deviceCommand.getValue()){
					return mappingStore;
				}
			}

			
		}
		
		return null;
	}
	

}
