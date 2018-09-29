package com.muksihs.farhorizons.steemapi;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bittrade.libs.steemj.base.models.AccountName;

public class SteemRcApi {
	private static ObjectMapper json;
	private static ObjectMapper json() {
		if (json==null) {
			json = new ObjectMapper();
			json.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			json.setDateFormat(dateFormat);
		}
		return json;
	}
	public static RcAccounts getRc(AccountName account) throws JsonParseException, JsonMappingException, IOException {
		JsonRpcCallMethod<FindRcAccounts> cmd = new JsonRpcCallMethod<>();
		FindRcAccounts e = new FindRcAccounts();
		e.accountNameList.getAccounts().add(account.getName());
		cmd.setParams(e);
		String jsonResponse = doPost("https://api.steemit.com/", cmd);
		return json().readValue(jsonResponse, RcValuesResponse.class).getResult();
	}
	
	private static String doPost(String urlStr, Object postData) throws IOException {
		String dataJSON = json().writeValueAsString(postData);
	    URL url = new URL(urlStr);
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setConnectTimeout(5000);
	    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	    conn.setDoOutput(true);
	    conn.setDoInput(true);
	    conn.setRequestMethod("POST");

	    OutputStream os = conn.getOutputStream();
	    os.write(dataJSON.getBytes("UTF-8"));
	    os.close();

	    try (InputStream input = new BufferedInputStream(conn.getInputStream())) {
	    	return IOUtils.toString(input, StandardCharsets.UTF_8);
	    } finally {
	    	conn.disconnect();
	    }
	}
}
