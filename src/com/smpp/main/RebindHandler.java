package com.smpp.main;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;

public class RebindHandler {
	private static final Logger logger = LoggerFactory.getLogger(RebindHandler.class);
	private SmppSession session0;
	private SmppSessionConfiguration config0;
	private DefaultSmppSessionHandler sessionHandler;
	private DefaultSmppClient clientBootstrap;
	private ThreadPoolExecutor executor;
	private ScheduledThreadPoolExecutor monitorExecutor;
	
	/**
	 * Constructor with parameters
	 */
	public RebindHandler(SmppSession session0, SmppSessionConfiguration config0, DefaultSmppSessionHandler sessionHandler, DefaultSmppClient clientBootstrap){
		this.session0 = session0;
		this.config0 = config0;
		this.sessionHandler = sessionHandler;
		this.clientBootstrap = clientBootstrap;
	}
	
	public void rebind(){
    	try{
    		//Check if session0 is live, else reconnect
        	if(session0 == null){
        		session0 = clientBootstrap.bind(config0, sessionHandler);
        	}else if(!session0.isBound() && !session0.isBinding() && !session0.isOpen()){
        		session0 = clientBootstrap.bind(config0, sessionHandler);	
        	}    		
    	}catch(Exception e){
    		logger.debug("{}", e);
    	}
	}
	
	public void unbind(){
    	// On exit unbind
    	session0.unbind(5000);       
           
        // Close session if still alive
        if (session0 != null) {
            logger.info("Cleaning up session... (final counters)");
            if (session0.hasCounters()) {
                logger.info("tx-enquireLink: {}", session0.getCounters().getTxEnquireLink());
                logger.info("tx-submitSM: {}", session0.getCounters().getTxSubmitSM());
                logger.info("tx-deliverSM: {}", session0.getCounters().getTxDeliverSM());
                logger.info("tx-dataSM: {}", session0.getCounters().getTxDataSM());
                logger.info("rx-enquireLink: {}", session0.getCounters().getRxEnquireLink());
                logger.info("rx-submitSM: {}", session0.getCounters().getRxSubmitSM());
                logger.info("rx-deliverSM: {}", session0.getCounters().getRxDeliverSM());
                logger.info("rx-dataSM: {}", session0.getCounters().getRxDataSM());
            }
            session0.destroy();
        }
        
        // Close all channels and threads
        logger.info("Shutting down client bootstrap and executors...");
        clientBootstrap.destroy();
        executor.shutdownNow();
        monitorExecutor.shutdownNow();    
        logger.info("Done. Exiting");   
	}
	
	

}
