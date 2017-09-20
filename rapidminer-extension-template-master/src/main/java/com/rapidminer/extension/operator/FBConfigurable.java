package com.rapidminer.extension.operator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

import org.json.JSONObject;

import com.rapidminer.tools.LogService;
import com.rapidminer.tools.config.AbstractConfigurable;

public class FBConfigurable extends AbstractConfigurable {

	@Override
	public String getTypeId() {
		// TODO Auto-generated method stub
		return "FB";
	}

	public String connect(String token, String pageId) {
		
		String url = "https://graph.facebook.com/v2.10/" + pageId + "?fields=posts&access_token=";
		URLConnection conn = null;
		String inputLine = "";
		StringBuffer builder = new StringBuffer();
		int ctr=0;
		try {
			conn = new URL(url + token).openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((inputLine = in.readLine()) != null){
				//LogService.getRoot().log(Level.INFO, ctr++ +inputLine);
				builder.append(inputLine);
			}
			in.close();
		} catch (Exception e) {
			LogService.getRoot().log(Level.INFO, "Exception in Configurable "+e.toString());
			return "error";
		}
		return builder.toString();		
	}
}
