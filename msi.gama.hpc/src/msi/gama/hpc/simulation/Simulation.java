/*********************************************************************************************
 * 
 * 
 * 'Simulation.java', in plugin 'msi.gama.hpc', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.hpc.simulation;

import java.util.ArrayList;

public class Simulation {

	/**
	 * Variable listeners
	 */

	public ArrayList<Result> result;

	/**
	 * current step
	 */
	// private int step;

	/**
	 * id of current experiment
	 */
	private int experimentID;
	public int maxStep;

	public void addResult(final Result p) {
		this.result.add(p);
	}

	public Simulation(final int expId) {
		this.experimentID = expId;
		result = new ArrayList<Result>();
		initialize();
	}

	public void setup() {
		// this.step = 0;
		// this.model.setup();
	}

	public int getExperimentID() {
		return experimentID;
	}

	public void setExperimentID(final int experimentID) {
		this.experimentID = experimentID;
	}

	public void initialize() {
		// results = new Vector<Result>();

	}

}
