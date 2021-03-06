/*********************************************************************************************
 * 
 * 
 * 'ExperimentParametersView.java', in plugin 'msi.gama.application', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.gui.views;

import java.util.Collection;
import msi.gama.common.util.GuiUtils;
import msi.gama.gui.swt.*;
import msi.gama.gui.swt.controls.GamaToolbar2;
import msi.gama.kernel.experiment.*;
import msi.gama.runtime.*;
import msi.gaml.compilation.GamaHelper;
import msi.gaml.statements.*;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class ExperimentParametersView extends AttributesEditorsView<String> {

	public static final String ID = GuiUtils.PARAMETER_VIEW_ID;
	public final static int REVERT = 0;
	private IExperimentPlan experiment;

	@Override
	public void ownCreatePartControl(final Composite view) {
		final Composite intermediate = new Composite(view, SWT.NONE);
		intermediate.setBackground(view.getBackground());
		final GridLayout parentLayout = new GridLayout(1, false);
		parentLayout.marginWidth = 0;
		parentLayout.marginHeight = 0;
		parentLayout.verticalSpacing = 5;
		intermediate.setLayout(parentLayout);
		view.pack();
		view.layout();
		parent = intermediate;
	}

	public void addItem(final IExperimentPlan exp) {
		if ( exp != null /* && exp != experiment */) {
			experiment = exp;
			reset();
			editors = (EditorsList<String>) exp.getParametersEditors();
			if ( editors == null && exp.getUserCommands().isEmpty() ) { return; }
			displayItems();
		} else {
			experiment = null;
		}
	}

	// @Override
	// public/* final */void createPartControl(final Composite composite) {
	// super.createPartControl(composite);
	// }

	@Override
	public void displayItems() {
		super.displayItems();
		this.displayCommands();
	}

	protected void displayCommands() {
		final Collection<UserCommandStatement> userCommands = experiment.getUserCommands();

		String expInfo =
			"Model " + experiment.getModel().getDescription().getTitle() + " / " +
				StringUtils.capitalize(experiment.getDescription().getTitle());
		toolbar.status((Image) null, expInfo, IGamaColors.NEUTRAL, SWT.LEFT);
		toolbar.sep(2, SWT.LEFT);
		for ( final IStatement command : userCommands ) {
			ToolItem f = toolbar.button(IGamaColors.BLUE, command.getName(), new SelectionAdapter() {

				@Override
				public void widgetSelected(final SelectionEvent e) {
					GAMA.getSimulation().getScheduler().executeOneAction(new GamaHelper() {

						@Override
						public Object run(final IScope scope) {
							final Object result = command.executeOn(scope);
							experiment.getSimulationOutputs().forceUpdateOutputs();
							return result;
						}

					});
				}

			}, SWT.LEFT);
			toolbar.sep(2, SWT.LEFT);
		}
		toolbar.refresh(true);

	}

	@Override
	public void createToolItems(final GamaToolbar2 tb) {
		super.createToolItems(tb);
		tb.button(IGamaIcons.ACTION_REVERT.getCode(), "Revert parameter values",
			"Revert parameters to their initial values", new SelectionAdapter() {

				@Override
				public void widgetSelected(final SelectionEvent e) {
					EditorsList eds = (EditorsList) GAMA.getExperiment().getParametersEditors();
					if ( eds != null ) {
						eds.revertToDefaultValue();
					}
				}

			}, SWT.RIGHT);
	}

	@Override
	public boolean addItem(final String object) {
		createItem(parent, object, true);
		return true;
	}

	public IExperimentPlan getExperiment() {
		return experiment;
	}

	@Override
	public void stopDisplayingTooltips() {
		displayCommands();
	}

	@Override
	protected GamaUIJob createUpdateJob() {
		return null;
	}

}
