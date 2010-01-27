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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

import com.bmc.arsys.api.ARException;
import com.bmc.arsys.api.ARServerUser;
import com.bmc.arsys.api.ArithmeticOrRelationalOperand;
import com.bmc.arsys.api.AttachmentValue;
import com.bmc.arsys.api.Constants;
import com.bmc.arsys.api.Entry;
import com.bmc.arsys.api.EntryListFieldInfo;
import com.bmc.arsys.api.OutputInteger;
import com.bmc.arsys.api.QualifierInfo;
import com.bmc.arsys.api.SortInfo;
import com.bmc.arsys.api.StatisticsResultInfo;
import com.bmc.arsys.api.StatusInfo;
import com.bmc.arsys.api.Timestamp;
import com.bmc.arsys.api.VendorForm;
import com.bmc.arsys.pluginsvr.plugins.ARDBCPlugin;
import com.bmc.arsys.pluginsvr.plugins.ARPluginContext;
import com.bmc.arsys.pluginsvr.plugins.ARPluginInfo;
import com.bmc.arsys.pluginsvr.plugins.ARVendorField;



/**
 * A twitter ARDBC plugin implementation 
 *  @author Sebastiaan de Man
 */
public class ARSocialARDBC extends ARDBCPlugin {

    
    private ARPluginInfo pluginInfo = new ARPluginInfo("XQSR.ARDBC.TWITTER", this);
    
    //some funny options
    private static final String NAME = "XQSR.ARDBC.TWITTER";
    private static final String FRIENDSTIMELINE = "friendstimeline";
    private static final String MENTIONS = "mentions";
    private static final String QUERY = "query";
    private static final String DM = "directmessages";
    private static final String FOLLOWERS = "followers";
    private static final String MYTWEETS = "mytweets";
    private static String TWITTERLOGIN;
    private static String TWITTERPASS;
    private static TwitterFactory twitfac = new TwitterFactory();
    private static Twitter twitter;
    private static List<ARTweet> allTweets; 
    //private static final String DBNAME = "ARSocialARDBC";

    /**
     * Default constructor.<p>
     * Since we don't have a context here it's better not to use it.
     */
    public ARSocialARDBC() {
    	//allTweets = new ArrayList<ARTweet>(); 
    }

    /** Here we give back the 'tables' that Remedy can see.<p>
     * 
     *  They are all hardcoded, new ones can be added to the list.
     *  
     *  */
    synchronized ArrayList<VendorForm> getTables(ARPluginContext context) {
                
        ArrayList<VendorForm> list = new ArrayList<VendorForm>();
        
        context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "in getTables()");
        list.add(new VendorForm(ARSocialARDBC.NAME, ARSocialARDBC.QUERY)); //search form
        list.add(new VendorForm(ARSocialARDBC.NAME, ARSocialARDBC.FOLLOWERS)); //followers form
        list.add(new VendorForm(ARSocialARDBC.NAME, ARSocialARDBC.DM)); // direct messages
        list.add(new VendorForm(ARSocialARDBC.NAME, ARSocialARDBC.FRIENDSTIMELINE)); //friendstimeline
        list.add(new VendorForm(ARSocialARDBC.NAME, ARSocialARDBC.MENTIONS)); // mentions
  
