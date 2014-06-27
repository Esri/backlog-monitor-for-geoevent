package com.esri.ges.monitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class ChartDisplay extends Application {

	private Monitor monitor;
	private long startTime = System.currentTimeMillis();
	private HashMap<String, XYChart.Series> seriesSet = new HashMap<>();
	private LineChart<Number,Number> lineChart;
	private int maxHistory = 60 * 60;
	private NumberAxis xAxis;
	private NumberAxis yAxis;

	private Thread updater = new Thread(){
		@Override
		public void run()
		{
			while(true)
			{
				Platform.runLater( new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							updateDisplay();
						}catch( Exception ex )
						{
							//System.err.println("Stopping graph updates because there was a problem updating the display ("+ex.getLocalizedMessage()+")");
						}
					}
				}
						);

				try
				{

					Thread.sleep(1000);
				}catch(InterruptedException ex)
				{
					break;
					// just exit.
				}
			}
		}
	};

	@Override
	public void stop()
	{
		try
		{
			monitor.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void start(Stage stage)
	{
		stage.setTitle("GEP Buffer Monitor");

		//defining the axes
		xAxis = new NumberAxis();
		xAxis.setAnimated(true);
		xAxis.setForceZeroInRange(false);

		yAxis = new NumberAxis();
		yAxis.setLabel("Messages in backlog");

		//creating the chart
		lineChart = new LineChart<Number,Number>(xAxis,yAxis);

		lineChart.setTitle("Message backlog for each Input/Output over time.");

		try
		{
			monitor = new Monitor( new String[0] );

			updateDisplay();

			Scene scene  = new Scene(lineChart,800,600);

			stage.setScene(scene);
			stage.show();

			updater.setDaemon(true);
			updater.start();
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void updateDisplay() throws Exception
	{
		Map<String,Long> counts = monitor.getCounts();
		for( String outputName : counts.keySet() )
		{
			XYChart.Series series = null;
			long x = (System.currentTimeMillis() - startTime)/1000;
			long v = counts.get(outputName);
			if( ! seriesSet.containsKey(outputName) )
			{
				series = new XYChart.Series();
				series.setName(outputName + " (" + v + ")");
				lineChart.getData().add(series);
				seriesSet.put(outputName, series);
			}
			series = seriesSet.get(outputName);
            series.setName(outputName + " (" + v + ")");
			if( v < 0 )
				v = 0;
			//System.out.println("Adding value " +x+","+v+" to series " + series.getName());
			ObservableList seriesData = series.getData();
			if( seriesData.size() >= maxHistory  )
			{
				seriesData.remove(0);
				//xAxis.setLowerBound(x - maxHistory);
			}
			seriesData.add( new XYChart.Data( x, v ) );

		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}