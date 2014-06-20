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
	private MBeanServerConnection mbsc;
	private JMXConnector jmxc;
	String username = "arcgis";
	String password = "manager";

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
		Map<String, String[]> env = new HashMap<String, String[]>();
		String[] credentials = new String[] { username, password };
		env.put(JMXConnector.CREDENTIALS, credentials);

		JMXServiceURL url;
		try
		{
			url = new JMXServiceURL("service:jmx:rmi://0.0.0.0:44445/jndi/rmi://0.0.0.0:1100/karaf-root");
		} catch (MalformedURLException e)
		{
			throw new IOException(e.getMessage());
		}
		jmxc = JMXConnectorFactory.connect(url, env);
		mbsc = jmxc.getMBeanServerConnection();
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
		HashMap<String,Long> results = new HashMap<>();

		try
		{
			if( mbsc == null )
				connect();

			// Collect input Queues
			ObjectName queueNamePrefix = new ObjectName("org.apache.activemq:BrokerName=localhost,Type=Queue,*");
			Set<ObjectName> intputNames = mbsc.queryNames(queueNamePrefix, null);
			for( ObjectName inputBeanName : intputNames )
			{
				String inputName = (String) mbsc.getAttribute(inputBeanName, "Name");
				if( inputName.equals("incidents"))
					continue;
				long inflightCount = (Long) mbsc.getAttribute(inputBeanName, "InFlightCount");
				results.put("Input:"+inputName, inflightCount);
			}

			// Collect output Topics
			ObjectName topicNamePrefix = new ObjectName("org.apache.activemq:BrokerName=localhost,Type=Topic,*");
			Set<ObjectName> outputNames = mbsc.queryNames(topicNamePrefix, null);
			for( ObjectName outputBeanName : outputNames )
			{
				String outputName = (String) mbsc.getAttribute(outputBeanName, "Name");
				if( outputName.startsWith("ActiveMQ.Advisory"))
					continue;
				long inflightCount = (Long) mbsc.getAttribute(outputBeanName, "InFlightCount");
				results.put("Output:"+outputName, inflightCount);
			}
		}catch(IOException ex)
		{
			mbsc = null;
			throw ex;
		}

		return results;
	}

}