        return list;
    }

    /** 
     * get list of fields for the form and return a list of vendor fields.<p>
     * 
     *
     * */
    synchronized ArrayList<ARVendorField> getColumns(String tableName, ARPluginContext context) throws Exception {
        
    	ArrayList<ARVendorField> list = new ArrayList<ARVendorField>();
    	context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "in getcolumns");
    	if(tableName.equals(FRIENDSTIMELINE) | tableName.equals(QUERY)| tableName.equals(MENTIONS)| tableName.equals(DM)| tableName.equals(MYTWEETS) )
    	{
    		context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "in getcolumns for friendstimeline");
            list.add(new ARVendorField("id", 0,Constants.AR_DATA_TYPE_CHAR));	//note, character here!
            list.add(new ARVendorField("tweet", 0,Constants.AR_DATA_TYPE_CHAR));
            list.add(new ARVendorField("username", 0,Constants.AR_DATA_TYPE_CHAR));
            list.add(new ARVendorField("tweetquery", 0,Constants.AR_DATA_TYPE_CHAR));
            list.add(new ARVendorField("profilepicture", 0,Constants.AR_DATA_TYPE_CHAR));
            list.add(new ARVendorField("createdAt", 0,Constants.AR_DATA_TYPE_TIME));
            if(!tableName.equals(ARSocialARDBC.DM)){
            	list.add(new ARVendorField("inReplyTo", 0,Constants.AR_DATA_TYPE_CHAR));	
            } else if(tableName.equals(ARSocialARDBC.DM)){
            	list.add(new ARVendorField("sendTo", 0,Constants.AR_DATA_TYPE_CHAR));	
            }
            	
            
            } else if(tableName.equals(ARSocialARDBC.FOLLOWERS) )
    	{
    		context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "in getcolumns for followers");
            list.add(new ARVendorField("id", 0,Constants.AR_DATA_TYPE_CHAR));	 // character!
            list.add(new ARVendorField("username", 0,Constants.AR_DATA_TYPE_CHAR));
            list.add(new ARVendorField("profilepicture", 0,Constants.AR_DATA_TYPE_CHAR));
            list.add(new ARVendorField("friendType", 0,Constants.AR_DATA_TYPE_CHAR)); 
    	}

        return list;
    }

     /* (non-Javadoc)
     * @see com.bmc.arsys.pluginsvr.plugins.ARDBCPlugin#cancel(com.bmc.arsys.pluginsvr.plugins.ARPluginContext, java.lang.Object, long)
     */
    @Override
    public void cancel(ARPluginContext context, long transid) throws ARException {
        context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "cancel()");
        super.cancel(context, transid);
    }

    /* (non-Javadoc)
     * @see com.bmc.arsys.pluginsvr.plugins.ARDBCPlugin#commit(com.bmc.arsys.pluginsvr.plugins.ARPluginContext, java.lang.Object, long)
     */
    @Override
    public void commit(ARPluginContext context, long transid) throws ARException {
        context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "commit()");
        super.commit(context, transid);
    }

    /** 
     * )
     */
    @Override
    public String createEntry(ARPluginContext context, String tableName, List<ARVendorField> fields, long transid, Entry newEntry) throws ARException {
        context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "createEntry()");
        
        String entry = null;
		try {
			
			if(tableName.equals(FRIENDSTIMELINE)){
			entry = updateTwitter(context, fields, newEntry);}
			else if (tableName.equals(DM)){
			entry = sendDM(context, fields, newEntry);	
				
			}			
		} catch (Exception e) {
			context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_ERROR, "createEntry()" + e.getMessage());
			e.printStackTrace();
		}
        //newEntry.
        
        
        return entry;

    }
synchronized String sendDM(ARPluginContext context, List<ARVendorField> fields, Entry newEntry) throws Exception{
	
	//
	ARTweet arTweet = new ARTweet(fields);
	arTweet.convertEntry(newEntry);
	String tweetText = arTweet.getTweetText();
	String sendTo = arTweet.getSendTo();
	context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "DM User to: " + sendTo + " messagetext: " + tweetText);
	DirectMessage sentDM = twitter.sendDirectMessage(sendTo, tweetText);
	
	return String.valueOf(sentDM.getId());
}
    
