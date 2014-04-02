package com.smpp.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.surftools.BeanstalkClient.Client;
import com.surftools.BeanstalkClientImpl.ClientImpl;

public class BeanStalkHandler {
	private static final Logger logger = LoggerFactory.getLogger(BeanStalkHandler.class);
	// Class variables
	private String host;
	private int port;
	private String tube;
	
	
	/**
	 * Empty constructor
	 */
	public BeanStalkHandler() {
		
	}
	
	/**
	 * Constructor
	 * @param host BeanStalk server URL
	 * @param port BeanStalk server port
	 * @param tube BeanStalk server tube
	 */
	public BeanStalkHandler(String host, int port, String tube){
		this.host = host;
		this.port = port;
		this.tube = tube;
	}
	
	/**
	 * Push a JSON message to BeanStalk
	 */
	public boolean push(String json){
		// Response variable
		boolean returner = false;
		
		// Create BeanstalkClient instance
		logger.info("BeanStalk Client trying to connect to "+this.host+":"+this.port);
		Client client = new ClientImpl(this.host, this.port, false);
	
		// assign tube to push job
		client.useTube(this.tube);
		
		// put to BeanStalk tube
		// arguments as follows:
		// long priority, int delaySeconds, int timeToRun, byte[] data
		long job = client.put(10, 0, 120, json.getBytes());
		
		// job id > 0 then job was created
		if(job > 0){
			returner = true;
		}
		
		return returner;
	}
	

}



