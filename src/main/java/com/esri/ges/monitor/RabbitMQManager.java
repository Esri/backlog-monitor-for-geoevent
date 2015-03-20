package com.esri.ges.monitor;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class RabbitMQManager 
{
	private String server;
	private int port;
	private String password;
	private String username;
	private ObjectMapper mapper = new ObjectMapper();
	private String rabbitMQPassword;
	private String rabbitMQUser;
	private HashMap<String,String> labels = new HashMap<>();

	public RabbitMQManager( String server, int port, String username, String password ) throws IOException
	{
		this.server = server;
		this.port = port;
		this.username = username;
		this.password = password;
		fetchRabbitMQCredentials();
	}
	
	public void fetchRabbitMQCredentials() throws ClientProtocolException, IOException
	{
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String url = "http://localhost:6080/arcgis/admin/generateToken";
		String referer = "http://localhost:6080/arcgis";
		HttpUriRequest loginRequest = RequestBuilder.post(url)
				.addParameter("client", "referer")
				.addParameter("referer", referer)
				.addParameter("username", username)
				.addParameter("password", password)
				.addParameter("f", "json")
				.build();
		CloseableHttpResponse response = httpclient.execute(loginRequest);
        String responseString = EntityUtils.toString(response.getEntity());
        response.close();
        
        JsonNode tree = mapper.readTree(responseString);
        if( tree.has("status") && tree.get("status").asText().equals("error") )
        {
        	String message = "Unknown Error communicating with "+url;
        	if( tree.has("messages") )
        		message = tree.get("messages").get(0).asText();
        	throw new IOException(message);
        }
        String token = tree.get("token").asText();
        
        String platformServiceUrl = "http://localhost:6080/arcgis/admin/system/platformServices";
		HttpUriRequest serviceDetailsRequest = RequestBuilder.post(platformServiceUrl)
				.addHeader("referer", referer)
				.addParameter("token", token )
				.addParameter("f", "json")
				.build();
		response = httpclient.execute(serviceDetailsRequest);
        responseString = EntityUtils.toString(response.getEntity());
        response.close();
		
        tree = mapper.readTree(responseString);
        if( tree.has("platformservices") )
        {
        	for( JsonNode service : tree.get("platformservices") )
        	{
        		if( service.get("type").asText().equals("MESSAGE_BUS") && service.get("provider").asText().equals("RabbitMQ") )
        		{
        			JsonNode info = service.get("info");
           			rabbitMQPassword = info.get("password").asText();
           			rabbitMQUser = info.get("user").asText();
        		}
        	}
        }
	}
	
	private List<ExchangeName> getExchangeNames() throws ClientProtocolException, IOException
	{
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope("localhost", 27274),
                new UsernamePasswordCredentials( rabbitMQUser, rabbitMQPassword ) );
        CloseableHttpClient httpclient = HttpClients.custom()
        		.setSSLSocketFactory(getSSLSocketFactory())
                .setDefaultCredentialsProvider(credsProvider)
                .build();

		String url = "https://localhost:27274/api/exchanges";
		HttpUriRequest overviewRequest = RequestBuilder.get(url).build();
		CloseableHttpResponse response = httpclient.execute(overviewRequest);
        String responseString = EntityUtils.toString(response.getEntity());
        response.close();
        
        ArrayList<ExchangeName> results = new ArrayList<>();
        JsonNode exchanges = mapper.readTree(responseString);
        for( JsonNode exchange : exchanges )
        {
        	String name = exchange.get("name").asText();
        	if( looksLikeGeoEventExchange(name) )
        		results.add( new ExchangeName( name ) );
        }
        return results;
	}
	
	private boolean looksLikeGeoEventExchange( String name )
	{
		String[] parts = name.split("\\.");
		if( parts.length < 3 || (! parts[0].equals("geoevent")) )
			return false;
		String guid = parts[parts.length-2];
		if( guid.length() == 36 )
			return true;
		return false;
	}
	
	private List<QueueDetails> getQueueDetails() throws ClientProtocolException, IOException
	{
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope("localhost", 27274),
                new UsernamePasswordCredentials( rabbitMQUser, rabbitMQPassword ) );
        CloseableHttpClient httpclient = HttpClients.custom()
        		.setSSLSocketFactory(getSSLSocketFactory())
                .setDefaultCredentialsProvider(credsProvider)
                .build();

		String url = "https://localhost:27274/api/queues";
		HttpUriRequest overviewRequest = RequestBuilder.get(url).build();
		CloseableHttpResponse response = httpclient.execute(overviewRequest);
        String responseString = EntityUtils.toString(response.getEntity());
        response.close();
        
        ArrayList<QueueDetails> results = new ArrayList<>();
        JsonNode queues = mapper.readTree(responseString);
        for( JsonNode queue : queues )
        {
        	String name = queue.get("name").asText();
        	if( looksLikeGeoEventQueue(name) )
        		results.add(new QueueDetails(name, queue.get("messages_unacknowledged").asLong()));
        }
        return results;
	}
	
	private boolean looksLikeGeoEventQueue( String name )
	{
		int dotIndex = name.lastIndexOf('.');
		if( dotIndex == -1 )
			return false;
		String guid = name.substring(dotIndex+1);
		if( guid.length() == 36 )
			return true;
		return false;
	}
	
	private SSLConnectionSocketFactory getSSLSocketFactory()
	{
		KeyStore trustStore;
		try
		{
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init((KeyStore) null);
			X509TrustManager x509TrustManager = null;
			for (TrustManager trustManager : trustManagerFactory.getTrustManagers())
			{
				if (trustManager instanceof X509TrustManager)
				{
					x509TrustManager = (X509TrustManager) trustManager;
					break;
				}
			}

			TrustStrategy recklessStrategy = new TrustStrategy(){
					public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException{
						return true;
					}
				};

			SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
			sslContextBuilder.loadTrustMaterial(trustStore, recklessStrategy);
			SSLContext sslContext = sslContextBuilder.build();
			HostnameVerifier recklessVerifier = new HostnameVerifier(){
					public boolean verify(String hostname, SSLSession session){
						return true;
					}				
				};
			SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, recklessVerifier);
			return sslSocketFactory;
		}
		catch (GeneralSecurityException | IOException e)
		{
			System.err.println("SSL Error : " + e.getMessage());
		}
		return null;
	}
	
	public Map<String,Long> getCounts() throws IOException
	{
		HashMap<String,Long> results = new HashMap<>();
		List<QueueDetails> queues = getQueueDetails();
		for( QueueDetails queue : queues )
			results.put(queue.label, queue.unacknowlegedCount );
		return results;
	}

	public static void main(String[] args)
	{
		try {
			RabbitMQManager mgr = new RabbitMQManager("localhost", 6080, "admin","admin");
			Map<String,Long> counts = mgr.getCounts();
			for( String key : counts.keySet() )
				System.out.println(key+" has "+counts.get(key)+" messages in the backlog.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
