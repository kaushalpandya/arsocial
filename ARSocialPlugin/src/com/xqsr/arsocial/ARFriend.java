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

import java.util.List;

import twitter4j.User;

import com.bmc.arsys.api.Entry;
import com.bmc.arsys.api.Value;
import com.bmc.arsys.pluginsvr.plugins.ARVendorField;

/**
 * @author Sebastiaan de Man
 *
 */
public class ARFriend implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1888888L;
	String id = "";
	String username = "";
	String profileImgURL = "";
	String tweetQuery = "";
	String friendType = "";
	public String getFriendType() {
		return friendType;
	}

	public void setFriendType(String friendType) {
		this.friendType = friendType;
	}

	int idField = 9999;
	int usernameField = 9999;
	int profileImgField = 9999;
	int tweetQueryField = 9999;
	int friendTypeField = 9999;
	
	
	public int getFriendTypeField() {
		return friendTypeField;
	}

	public void setFriendTypeField(int friendTypeField) {
		this.friendTypeField = friendTypeField;
	}

	public ARFriend(List<ARVendorField> fields){
		setVendorField(fields);}
	
	public ARFriend(){}
	
	
	public void convertUser(User user){
		this.id = String.valueOf(user.getId());
		this.profileImgURL = user.getProfileImageURL().toString();
		this.username = user.getScreenName();
		
	}
    
	
	
	public void convertEntry(Entry newEntry){
		
		this.username = (String)newEntry.get(usernameField).getValue();
		this.profileImgURL = (String)newEntry.get(profileImgField).getValue();
		this.friendType = (String)newEntry.get(friendTypeField).getValue();
		
		
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
	
	public void setVendorField(List<ARVendorField> fields){
		
		// if we don't ask for the fields we return it in 9999 very quick hack...
		
		
		for(ARVendorField vendorField : fields){
			if (vendorField.getName().equals("id")){
				idField = 	vendorField.getFieldId();
			}  else if (vendorField.getName().equals("username")){
				usernameField = 	vendorField.getFieldId();
			} else if (vendorField.getName().equals("profilepicture")){
				profileImgField = 	vendorField.getFieldId();
			} else if (vendorField.getName().equals("friendType")){
				friendTypeField = 	vendorField.getFieldId();}
			
			
		}
	}

	public int getFieldInteger(String fieldName){
		int fieldInteger = 9999;
		
		
		
		if(fieldName.equals("username")){
			fieldInteger = usernameField;}
		
		else if(fieldName.equals("profilepicture")){
			fieldInteger = profileImgField;}
		
		else if(fieldName.equals("friendType")){
			fieldInteger = friendTypeField;}
		else if(fieldName.equals("id")){
			fieldInteger = idField;}
		return fieldInteger;
	}
	
	public Value getFieldValue(String fieldName){
		Value fieldValue = null;
		
		
	 if(fieldName.equals("username")){
			fieldValue = new Value(this.username);}
		
		else if(fieldName.equals("tweetquery")){
			fieldValue = new Value(this.tweetQuery);}
		
		else if(fieldName.equals("profilepicture")){
			fieldValue = new Value(this.profileImgURL);}
		
		
		else if(fieldName.equals("id")){
			fieldValue = new Value(this.id);}
		else if(fieldName.equals("friendType")){
			fieldValue = new Value(this.friendType);}
		
		return fieldValue;
	}
	
	
	
}
