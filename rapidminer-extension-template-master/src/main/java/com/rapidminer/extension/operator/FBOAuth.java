package com.rapidminer.extension.operator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;

import org.json.JSONObject;

import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.parameter.OAuthMechanism;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.LogService;

public class FBOAuth implements OAuthMechanism {

	private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36";
	private String token = "";

	@Override
	public String startOAuth() {
		// TODO Auto-generated method stub

		String url = "http://www.facebook.com/dialog/oauth?client_id=485528671808097&redirect_uri=https://tpconnect.yellowfin.bi/getToken.jsp&response_type=token&scope=email,user_friends";
		return url;
	}

	@Override
	public String endOAuth(String code) {
		// TODO Auto-generated method stub
		if (code != null) {
			String url = "https://graph.facebook.com/v2.10/oauth/access_token?grant_type=fb_exchange_token&client_id=485528671808097&client_secret=2fc69dddcce9ace0c67f20cf8bd0101f&fb_exchange_token=";
			try {
				URL obj = new URL(url + code);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("GET");
				con.setRequestProperty("User-Agent", USER_AGENT);
				int responseCode = con.getResponseCode();

				String inputLine;
				StringBuffer builder = new StringBuffer();
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				while ((inputLine = in.readLine()) != null){
					builder.append(inputLine);
				}
				in.close();
				String response = builder.toString();
				JSONObject jsonObj = new JSONObject(response);
				token = jsonObj.getString("access_token");
				
				if (responseCode != 200)
					return responseCode + "";

			} catch (Exception e) {
				LogService.getRoot().log(Level.INFO, e.getMessage());
				return e.toString();
			}
			url = "https://graph.facebook.com/v2.10/me?fields=posts&access_token=";
			try {
				URL obj = new URL(url + token);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("GET");
				con.setRequestProperty("User-Agent", USER_AGENT);
				int responseCode = con.getResponseCode();
				//LogService.getRoot().log(Level.INFO, responseCode + " Authenticated");
				if (responseCode == 200) {
					return null;
				} else
					return responseCode + "";

			} catch (Exception e) {
				e.printStackTrace();
				return e.toString();
			}
		}
		return I18N.getMessage(I18N.getGUIBundle(), "empty code", new Object[0]);
	}

	@Override
	public String getToken() {
		// TODO Auto-generated method stub
		String url = "https://graph.facebook.com/v2.10/me?fields=posts&access_token=";
		
		try {
			URL obj = new URL(url + token);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", USER_AGENT);
			int responseCode = con.getResponseCode();
			//LogService.getRoot().log(Level.INFO, responseCode + " Authenticated");
			if (responseCode == 200) {
				return token;
			} else
				return null;

		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		}
	}

	@Override
	public boolean isOAuth2() {
		// TODO Auto-generated method stub
		return true;
	}

}
