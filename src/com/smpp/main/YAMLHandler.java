package com.smpp.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.prefs.InvalidPreferencesFormatException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class YAMLHandler {
	private static final Logger logger = LoggerFactory.getLogger(YAMLHandler.class);
	
	/**
	 * Empty constructor
	 */
	public YAMLHandler(){
		
	}
	
	/**
	 * Get configuration attributes from config.yml file
	 * 
	 * @throws InvalidPreferencesFormatException 
	 * @throws FileNotFoundException
	 */
	public static HashMap<String, Object> get() throws FileNotFoundException, InvalidPreferencesFormatException {
		// Variable to return Map with configuration
		HashMap<String, Object> returner = new HashMap<String, Object>();
		// ClassLoader instance to get absolute path
		ClassLoader classLoader = YAMLHandler.class.getClassLoader();
		// define path
		String path = classLoader.getResource("").getPath()+"/config.yml";
		logger.info("Path to config file: "+path);
		// load file to InputStream
		InputStream input = new FileInputStream(new File(path));
		//Create YAML instance to parse the file
		Yaml yaml = new Yaml();
		// Parse the file
		@SuppressWarnings("unchecked")
		HashMap<String, Object> data = (HashMap<String, Object>) yaml.load(input);
		// Validar atributos
		if(valid(data)){
			returner = data;
		}else{
			throw new InvalidPreferencesFormatException("Configuration file presents incomplete or wrong information.");
		}		
	
		return returner;
	}
	
	private static boolean valid(HashMap<String, Object> data) {
		boolean returner = true;
		
		// Validate all attributes exist
		if(data.get("smsc_host") == null || data.get("smsc_port") == null || data.get("esme_username") == null 
				|| data.get("esme_password") == null || data.get("shortcode") == null || data.get("type") == null
				|| data.get("beanstalk_host") == null || data.get("beanstalk_port") == null || data.get("beanstalk_tube") == null){
			returner = false;
			logger.debug("Value missing in configuration file");
		}
	
		
		// Check typeof "smsc_port"
		if(returner && !(data.get("smsc_port") instanceof Integer)){
			logger.debug("Typeof smsc_port not an integer");
			returner = false;			
		}

		
		// Check typeof "beanstalk_port"
		if(returner && !(data.get("beanstalk_port") instanceof Integer)){
			logger.debug("Typeof beanstalk_port not an integer");
			returner = false;			
		}

		
		// Check type (MO|MT)
		if(!data.get("type").equals("MO") && !data.get("type").equals("MT")){
			logger.debug("type does not match (MO|MT)");
			returner = false;
		}
		
		
		return returner;		
	}
	

}

