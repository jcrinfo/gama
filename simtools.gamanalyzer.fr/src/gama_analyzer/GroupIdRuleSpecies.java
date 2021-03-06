package gama_analyzer;

import java.util.Iterator;
import java.util.Map;

import msi.gama.metamodel.agent.GamlAgent;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaList;
import msi.gama.util.IList;
import msi.gaml.species.ISpecies;
import java.util.ArrayList;

public class GroupIdRuleSpecies extends GroupIdRule {

	String nom;
	GamaList<GamlAgent> liste = null;

	public GamaList<GamlAgent> getListe() {
		return liste;
	}
	public void setListe(GamaList<GamlAgent> liste) {
		this.liste = liste;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}

	public IList<IAgent> update(IScope scope, IList<IAgent> liste) {

		liste=(IList<IAgent>) new ArrayList<IAgent>();
		Map<String,ISpecies> especes =scope.getModel().getAllSpecies();
		Iterator<String> it=especes.keySet().iterator();
		while (it.hasNext())
		{
			String s=it.next();
			if (s.equals(nom)) {
			Iterator<IAgent> monde = (Iterator<IAgent>) especes.get(s).iterable(scope).iterator();
			while (monde.hasNext()) {
					liste.add(monde.next()); 
				}
			}
			
		}
		return liste;
	}
}
