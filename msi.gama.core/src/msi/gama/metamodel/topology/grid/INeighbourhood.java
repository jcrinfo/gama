/*********************************************************************************************
 * 
 * 
 * 'INeighbourhood.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.metamodel.topology.grid;

import java.util.Set;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;

/**
 * Class INeighbourhood.
 * 
 * @author drogoul
 * @since 19 mai 2013
 * 
 */
public interface INeighbourhood {

	public abstract Set<IAgent> getNeighboursIn(IScope scope, final int placeIndex, final int radius);

	public abstract boolean isVN();

	/**
	 * @param placeIndex
	 * @param range
	 * @return
	 */
	public abstract int[] getRawNeighboursIncluding(IScope scope, int placeIndex, int range);

	/**
	 * @param placeIndex
	 * @param n
	 * @return
	 */
	public abstract int neighboursIndexOf(IScope scope, int placeIndex, int n);

}