/**
 * Copyright (c) 2018 Inversebit
 *
 * This code is free under the MIT License.
 * Full license text: https://opensource.org/licenses/MIT
 *
 * AccelLogger. This app logs accelerometer data as fast as possible
 * and lets you mark special events. The output is written to a CSV file.
 */

package org.inversebit.accellogger;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.TimeZone;

public class MainActivity extends Activity implements SensorEventListener
{
	//CONSTANTS
	private final static String ACCLOG = "ACCLOG";

	//Properties for GUI management
	private boolean pressed;

	private Button logBtn;
	private Button startBtn;
	private Button stopBtn;

	//Properties for sensor management
	private SensorManager mSensorManager;

	//Properties for output management
	private BufferedWriter writer;

	private Formatter csvformatter;
	private StringBuilder formatterOutput;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//Keep screen on, otherwise the app would be stopped
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		//Init properties
		logBtn = findViewById(R.id.logbtn);
		startBtn = findViewById(R.id.startbtn);
		stopBtn = findViewById(R.id.stopbtn);

		formatterOutput = new StringBuilder();
		csvformatter = new Formatter(formatterOutput);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		stopLogging(null);
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		try
		{
			formatterOutput.setLength(0);
			if(pressed)
			{
				pressed = false;
				csvformatter.format("%d, %f, %f, %f, 1", System.currentTimeMillis(), event.values[0], event.values[1], event.values[2]);
				writer.write(formatterOutput.toString());
			}
			else{
				csvformatter.format("%d, %f, %f, %f", System.currentTimeMillis(), event.values[0], event.values[1], event.values[2]);
				writer.write(formatterOutput.toString());
			}

			writer.newLine();
		}
		catch(Exception e)
		{

		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	/**
	 * Called from GUI
	 *
	 * When the "Log bump" button is pressed the "pressed" variable will be activated
	 * and in the next sensor change the bump will be log and the variable reset
	 */
	public void logBump(View v)
	{
		pressed = true;
	}

	/**
	 * Called from GUI
	 *
	 * This function initializes the CSV output file, subscribes to a sensor stream and
	 * sets the GUI to a new state
	 */
	public void startLogging(View v)
	{
		//Prepare CSV filename
		SimpleDateFormat form = new SimpleDateFormat("yy_MM_dd-hh_mm_ss");
		form.setTimeZone(TimeZone.getTimeZone("utc"));
		String possibleName = form.format(new Date()) + ".csv";

		try
		{
			//Try to get file
			File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), possibleName);
			f.createNewFile();

			//Prepare output
			writer = new BufferedWriter(new FileWriter(f));

			//Write header
			writer.write("ts,accX,accY,accZ");
			writer.newLine();

			//Init sensor stream
			mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)
			{
				Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
			}

			//Set GUI to new state
			enableLogBtn(true);
		}
		catch (Exception e)
		{
			Log.e(ACCLOG, "startLogging: ", e);

			//Housekeeping in case of error
			if(writer != null)
			{
				try
				{
					writer.close();
				}catch(Exception e2)
				{
					Log.e(ACCLOG, "startLogging: ", e2);
				}
			}
			writer = null;
		}
	}

	/**
	 * Called from GUI and this class
	 */
	public void stopLogging(View v)
	{
		if(mSensorManager != null)
			mSensorManager.unregisterListener(this);

		if(writer != null)
		{
			try
			{
				writer.close();
				enableLogBtn(false);
			}
			catch(Exception e)
			{
				Log.e(ACCLOG, "stopLogging: ", e);
			}
		}
	}

	/**
	 * This function plays around with the GUI buttons to set one of the
	 * two available states
	 */
	private void enableLogBtn(boolean enable)
	{
		logBtn.setEnabled(enable);
		stopBtn.setEnabled(enable);
		startBtn.setEnabled(!enable);
	}
}
