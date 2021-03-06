/**
 * Created by drogoul, 5 déc. 2014
 * 
 */
package msi.gama.lang.gaml.ui.editor;

import java.util.*;
import msi.gama.common.*;
import msi.gama.gui.swt.commands.GamaMenuItem;
import msi.gama.lang.gaml.ui.XtextGui;
import msi.gama.lang.gaml.ui.templates.GamlTemplateFactory;
import msi.gaml.descriptions.OperatorProto;
import msi.gaml.expressions.IExpressionCompiler;
import msi.gaml.types.*;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.Menu;

/**
 * The class EditToolbarTemplateMenu.
 * 
 * @author drogoul
 * @since 5 déc. 2014
 * 
 */
public class EditToolbarOperatorsMenu extends EditToolbarMenu {

	public static Boolean byName = null;

	@Override
	protected void fillMenu() {
		if ( byName == null ) {
			byName = XtextGui.OPERATORS_MENU_SORT.getValue().equals("Name");
		}
		Menu sub = sub("Sort by...");
		sep();
		check(sub, "Name", byName, new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent event) {
				byName = true;
				reset();
			}
		});
		check(sub, "Category", !byName, new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent event) {
				byName = false;
				reset();
			}
		});
		if ( byName ) {
			fillMenuByName();
		} else {
			fillMenuByCategory();
		}
	}

	protected void fillMenuByName() {
		Map<String, Map<Signature, OperatorProto>> operators = IExpressionCompiler.OPERATORS;
		List<String> nn = new ArrayList(operators.keySet());
		Collections.sort(nn, IGNORE_CASE);
		for ( String name : nn ) {
			List<OperatorProto> protos = new ArrayList();
			for ( Signature sig : operators.get(name).keySet() ) {
				OperatorProto proto = operators.get(name).get(sig);
				if ( proto.getDeprecated() == null ) {
					protos.add(proto);
				}
			}
			if ( protos.isEmpty() ) {
				continue;
			}
			Menu name_menu = sub(name);
			for ( final OperatorProto proto : protos ) {
				final Template t = GamlTemplateFactory.from(proto);
				GamaMenuItem item =
					action(name_menu,
						"(" + proto.signature.asPattern(false) + ") -> " + proto.returnType.serialize(true),
						new SelectionAdapter() {

							@Override
							public void widgetSelected(final SelectionEvent event) {
								applyTemplate(t);
							}
						});
				item.setTooltipText(t.getDescription());
			}
		}
	}

	protected void fillMenuByCategory() {
		Map<String, Map<Signature, OperatorProto>> operators = IExpressionCompiler.OPERATORS;
		final Map<String, Map<String, Map<OperatorProto, Template>>> categories = new LinkedHashMap();
		List<String> nn = new ArrayList(operators.keySet());
		Collections.sort(nn, IGNORE_CASE);
		for ( String name : nn ) {
			Map<Signature, OperatorProto> ops = operators.get(name);
			for ( Signature sig : ops.keySet() ) {
				OperatorProto proto = ops.get(sig);
				if ( proto.getDeprecated() != null ) {
					continue;
				}
				String category = proto.getCategory().replace("-related", "");
				Map<String, Map<OperatorProto, Template>> names = categories.get(category);
				if ( names == null ) {
					names = new LinkedHashMap();
					categories.put(category, names);
				}
				Map<OperatorProto, Template> templates = names.get(name);
				if ( templates == null ) {
					templates = new LinkedHashMap();
					names.put(name, templates);
				}
				templates.put(proto, GamlTemplateFactory.from(proto));
			}
		}
		List<String> cc = new ArrayList(categories.keySet());
		Collections.sort(cc, IGNORE_CASE);
		for ( final String category : cc ) {
			Menu category_menu = sub(category);
			List<String> nn2 = new ArrayList(categories.get(category).keySet());
			Collections.sort(nn2, IGNORE_CASE);
			for ( final String name : nn2 ) {
				List<OperatorProto> protos = new ArrayList(categories.get(category).get(name).keySet());
				//
				Menu name_menu = sub(category_menu, name);
				for ( final OperatorProto proto : protos ) {
					final Template t = categories.get(category).get(name).get(proto);
					GamaMenuItem item =
						action(name_menu,
							"(" + proto.signature.asPattern(false) + ") -> " + proto.returnType.serialize(true),
							new SelectionAdapter() {

								@Override
								public void widgetSelected(final SelectionEvent event) {
									applyTemplate(t);
								}
							});
					item.setTooltipText(t.getDescription());
				}

			}
		}

	}

	@Override
	protected void openView() {}

}