synchronized String updateTwitter(ARPluginContext context, List<ARVendorField> fields, Entry newEntry) throws Exception{
    	ARTweet arTweet = new ARTweet();
    	arTweet.setVendorField(fields, context) ;   	
    	arTweet.convertEntry(newEntry);
     	String tweetText = arTweet.getTweetText();

    	context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "tweetText: " + tweetText);
        Status arsocialTweet = null;
        if(arTweet.getInReplyTo().equals("")){
        arsocialTweet = twitter.updateStatus(tweetText);}
        else if(!arTweet.getInReplyTo().equals("")){
        	arsocialTweet = twitter.updateStatus(tweetText, Long.valueOf(arTweet.getInReplyTo()));
        
        }
		
		
        context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "tweetID: " + arsocialTweet.getId());
        
        //Passing back a string of the long that the twitter api gives us.
        
        return String.valueOf(arsocialTweet.getId());
    }
    /* (non-Javadoc)
     * @see com.bmc.arsys.pluginsvr.plugins.ARDBCPlugin#deleteEntry(com.bmc.arsys.pluginsvr.plugins.ARPluginContext, java.lang.Object, java.lang.String, java.util.List, long, com.bmc.arsys.api.String)
     */
    @Override
    public void deleteEntry(ARPluginContext context, String tableName, List<ARVendorField> fields, long transid, String entryId) throws ARException {
        context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "deleteEntry()");
    }

    /* (non-Javadoc)
     * @see com.bmc.arsys.pluginsvr.plugins.ARDBCPlugin#getBLOB(com.bmc.arsys.pluginsvr.plugins.ARPluginContext, java.lang.Object, java.lang.String, java.util.List, long, com.bmc.arsys.api.EntryKey, long)
     */
    @Override
    public AttachmentValue getBLOB(ARPluginContext context, String tableName, List<ARVendorField> fields, long transid, String entryId,
            int fieldId) throws ARException {
        context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "getBlob()");
        return super.getBLOB(context, tableName, fields, transid, entryId, fieldId);
    }

    /** 
     * <b>Notused, retrieving individual statuses would take too much rateLimit from the twitter API.</b>
     * @see com.bmc.arsys.pluginsvr.plugins.ARDBCPlugin#getEntry(com.bmc.arsys.pluginsvr.plugins.ARPluginContext, java.lang.Object, java.lang.String, java.util.List, long, com.bmc.arsys.api.EntryKey, int[])
     */
    @Override
    public Entry getEntry(ARPluginContext context, String tableName, List<ARVendorField> fields, long transid, String entryId,
            int[] fieldIds) throws ARException {
        context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "getEntry()");
        Entry foundEntry = new Entry();
        context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "size of alltweets:" + allTweets.size());
        for(ARTweet singleTweet: allTweets){
        	singleTweet.setVendorField(fields);
        	context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "EntryID: " + entryId + " checking for: " + singleTweet.getId());
        	if(singleTweet.getId().equals(entryId)){
        		
    	   		
        		for(ARVendorField field: fields){
        			foundEntry.put(singleTweet.getFieldInteger(field.getName()), singleTweet.getFieldValue(field.getName()));
        		
        		}	
        		break;
        	}
        	
        }
        return foundEntry;
        //return super.getEntry(context, tableName, fields, transid, entryId,fieldIds);
    }

    /* (non-Javadoc)
     * @see com.bmc.arsys.pluginsvr.plugins.ARDBCPlugin#getEntryStatistics(com.bmc.arsys.pluginsvr.plugins.ARPluginContext, java.lang.Object, java.lang.String, java.util.List, long, com.bmc.arsys.api.QualifierInfo, com.bmc.arsys.api.ArithmeticOrRelationalOperand, int, int[])
     */
    @Override
    public List<StatisticsResultInfo> getEntryStatistics(ARPluginContext context, String tableName, List<ARVendorField> fields,
            long transid, QualifierInfo qual, ArithmeticOrRelationalOperand arithOp, int statistic, int[] groupBy) throws ARException {
        context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "getEntryStatistics()");
        return super.getEntryStatistics(context, tableName, fields, transid, qual, arithOp, statistic, groupBy);
    }

    /* (non-Javadoc)
     * @see com.bmc.arsys.pluginsvr.plugins.ARDBCPlugin#getListEntryWithFields(com.bmc.arsys.pluginsvr.plugins.ARPluginContext, java.lang.Object, java.lang.String, java.util.List, long, com.bmc.arsys.api.QualifierInfo, java.util.List, java.util.List, int, int, java.lang.Integer)
     */
    
    synchronized List<Entry> getFollowers(ARPluginContext context, List<ARVendorField> fields){
    	List<Entry> entries = new ArrayList<Entry>();
    	allTweets = new ArrayList<ARTweet>();
    	
    	try {
    		 PagableResponseList<User> followersList = twitter.getFollowersStatuses();
    		 PagableResponseList<User> followingList = twitter.getFriendsStatuses();
    		 
    		 
    		 for(User follower : followersList){
    			 ARFriend arFollower = new ARFriend(fields);
    	    		arFollower.convertUser(follower);
    	    		arFollower.setFriendType("follower");
    		   		
    	    		for(User following : followingList){
    	    			ARFriend arFollowing = new ARFriend(fields);
    	    			arFollowing.convertUser(following);
    		   			if(arFollower.getUsername().equals(arFollowing.getUsername())){
    		   				followingList.remove(following);
    		   				arFollower.setFriendType("friend");
    		   				break;
    		   			}
    		   		}
    	    		
    	    		
    	    		Entry newEntry = new Entry();
    	    		for(ARVendorField field: fields){
    	    			newEntry.put(arFollower.getFieldInteger(field.getName()), arFollower.getFieldValue(field.getName()));
    	    		}
    	    		
    	    
    	    		
    				entries.add(newEntry);
    				context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "addTweet " + arFollower.getUsername());
    				
    				//allTweets.add(arTweet);
    				context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "size of alltweets after add: " + allTweets.size());
    			 
    		 }
    		 
    		 for(User following : followingList){
    			 ARFriend arFollower = new ARFriend(fields);
    			 arFollower.convertUser(following);
     	    	arFollower.setFriendType("following");
     	    	Entry newEntry = new Entry();
     	    		for(ARVendorField field: fields){
     	    			newEntry.put(arFollower.getFieldInteger(field.getName()), arFollower.getFieldValue(field.getName()));
 	    	}
     	    		entries.add(newEntry);
     	    		
     	    		}
    		 
    		 //followersList.hasNext()
    		
					
		}catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	
    	return entries;
    }
    
    synchronized List<Entry> getFriendstimeline(ARPluginContext context, List<ARVendorField> fields, String tableName){
    	List<Entry> entries = new ArrayList<Entry>();
    	
    	ResponseList<Status> statuses = null;
    	allTweets = new ArrayList<ARTweet>();
    	Paging page = new Paging();
    	page.setCount(50);
    	page.setPage(1);
    	try {
    		if(tableName.equals(MENTIONS)){
    			statuses = twitter.getMentions(page);
    		} else if(tableName.equals(FRIENDSTIMELINE)){
    			statuses = twitter.getFriendsTimeline(page);
    			
    		}
		
		
				
    	for (Status singleStatus: statuses) {
    		
    		ARTweet arTweet = new ARTweet(fields);
    		arTweet.convertStatus(singleStatus);
	   		Entry newEntry = new Entry();
    		for(ARVendorField field: fields){
    			newEntry.put(arTweet.getFieldInteger(field.getName()), arTweet.getFieldValue(field.getName()));
    		}
    		
			entries.add(newEntry);
			context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "addTweet " + arTweet.getId());
			
			allTweets.add(arTweet);
			//allTweets.c
			//Collections.sort(allTweets);
			//allTweets.
			context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "size of alltweets after add: " + allTweets.size());
     	}
    	
    	} catch (TwitterException e) {
			// Catch and return
			context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_ERROR, e.getMessage());
			e.printStackTrace();
			return null;
		}
    	return entries;
    }
    
    synchronized List<Entry> getDirectMessages(ARPluginContext context, List<ARVendorField> fields){
    	List<Entry> entries = new ArrayList<Entry>();
    	
    	allTweets = new ArrayList<ARTweet>();
    	Paging page = new Paging();
    	page.setCount(50);
    	page.setPage(1);    	
    	ResponseList<DirectMessage> statuses;
		try {
			statuses = twitter.getDirectMessages(page);
			statuses.addAll(twitter.getSentDirectMessages(page));
			
    	for (DirectMessage singleStatus: statuses) {
    		ARTweet arTweet = new ARTweet(fields);
    		arTweet.convertDM(singleStatus);
    		Entry newEntry = new Entry();
    		
    		for(ARVendorField field: fields){
    			newEntry.put(arTweet.getFieldInteger(field.getName()), arTweet.getFieldValue(field.getName()));
    		}
    		entries.add(newEntry);
    		allTweets.add(arTweet);
   		}
    	
		} catch (TwitterException e) {
			// Catch and return
			context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_ERROR, e.getMessage());
			e.printStackTrace();
			return null;
		}
    	
    return entries;	
    }
    
    synchronized List<Entry> getQueryTweets(ARPluginContext context, List<ARVendorField> fields, QualifierInfo qual){
List<Entry> entries = new ArrayList<Entry>();
    	
    	
				
		try {
			Query twitquery = new Query();
			String arServer = context.getConfigItem("server_name");
			
			
			
			
			//here we're going to the arserver to get a string for the qual.
			ARServerUser arcontext = new ARServerUser(context, "", arServer);
			String arPort = context.getConfigItem("server_port");
			
			if(arPort != null && !arPort.equals("")){
			arcontext.setPort(Integer.valueOf(arPort));
			}
			//TODO this is a very lousy way to get the query string. Will might want to find a better way...
			String queryText = arcontext.formatQualification("XQSR:ARSocial-SearchResult", qual);
			
			// using the above we will get a string representation of the query, just not nice but now we'll hack it into a query
			queryText = queryText.substring(16);
            queryText = queryText.substring(0, queryText.length()-1);
 
			context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "Querytext:" + queryText);
			twitquery.setQuery(queryText);
			twitquery.setRpp(50);
			twitquery.setPage(1);
			List<Tweet> resultTweet = twitter.search(twitquery).getTweets();

    	for (Tweet singleStatus: resultTweet) {
    		ARTweet arTweet = new ARTweet(fields);
    		arTweet.convertTweet(singleStatus);
    		Entry newEntry = new Entry();
			
    		
    		for(ARVendorField field: fields){
    			newEntry.put(arTweet.getFieldInteger(field.getName()), arTweet.getFieldValue(field.getName()));
    		}
    		allTweets.add(arTweet);
    		entries.add(newEntry);
   			
    	}
    	
		} catch (Exception e) {
			// Catch and return
			context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_ERROR, e.getMessage());
			e.printStackTrace();
			return null;
		}
	
		return entries;	
    }
    
    @Override
    public List<Entry> getListEntryWithFields(ARPluginContext context, String tableName, List<ARVendorField> fields, long transid,
            QualifierInfo qual, List<SortInfo> sortList, List<EntryListFieldInfo> fieldList, int startAt, int maxReturn, OutputInteger numMatches) throws ARException {
        context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "getListEntryWithFields()");
        List<Entry> entries = new ArrayList<Entry>();
        
       if(tableName.equals(FRIENDSTIMELINE) | tableName.equals(MENTIONS)){
    	   entries = getFriendstimeline(context, fields, tableName);
    	      	
       }
       else if(tableName.equals(DM)){
    	     	 
    	   entries = getDirectMessages(context, fields);
       }
       
       else if(tableName.equals(QUERY)){
  	
    	   entries = getQueryTweets(context, fields, qual);
       }
       
       else if(tableName.equals(FOLLOWERS)){
    	  	
    	   entries = getFollowers(context, fields);
       }
       
       
        if(entries == null || entries.size() == 0){
            new ARException(Arrays.asList(new
        			StatusInfo(Constants.AR_RETURN_ERROR, 1005, "No entries for table " + tableName, "")));
        }
        
        return entries;
    }

    /* (non-Javadoc)
     * @see com.bmc.arsys.pluginsvr.plugins.ARDBCPlugin#getListForms(com.bmc.arsys.pluginsvr.plugins.ARPluginContext, java.lang.Object)
     */
    @Override
    public List<VendorForm> getListForms(ARPluginContext context) throws ARException {
        context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "getListForms()");
        ArrayList<VendorForm> list = null;
        
        list = this.getTables(context);
        return list;
    }

    /* (non-Javadoc)
     * @see com.bmc.arsys.pluginsvr.plugins.ARDBCPlugin#getMultipleFields(com.bmc.arsys.pluginsvr.plugins.ARPluginContext, java.lang.Object, com.bmc.arsys.api.VendorForm)
     */
    @Override
    public List<ARVendorField> getMultipleFields(ARPluginContext context, VendorForm form) throws ARException {
        context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "getMultipleFields()");
        ArrayList<ARVendorField> list = null;
        try {
            list = this.getColumns(form.getTableName(), context);
        } catch (Exception e) {
            new ARException(Arrays.asList(new
        			StatusInfo(Constants.AR_RETURN_ERROR, 1003, "Error fetching Columnnames for the table " + form.getTableName(), e.getMessage())));
        }
        return list;
    }

    /* (non-Javadoc)
     * @see com.bmc.arsys.pluginsvr.plugins.ARDBCPlugin#rollback(com.bmc.arsys.pluginsvr.plugins.ARPluginContext, java.lang.Object, long)
     */
    @Override
    public void rollback(ARPluginContext context, long transid) throws ARException {
        context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "rollBack()");
    }

    /* (non-Javadoc)
     * @see com.bmc.arsys.pluginsvr.plugins.ARDBCPlugin#setBLOB(com.bmc.arsys.pluginsvr.plugins.ARPluginContext, java.lang.Object, java.lang.String, java.util.List, long, com.bmc.arsys.api.EntryKey, long, com.bmc.arsys.api.AttachmentInfo)
     */
    @Override
    public void setBLOB(ARPluginContext context, String tableName, List<ARVendorField> fields, long transid, String entryId,
            int fieldId, AttachmentValue blob) throws ARException {
        context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "setBlob()");
    }

    /* (non-Javadoc)
     * @see com.bmc.arsys.pluginsvr.plugins.ARDBCPlugin#setEntry(com.bmc.arsys.pluginsvr.plugins.ARPluginContext, java.lang.Object, java.lang.String, java.util.List, long, com.bmc.arsys.api.Entry, com.bmc.arsys.api.Timestamp)
     */
    @Override
    public void setEntry(ARPluginContext context, String tableName, List<ARVendorField> fields, long transid, String entryId,
            Entry entry, Timestamp lastModified) throws ARException {
        context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "setEntry()");
    }

    /* (non-Javadoc)
     * @see com.bmc.arsys.pluginsvr.plugins.ARPlugin#initialize(com.bmc.arsys.pluginsvr.plugins.ARPluginContext, java.lang.String[])
     */
    @Override
    public void initialize(ARPluginContext context) throws ARException {
        
    	context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "initialize()");
        //default connection information
           
        try {
            //now fetch the connection information provided in the configuration
            String item = context.getConfigItem(pluginInfo, "proxyserver");
           
			if(item != null && !item.equals(""))
				System.setProperty("http.proxyHost", item);
            
			item = context.getConfigItem(pluginInfo, "proxyport");
            if(item != null && !item.equals(""))
            	System.setProperty("http.proxyPort", item);
            
            item = context.getConfigItem(pluginInfo, "twitterlogin");
            if(item != null && !item.equals(""))
               	ARSocialARDBC.TWITTERLOGIN = item;
            
            	item = context.getConfigItem(pluginInfo, "twitterpass");
            if(item != null)
            	ARSocialARDBC.TWITTERPASS = item;
            
            
        } catch (Exception e) {
            new ARException(Arrays.asList(new
        			StatusInfo(Constants.AR_RETURN_ERROR, 1000, "There's a problem with getting the configuration ", e.getMessage())));
        }
        
        ARSocialARDBC.twitter = twitfac.getInstance(ARSocialARDBC.TWITTERLOGIN, ARSocialARDBC.TWITTERPASS);
    
    }

    /* (non-Javadoc)
     * @see com.bmc.arsys.pluginsvr.plugins.ARPlugin#onEvent(com.bmc.arsys.pluginsvr.plugins.ARPluginContext, int)
     */
    @Override
    public void onEvent(ARPluginContext contexxt, int eventId) throws ARException {
        contexxt.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "onEvent()");
        super.onEvent(contexxt, eventId);
    }

    public static void init(ARPluginContext context){
    	
    	context.logMessage(ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "in init phase");
    	allTweets = new ArrayList<ARTweet>(); 
	
	
	}
    /* (non-Javadoc)
     * @see com.bmc.arsys.pluginsvr.plugins.ARPlugin#terminate(com.bmc.arsys.pluginsvr.plugins.ARPluginContext)
     */
    @Override
    public void terminate(ARPluginContext context) throws ARException {
        try {
            context.logMessage(pluginInfo, ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "terminate()");
            /*
             * Close the connection
             */
            //con.close();
        } catch (Exception e) {
            new ARException(Arrays.asList(new
        			StatusInfo(Constants.AR_RETURN_ERROR, 1001, "There was a problem")));
        }
    }

}

