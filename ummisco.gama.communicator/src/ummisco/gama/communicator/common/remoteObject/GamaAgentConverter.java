/*********************************************************************************************
 * 
 * 
 * 'GamaAgentConverter.java', in plugin 'ummisco.gama.communicator', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package ummisco.gama.communicator.common.remoteObject;

import msi.gama.metamodel.agent.GamlAgent;
import com.thoughtworks.xstream.converters.*;
import com.thoughtworks.xstream.io.*;

public class GamaAgentConverter implements Converter {

	public GamaAgentConverter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean canConvert(final Class arg0) {
		return arg0.equals(GamlAgent.class);
	}

	@Override
	public void marshal(final Object arg0, final HierarchicalStreamWriter writer, final MarshallingContext arg2) {
		GamlAgent agt = (GamlAgent) arg0;
		arg2.convertAnother(new RemoteAgent(agt));
	}

	@Override
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext arg1) {

		reader.moveDown();
		RemoteAgent rmt = (RemoteAgent) arg1.convertAnother(null, RemoteAgent.class);
		reader.moveUp();
		return rmt; // ragt;
	}

}
