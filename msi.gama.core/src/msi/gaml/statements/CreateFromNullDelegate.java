/**
 * Created by drogoul, 27 mai 2015
 *
 */
package msi.gaml.statements;

import java.util.*;
import msi.gama.common.interfaces.ICreateDelegate;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaMapFactory;
import msi.gaml.types.*;

/**
 * Class CreateFromDatabaseDelegate.
 *
 * @author drogoul
 * @since 27 mai 2015
 *
 */
public class CreateFromNullDelegate implements ICreateDelegate {

	/**
	 * Method acceptSource()
	 *
	 * @see msi.gama.common.interfaces.ICreateDelegate#acceptSource(java.lang.Object)
	 */
	@Override
	public boolean acceptSource(final Object source) {

		return source == null;
	}

	/**
	 * Method createFrom() reads initial values decribed by the modeler (facet with)
	 *
	 * @author Alexis Drogoul
	 * @since 04-09-2012
	 * @see msi.gama.common.interfaces.ICreateDelegate#createFrom(msi.gama.runtime.IScope, java.util.List, int, java.lang.Object)
	 */
	@Override
	public boolean createFrom(final IScope scope, final List<Map> inits, final Integer max, final Object input,
		final Arguments init, final CreateStatement statement) {
		if ( init == null ) { return true; }
		final int num = max == null ? 1 : max;
		for ( int i = 0; i < num; i++ ) {
			final Map map = GamaMapFactory.create(Types.NO_TYPE, Types.NO_TYPE);
			statement.fillWithUserInit(scope, map);
			inits.add(map);
		}
		return true;
	}

	/**
	 * Method fromFacetType()
	 * @see msi.gama.common.interfaces.ICreateDelegate#fromFacetType()
	 */
	@Override
	public IType fromFacetType() {
		return Types.NO_TYPE; // Only delegate allowed to do this
	}

}
