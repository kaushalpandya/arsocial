/**
 * 
 */
package com.xqsr.arsocial;
/*
* Copyright (c) 2010, Sebastiaan de Man
All rights reserved.
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

•	Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

•	Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer 
	in the documentation and/or other materials provided with the distribution.

•	Neither the name of the xqsr.com nor the names of its contributors may be used to endorse or promote products 
	derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN 
IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.Tweet;

import com.bmc.arsys.api.Entry;
import com.bmc.arsys.api.Timestamp;
import com.bmc.arsys.api.Value;
import com.bmc.arsys.pluginsvr.plugins.ARPluginContext;
import com.bmc.arsys.pluginsvr.plugins.ARVendorField;

/**
 * @author Sebastiaan de Man
 *
 */
public class ARTweet implements Comparable<Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 19999L;
	String id = "";
	String tweetText = "";
	String inReplyTo = "";
	String username = "";
	String sendTo = "";
	String profileImgURL = "";
	String tweetQuery = "";
	Date createdAt = Calendar.getInstance().getTime();
	int idField = 9999;
	int tweetField = 9999;
	int screenNameField = 9999;
	int profileImgField = 9999;
	int inReplyField = 9999;
	int sendToField = 9999;
	int tweetQueryField = 9999;
	int createdAtField = 9999;
	
	
	public ARTweet(List<ARVendorField> fields){
		setVendorField(fields);}
	
	public ARTweet(){}
	
	public void setTweetText(String tweetText) {
		this.tweetText = tweetText;
	}
	public String getInReplyTo() {
		return inReplyTo;
	}
	public void setInReplyTo(String inReplyTo) {
		this.inReplyTo = inReplyTo;
	}
	public String getSendTo() {
		return sendTo;
	}
	public void setSendTo(String sendTo) {
		this.sendTo = sendTo;
	}
	
	public void convertTweet(Tweet tweet){
		this.id = String.valueOf(tweet.getId());
		this.tweetText = tweet.getText();
		//this.inReplyTo = tweet.get;
		this.inReplyTo = "";
		this.profileImgURL = tweet.getProfileImageUrl();
		this.username = tweet.getFromUser();
		this.createdAt = tweet.getCreatedAt();
	}
    
	public void convertDM(DirectMessage DM){
		this.id = String.valueOf(DM.getId());
		this.tweetText = DM.getText();
		this.profileImgURL = DM.getSender().getProfileImageURL().toString();
		this.username = DM.getSenderScreenName();
		this.sendTo = DM.getRecipientScreenName();
		this.createdAt = DM.getCreatedAt();
		
			
	}
	
	public void convertEntry(Entry newEntry){
		
		this.tweetText = (String)newEntry.get(tweetField).getValue();
		Value tmpValue = newEntry.get(inReplyField);
			if (tmpValue != null){
		this.inReplyTo = (String)newEntry.get(inReplyField).getValue();}
		Value tmpSendTo = newEntry.get(sendToField);
		if (tmpSendTo != null){
		this.sendTo = (String)newEntry.get(sendToField).getValue();
		}
		//this.createdAt = (Date)newEntry.get(createdAtField).getValue();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getProfileImgURL() {
		return profileImgURL;
	}
	public void setProfileImgURL(String profileImgURL) {
		this.profileImgURL = profileImgURL;
	}
	public String getTweetText() {
		return tweetText;
	}
	public void convertStatus(Status status){
		
		this.id = String.valueOf(status.getId());
		this.tweetText = status.getText();
		//this.inReplyTo = tweet.get;
		this.inReplyTo = String.valueOf(status.getInReplyToStatusId());
		this.profileImgURL = status.getUser().getProfileImageURL().toString();
		this.username = status.getUser().getScreenName();
		this.createdAt = status.getCreatedAt();
	}
	
	public void setVendorField(List<ARVendorField> fields){
		
		// if we don't ask for the fields we return it in 9999 very quick hack...
		
		
		for(ARVendorField vendorField : fields){
			if (vendorField.getName().equals("id")){
				idField = 	vendorField.getFieldId();
			} else if (vendorField.getName().equals("tweet")){
				tweetField = 	vendorField.getFieldId();
			} else if (vendorField.getName().equals("username")){
				screenNameField = 	vendorField.getFieldId();
			} else if (vendorField.getName().equals("profilepicture")){
				profileImgField = 	vendorField.getFieldId();
			} else if (vendorField.getName().equals("inReplyTo")){
				inReplyField = 	vendorField.getFieldId();}
			else if (vendorField.getName().equals("sendTo")){
				sendToField = 	vendorField.getFieldId();}
			else if (vendorField.getName().equals("createdAt")){
				createdAtField = 	vendorField.getFieldId();}
			
			
		}
	}
public void setVendorField(List<ARVendorField> fields, ARPluginContext context){
		
		// if we don't ask for the fields we return it in 9999 very quick hack...
		
		
		for(ARVendorField vendorField : fields){
			if (vendorField.getName().equals("id")){
				idField = 	vendorField.getFieldId();
			} else if (vendorField.getName().equals("tweet")){
				tweetField = 	vendorField.getFieldId();
			} else if (vendorField.getName().equals("username")){
				screenNameField = 	vendorField.getFieldId();
			} else if (vendorField.getName().equals("profilepicture")){
				profileImgField = 	vendorField.getFieldId();
			} else if (vendorField.getName().equals("inReplyTo")){
				inReplyField = 	vendorField.getFieldId();}
			else if (vendorField.getName().equals("sendTo")){
				sendToField = 	vendorField.getFieldId();}
			else if (vendorField.getName().equals("createdAt")){
				createdAtField = 	vendorField.getFieldId();}
			context.logMessage(context.getPluginInfo(), ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "sednTo: " + sendToField);
			
		}
	}
	public int getFieldInteger(String fieldName){
		int fieldInteger = 9999;
		
		if(fieldName.equals("tweet")){
		fieldInteger = tweetField;} 
		
		else if(fieldName.equals("username")){
			fieldInteger = screenNameField;}
		
		else if(fieldName.equals("profilepicture")){
			fieldInteger = profileImgField;}
		
		else if(fieldName.equals("inReplyTo")){
			fieldInteger = inReplyField;}
		else if(fieldName.equals("id")){
			fieldInteger = idField;}
		else if(fieldName.equals("createdAt")){
			fieldInteger = createdAtField;}
		
		else if(fieldName.equals("sendTo")){
			fieldInteger = sendToField;}
		
		return fieldInteger;
	}
	
	public Value getFieldValue(String fieldName){
		Value fieldValue = null;
		
		if(fieldName.equals("tweet")){
		fieldValue = new Value(this.tweetText);} 
		
		else if(fieldName.equals("username")){
			fieldValue = new Value(this.username);}
		
		else if(fieldName.equals("tweetquery")){
			fieldValue = new Value(this.tweetQuery);}
		
		else if(fieldName.equals("profilepicture")){
			fieldValue = new Value(this.profileImgURL);}
		
		else if(fieldName.equals("inReplyTo")){
			fieldValue = new Value(this.inReplyTo);}
		else if(fieldName.equals("id")){
			fieldValue = new Value(this.id);}
		else if(fieldName.equals("createdAt")){
			fieldValue = new Value(new Timestamp(this.createdAt));}
		else if(fieldName.equals("sendTo")){
			fieldValue = new Value(this.sendTo);}
		return fieldValue;
	}

	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
}
