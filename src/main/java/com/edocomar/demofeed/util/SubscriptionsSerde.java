package com.edocomar.demofeed.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class SubscriptionsSerde {

	public void write(ConcurrentMap<String, Set<String>>subs, OutputStream os) throws Exception {
		JSONArray jarr = new JSONArray();
		for(Map.Entry<String, Set<String>> entry : subs.entrySet()) {
			JSONObject jo = new JSONObject();
			jo.put("user", entry.getKey());
			jo.put("feeds", entry.getValue());
			jarr.put(jo);
		}
		os.write(jarr.toString().getBytes(Charset.defaultCharset()));
	}

	public void readInto(ConcurrentMap<String, Set<String>>subs, InputStream is) throws Exception {
		
		JSONArray jarr = new JSONArray(IOUtils.toString(is, Charset.defaultCharset())); 
		
		for (int i=0; i<jarr.length(); i++) {
			JSONObject jo = (JSONObject) jarr.get(i);
			String user = (String) jo.get("user");
			// TODO encapsulate the ConcurrentHashMap 
			subs.put(user, ConcurrentHashMap.newKeySet());
			JSONArray userFeeds = (JSONArray) jo.get("feeds");
			for (int j=0; j<userFeeds.length(); j++) {
				subs.get(user).add((String) userFeeds.get(j));
			}
		}

	}
}
