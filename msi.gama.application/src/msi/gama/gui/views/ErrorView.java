/*
 * GAMA - V1.4 http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC & Partners (see below)
 * 
 * Developers :
 * 
 * - Alexis Drogoul, UMI 209 UMMISCO, IRD/UPMC (Kernel, Metamodel, GAML), 2007-2012
 * - Vo Duc An, UMI 209 UMMISCO, IRD/UPMC (SWT, multi-level architecture), 2008-2012
 * - Patrick Taillandier, UMR 6228 IDEES, CNRS/Univ. Rouen (Batch, GeoTools & JTS), 2009-2012
 * - Beno�t Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2012
 * - Phan Huy Cuong, DREAM team, Univ. Can Tho (XText-based GAML), 2012
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */
package msi.gama.gui.views;

import java.util.*;
import java.util.List;
import msi.gama.common.interfaces.*;
import msi.gama.common.util.GuiUtils;
import msi.gama.gui.parameters.EditorFactory;
import msi.gama.gui.swt.SwtGui;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class ErrorView extends ExpandableItemsView<GamaRuntimeException> {

	public static String ID = GuiUtils.ERROR_VIEW_ID;

	private final ArrayList<GamaRuntimeException> exceptions = new ArrayList();
	static private int numberOfDisplayedErrors = 10;
	static private boolean mostRecentFirst = true;
	static public boolean showErrors = true;

	// ParameterExpandItem parametersItem;

	@Override
	protected boolean areItemsClosable() {
		return true;
	}

	@Override
	public boolean addItem(final GamaRuntimeException e) {
		createItem(e, false);
		return true;
	}

	public synchronized void addNewError(final GamaRuntimeException ex) {
		for ( final GamaRuntimeException e : exceptions ) {
			if ( e.equivalentTo(ex) ) {
				e.addAgent(ex.getAgent());
				if ( showErrors ) {
					reset();
					displayItems();
				}
				return;
			}
		}
		exceptions.add(ex);

		gotoEditor(ex);

		if ( showErrors ) {
			reset();
			displayItems();
		}
	}

	/**
	 * @see msi.gama.gui.views.GamaViewPart#getToolbarActionsId()
	 */
	@Override
	protected Integer[] getToolbarActionsId() {
		// TODO Need to be defined and usable (not the case now)
		return new Integer[] { PAUSE /* , CLEAR */};
	}

	@Override
	public void ownCreatePartControl(final Composite view) {
		super.ownCreatePartControl(view);
		final Composite intermediate = new Composite(view, SWT.VERTICAL);
		final GridLayout parentLayout = new GridLayout(1, false);
		parentLayout.marginWidth = 0;
		parentLayout.marginHeight = 0;
		parentLayout.verticalSpacing = 0;
		intermediate.setLayout(parentLayout);
		final Composite parameters = new Group(intermediate, SWT.None);
		final GridLayout layout = new GridLayout(2, false);

		parameters.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		layout.verticalSpacing = 0;
		parameters.setLayout(layout);

		// final IntEditor ed =
		EditorFactory.create(parameters, "Display last ", null, numberOfDisplayedErrors, 0, 100, 1, false,
			new EditorListener<Integer>() {

				@Override
				public void valueModified(final Integer newValue) {
					if ( newValue == numberOfDisplayedErrors ) { return; }
					numberOfDisplayedErrors = newValue;
					reset();
					displayItems();
				}

			});

		EditorFactory.create(parameters, "Most recent first", mostRecentFirst, new EditorListener<Boolean>() {

			@Override
			public void valueModified(final Boolean newValue) {

				mostRecentFirst = newValue;
				reset();
				displayItems();
			}

		});

		parameters.pack();
		parent = intermediate;
	}

	private void gotoEditor(final GamaRuntimeException exception) {

		final EObject o = exception.getEditorContext();
		if ( o != null && GAMA.REVEAL_ERRORS_IN_EDITOR ) {
			GuiUtils.asyncRun(new Runnable() {

				@Override
				public void run() {
					GuiUtils.openEditorAndSelect(o);
				}
			});
		}

	}

	@Override
	protected Composite createItemContentsFor(final GamaRuntimeException exception) {
		final ScrolledComposite compo = new ScrolledComposite(getViewer(), SWT.NONE);
		final GridLayout layout = new GridLayout(1, false);
		final GridData firstColData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layout.verticalSpacing = 5;
		compo.setLayout(layout);
		final Table t = new Table(compo, SWT.H_SCROLL | SWT.V_SCROLL);

		t.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				gotoEditor(exception);
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {}
		});
		t.setLayoutData(firstColData);
		final java.util.List<String> strings = exception.getContextAsList();
		t.setLinesVisible(true);
		t.setForeground(exception.isWarning() ? SwtGui.COLOR_WARNING : SwtGui.COLOR_ERROR);
		final TableColumn c = new TableColumn(t, SWT.NONE);
		c.setResizable(true);
		final TableColumn column2 = new TableColumn(t, SWT.NONE);
		for ( int i = 0; i < strings.size(); i++ ) {
			final TableItem item = new TableItem(t, SWT.NONE);
			item.setText(new String[] { String.valueOf(i), strings.get(i) });
		}
		c.pack();
		column2.pack();
		t.setSize(t.computeSize(1000, 200));
		compo.setContent(t);
		compo.setSize(compo.computeSize(SWT.DEFAULT, 200));
		return compo;
	}

	@Override
	public void removeItem(final GamaRuntimeException obj) {
		exceptions.remove(obj);
	}

	@Override
	public void pauseItem(final GamaRuntimeException obj) {}

	@Override
	public void resumeItem(final GamaRuntimeException obj) {}

	@Override
	public String getItemDisplayName(final GamaRuntimeException obj, final String previousName) {
		final StringBuilder sb = new StringBuilder(300);
		final String a = obj.getAgent();
		if ( a != null ) {
			sb.append(a).append(" at ");
		}
		sb.append("cycle ").append(obj.getCycle()).append(ItemList.SEPARATION_CODE)
			.append(obj.isWarning() ? ItemList.WARNING_CODE : ItemList.ERROR_CODE).append(obj.getMessage());
		return sb.toString();
	}

	@Override
	public void focusItem(final GamaRuntimeException data) {}

	@Override
	public List<GamaRuntimeException> getItems() {
		final List<GamaRuntimeException> errors = new ArrayList();
		final int size = exceptions.size();
		if ( size == 0 ) { return errors; }
		final int end = size;
		int begin = end - numberOfDisplayedErrors;
		begin = begin < 0 ? 0 : begin;
		errors.addAll(exceptions.subList(begin, end));
		if ( mostRecentFirst ) {
			Collections.reverse(errors);
		}
		return errors;
	}

	@Override
	public void updateItemValues() {
		this.getViewer().updateItemNames();
	}

	public void clearErrors() {
		this.reset();
		exceptions.clear();
		displayItems();
	}

}
