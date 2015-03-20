package com.esri.ges.monitor;

import java.io.IOException;

public class ExchangeName {
	
	public String guid;
	public String cluster;
	public String type;
	public String label;

	public ExchangeName( String fullName )
	{
		String[] parts = fullName.split("\\.");
		guid = parts[2];
		cluster = parts[1];
		type = parts[3];
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
