package com.esri.ges.monitor;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class LabelLookup 
{
	private static ObjectMapper mapper = new ObjectMapper();
	
	public static String findLabel( String guid ) throws IOException
	{
		String result = findLabel( "input", guid );
		if( result == null )
			result = findLabel( "output", guid );
		return result;
	}

	private static String findLabel(String path, String guid) throws IOException 
	{
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String inputsUrl = "http://localhost:6180/geoevent/rest/"+path+"/"+guid+"/.json";
		HttpUriRequest inputsRequest = RequestBuilder.get(inputsUrl).build();
		CloseableHttpResponse response = httpclient.execute(inputsRequest);
		int statusCode = response.getStatusLine().getStatusCode();
        if( statusCode == 200 )
        {
        	String responseString = EntityUtils.toString(response.getEntity());
        	JsonNode component = mapper.readTree(responseString);
            response.close();
        	return component.get("label").asText() + " ("+path+")";
        }
        response.close();
        return null;
	}

}
