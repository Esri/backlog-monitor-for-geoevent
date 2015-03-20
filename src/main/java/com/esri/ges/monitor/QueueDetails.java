package com.esri.ges.monitor;

import java.io.IOException;

public class QueueDetails 
{
	public String guid;
	public String hostname;
	public String label;
	public String direction;
	public long unacknowlegedCount;

	public QueueDetails( String longName, long uack )
	{
		unacknowlegedCount = uack;
		int dotIndex = longName.lastIndexOf('.');
		guid = longName.substring(dotIndex+1);
		hostname = longName.substring(0, dotIndex);
		label = null;
		try
		{
			label = LabelLookup.findLabel(guid);
		}catch(IOException ex)
		{
			label = null;
		}
	}
	
}
