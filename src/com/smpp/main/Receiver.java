
package com.smpp.main;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.InvalidPreferencesFormatException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.pdu.EnquireLinkResp;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.google.gson.Gson;
import com.smpp.helpers.DeliveryReport;
import com.smpp.helpers.MobileOriginated;

public class Receiver {
	private static final Logger logger = LoggerFactory.getLogger(Receiver.class);
	private static HashMap<String, Object> config;
	private static RebindHandler rebindHandler;
	private static boolean keepAlive = true;

	public static void main(String[] args) {
		// Get configuration file
		logger.info("Getting configuration attributes from config.yml");
		try {
			config = YAMLHandler.get();
		} catch (FileNotFoundException e) {
			logger.debug(e.getMessage());
			return;
		} catch (InvalidPreferencesFormatException e){
			logger.debug(e.getMessage());
			return;
		}

		// Setup & Configuration required
		
		// ThreadExecutor for monitoring thread use.
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();		
        
        // Automatic expiration of requests, shared between client bootstraps 
        ScheduledThreadPoolExecutor monitorExecutor = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1, new ThreadFactory() {
            private AtomicInteger sequence = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("SmppClientSessionWindowMonitorPool-" + sequence.getAndIncrement());
                return t;
            }
        });  
        
        // Client Bootstrap with 1 expected session
        DefaultSmppClient clientBootstrap = new DefaultSmppClient(Executors.newCachedThreadPool(), 1, monitorExecutor);
        
        // Configuration for client session
        DefaultSmppSessionHandler sessionHandler = new ClientSmppSessionHandler();
        
        // Ton & Npi Configuration
        Address ton = new Address();
        ton.setNpi((byte) 0x01);
        ton.setTon((byte) 0x01);
        ton.setAddress("11*");
        
        logger.info("Connecting to "+(String) config.get("smsc_host")+":"+Integer.toString((int) config.get("smsc_port"))+" using "+(String) config.get("esme_username")+" - "+(String) config.get("esme_password"));
        logger.info("Shortcode: "+Integer.toString((int) config.get("shortcode")));
        
        SmppSessionConfiguration config0 = new SmppSessionConfiguration();
        config0.setWindowSize(1);
        config0.setName(Integer.toString((int) config.get("shortcode")));
        config0.setSystemType("");
        config0.setType(SmppBindType.RECEIVER);
        config0.setHost((String) config.get("smsc_host"));
        config0.setPort((int) config.get("smsc_port"));
        config0.setConnectTimeout(10000);
        config0.setAddressRange(ton);
        config0.setSystemId((String) config.get("esme_username"));
        config0.setPassword((String) config.get("esme_password"));
        config0.getLoggingOptions().setLogBytes(true);
        // to enable monitoring (request expiration)
        config0.setRequestExpiryTimeout(30000);
        config0.setWindowMonitorInterval(15000);
        config0.setCountersEnabled(true);      
        
        // Create session, keep-alive
        SmppSession session0 = null;
        
        // On Exit - unbind
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                rebindHandler.unbind();
            }
        });

        try {
        	// Create session and wait for response.
            session0 = clientBootstrap.bind(config0, sessionHandler); 
        } catch (Exception e) {
            logger.error("", e);
        }    
        
        rebindHandler = new RebindHandler(session0, config0, sessionHandler, clientBootstrap);
        
        while(keepAlive){
        	// Check bind status
        	while((!session0.isBound() && !session0.isBinding() && !session0.isOpen()) || session0 == null){
        		rebindHandler.rebind();
        	}      	
        	
            // "Synchronous" enquireLink call
        	try {
                EnquireLinkResp enquireLink = session0.enquireLink(new EnquireLink(), 10000);
                logger.info("enquire_link_resp: commandStatus [" + enquireLink.getCommandStatus() + "=" + enquireLink.getResultMessage() + "]");
                Thread.sleep(15000);             		
        	} catch(SmppTimeoutException e){
        		rebindHandler.rebind();
        		logger.error("", e);
        	} catch (Exception e){
        		logger.error("", e);
        	}
        }      
	}
	
	   /**
     * Could either implement SmppSessionHandler or only override select methods
     * by extending a DefaultSmppSessionHandler.
     */
    public static class ClientSmppSessionHandler extends DefaultSmppSessionHandler {

        public ClientSmppSessionHandler() {
            super(logger);
        }

        @SuppressWarnings("rawtypes")
		@Override
        public void firePduRequestExpired(PduRequest pduRequest) {
            logger.warn("PDU request expired: {}", pduRequest);
        }
        
        @Override
        public void fireChannelUnexpectedlyClosed() {
        	// On exit unbind
    		rebindHandler.rebind();
        }

        @SuppressWarnings("rawtypes")
		@Override
        public PduResponse firePduRequestReceived(PduRequest pduRequest) {   
        	// Check receiver type
        	if(config.get("type").equals("MO")){
        		//Process MO parse
                // Check type of PDU
                if (pduRequest.getCommandId() == SmppConstants.CMD_ID_DELIVER_SM) {
                	// Parse MO and build JSON payload
                    DeliverSm mo = (DeliverSm) pduRequest;
                    // Get Sender
                    Address source_address = mo.getSourceAddress();
                    // Get Recipient
                    Address dest_address = mo.getDestAddress();
                    // Get message to be parsed.
                    byte[] shortMessage = mo.getShortMessage();
                    // Bytes to String
                    String SMS = new String(shortMessage);
                    // Object containing complete MO
                    MobileOriginated mobileOriginated = new MobileOriginated(source_address.getAddress(), dest_address.getAddress(), SMS);
                    // Create Beanstalk Client
                    BeanStalkHandler beanstalkc = new BeanStalkHandler((String) config.get("beanstalk_host"), (int) config.get("beanstalk_port"), (String) config.get("beanstalk_tube"));
                    // Object to JSON
                    Gson gson = new Gson();
                	String jsonMO = gson.toJson(mobileOriginated);
                	// Push to Beanstalk
                    beanstalkc.push(jsonMO);
                }
        	}else if(config.get("type").equals("MT")){
                // Check type of PDU
                if (pduRequest.getCommandId() == SmppConstants.CMD_ID_DELIVER_SM) {
                	// Parse MO and build JSON payload
                    DeliverSm mo = (DeliverSm) pduRequest;
                    // DeliveryReport object
                    DeliveryReport dr = null;
                    // Get Recipient
                    Address source_address = mo.getSourceAddress();
                    // Get message to be parsed.
                    byte[] shortMessage = mo.getShortMessage();
                    // Bytes to String
                    String DLR = new String(shortMessage); 
                    // Split ""
                    String[]  splitter = DLR.split(" ");
                    // length should be > 5
                    if(splitter.length > 5){
                    	// hex to dec
                    	int hToD = Integer.parseInt(splitter[1], 16);
                    	int bToD = splitter[3].equals("false")?0:1;
                    	dr = new DeliveryReport(source_address.getAddress(), Integer.toString(hToD), Integer.toString(bToD), splitter[5]);	
                    }
                    
                    // Create Beanstalk Client
                    BeanStalkHandler beanstalkc = new BeanStalkHandler((String) config.get("beanstalk_host"), (int) config.get("beanstalk_port"), (String) config.get("beanstalk_tube"));
                    // Object to JSON
                    Gson gson = new Gson();
                    if(dr != null){
                    	String jsonMT = gson.toJson(dr);
                    	// Push to Beanstalk
                        beanstalkc.push(jsonMT);                    	
                    }
                }        		
        	}

  
            // PDU Acknowledgement
            PduResponse response = pduRequest.createResponse();
            return response;
        }
        
        
    }

}


