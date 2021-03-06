/*********************************************************************************************
 * 
 * 
 * 'HelpHandler.java', in plugin 'msi.gama.application', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.gui.swt.commands;

import msi.gama.common.util.GuiUtils;
import org.eclipse.core.commands.*;

public class HelpHandler extends AbstractHandler {

	public HelpHandler() {}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		GuiUtils.showWebEditor("http://doc.gama-platform.org", null);
		return null;
	}
}
