package com.smpp.helpers;

public class MobileOriginated {
	// Class variables
	private String sender;
	private String recipient;
	private String content;
	
	/**
	 * Empty constructor
	 */
	public MobileOriginated(){
		
	}
	
	/**
	 * All parameter constructor
	 * 
	 */
	public MobileOriginated(String sender, String recipient, String content){
		this.sender = sender;
		this.recipient = recipient;
		this.content = content;
	}

	// Getter & Setter Methods
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getRecipient() {
		return recipient;
	}
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
