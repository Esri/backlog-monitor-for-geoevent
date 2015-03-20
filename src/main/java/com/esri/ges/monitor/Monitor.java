package com.esri.ges.monitor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class Monitor
{
	private JMXConnector jmxc;
	String username = "admin";
	String password = "admin";
	private RabbitMQManager rabbitMQManager;

	public Monitor(String[] args ) throws Exception
	{

		if( args.length > 0 )
		{
			if( args.length > 1 )
			{
				username = args[0];
				password = args[1];
			}
			else
			{
				password = args[0];
			}
		}
		connect();
	}

	private void connect() throws IOException
	{
		rabbitMQManager = new RabbitMQManager( "localhost", 6080, username, password );
	}

	public void close() throws IOException
	{
		jmxc.close();
	}

	public static void main(String[] args) throws Exception
	{
		Monitor monitor = new Monitor(args);
		while(true)
		{
			Map<String,Long> counts = monitor.getCounts();
			for( String outputName : counts.keySet() )
			{
				System.out.println( outputName + ":" + counts.get(outputName) );
			}
			try{Thread.sleep(1000);}catch(InterruptedException ex){monitor.close();}
		}
	}

	public Map<String,Long> getCounts() throws Exception
	{
		Map<String,Long> results = rabbitMQManager.getCounts();
		return results;
	}

}