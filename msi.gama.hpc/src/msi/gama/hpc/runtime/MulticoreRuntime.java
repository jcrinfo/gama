/*********************************************************************************************
 * 
 *
 * 'MulticoreRuntime.java', in plugin 'msi.gama.hpc', is part of the source code of the 
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.hpc.runtime;

import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;

import msi.gama.hpc.common.HPCExperiment;

public class MulticoreRuntime extends Observable {
	
	public final static int UN_SUBMITED = 0;
	public final static int PENDING = 1;
	public final static int RUNNING = 2;
	public final static int FINISHED = 3;
	
	private int state;
	private long computeDuration;
	HPCExperiment experiment;
	private String inputPath;
	private String outputPath;
	
	
	
	public String getInputPath() {
		return inputPath;
	}

	public void setInputPath(String inputPath) {
		this.inputPath = inputPath;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	MulticoreRuntime(HPCExperiment exp, String in, String out)
	{
		this.experiment = exp;
		state = UN_SUBMITED;	
		this.inputPath = in;
		this.outputPath = out;
	}
	
	MulticoreRuntime(int st)
	{
		state = st;	
	}
	void pending()
	{
		this.state = PENDING;
		this.notifyListener();
	}
	
	void start()
	{
		this.state = RUNNING;
		this.computeDuration=Calendar.getInstance().getTimeInMillis();
		this.notifyListener();
	}
	
	void stop()
	{
		this.state = FINISHED;
		this.computeDuration= Calendar.getInstance().getTimeInMillis() - computeDuration;
		this.notifyListener();
	}
	
	void setComputeDuration(long cd)
	{
		this.computeDuration=cd;
	}
	
	public void notifyListener()
	{
		this.setChanged();
		this.notifyObservers();
	}
	
	public void addListener(Observer obs)
	{
		this.addObserver(obs);
	}
	
	public int getState()
	{
		return state;
	}
	
	public long getComputeDuration()
	{
		return this.computeDuration;
	}
	
	public HPCExperiment getExperiment()
	{
		return this.experiment;
	}

}
