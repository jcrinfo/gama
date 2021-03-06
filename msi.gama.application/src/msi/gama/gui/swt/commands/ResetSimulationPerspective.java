/*********************************************************************************************
 * 
 *
 * 'ResetSimulationPerspective.java', in plugin 'msi.gama.application', is part of the source code of the 
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.gui.swt.commands;

import msi.gama.runtime.GAMA;
import org.eclipse.core.commands.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.ui.*;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.*;

public class ResetSimulationPerspective extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		if ( activeWorkbenchWindow != null ) {
			WorkbenchPage page = (WorkbenchPage) activeWorkbenchWindow.getActivePage();
			if ( page != null ) {
				IPerspectiveDescriptor descriptor = page.getPerspective();
				if ( descriptor != null ) {
					String message =
						"Resetting the simulation perspective will close the current simulation. Do you want to proceed ?";
					boolean result =
						MessageDialog.open(MessageDialog.QUESTION, activeWorkbenchWindow.getShell(),
							WorkbenchMessages.ResetPerspective_title, message, SWT.SHEET);
					if ( result ) {
						GAMA.controller.closeExperiment();
						page.resetPerspective();
					}

				}
			}
		}

		return null;

	}

}
