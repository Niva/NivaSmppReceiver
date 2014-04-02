package com.smpp.helpers;

public class DeliveryReport {
	// Class variable
	private String recipient;
	private String id;
	private String status;
	private String dlr;
	
	/**
	 * Empty constructor
	 */
	public DeliveryReport (){
		
	}
	
	/**
	 * Full argument constructor
	 */
	public DeliveryReport (String recipient, String id, String status, String dlr){
		this.recipient = recipient;
		this.id = id;
		this.status = status;
		this.dlr = dlr;
	}

	//Getter & Setter Methods
	public String getRecipient() {
		return recipient;
	}
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDlr() {
		return dlr;
	}
	public void setDlr(String dlr) {
		this.dlr = dlr;
	}
	
	
	

}
