/**
 *  comparison_ABM_EBM_SIR.gaml
 *  Author: Benoit Gaudou and 
 *  Description: Comparison between an agent-based and an equation-based model of the SIR model.
 */
model comparison_ABM_EBM_SIR

global {
	int number_S <- 499; // The number of susceptible
	int number_I <- 1; // The number of infected
	int number_R <- 0; // The number of removed
	float beta <- 0.1; // The parameter Beta
	float gamma <- 0.01; // The parameter Delta
	int neighbours_size <- 2;
	int N <- number_S + number_I + number_R;
	int nb_hosts <- number_S + number_I + number_R;
	int nb_infected <- number_I;
	float hKR4 <- 0.7;
	geometry shape <- square(50);
	init {
		create Host number: number_S {
			is_susceptible <- true;
			is_infected <- false;
			is_immune <- false;
			color <- #green;
		}

		create Host number: number_I {
			is_susceptible <- false;
			is_infected <- true;
			is_immune <- false;
			color <- #red;
		}

		create node_agent number: 1 {
			S <- float(number_S);
			I <- float(number_I);
			R <- float(number_R);
		}
	}

	reflex compute_nb_infected_hosts {
		nb_infected <- Host count (each.is_infected);
		nb_hosts <- length(Host);
	}

}

grid sir_grid width: 50 height: 50 {
		rgb color <- #black;
		list<sir_grid> neighbours <- (self neighbours_at neighbours_size) of_species sir_grid;
	}
species Host {
	bool is_susceptible <- true;
	bool is_infected <- false;
	bool is_immune <- false;
	rgb color <- #green;
	int sic_count <- 0;
	sir_grid myPlace;
    int ngb_infected_number function: {self neighbours_at(neighbours_size) count(each.is_infected)};
	
	init {
		myPlace <- one_of(sir_grid as list);
		location <- myPlace.location;
	}

	reflex basic_move {
		myPlace <- one_of(myPlace.neighbours);
		location <- myPlace.location;
	}

	reflex become_infected when: is_susceptible {
    		if (flip(1 - (1 - beta)  ^ ngb_infected_number)) {
        		is_susceptible <-  false;
	            	is_infected <-  true;
	            	is_immune <-  false;
	            	color <-  #red;       			
			}    				
	}

	reflex become_immune when: (is_infected and flip(gamma)) {
		is_susceptible <- false;
		is_infected <- false;
		is_immune <- true;
		color <- #yellow;
	} 
	
	aspect basic {
		draw circle(1) color: color;
	}

}

species node_agent {
	float t;
	float I;
	float S;
	float R;
	equation eqSIR type: SIR vars: [S, I, R, t] params: [N, beta, gamma];
	reflex solving {solve eqSIR method: rk4 step: 1;}
	
}

experiment Simulation_ABM_EBM type: gui {
	parameter 'Number of Susceptible' type: int var: number_S <- 495 category: "Initial population";
	parameter 'Number of Infected' type: int var: number_I <- 5 category: "Initial population";
	parameter 'Number of Removed' type: int var: number_R <- 0 category: "Initial population";
	parameter 'Beta (S->I)' type: float var: beta <- 0.1 category: "Parameters";
	parameter 'Gamma (I->R)' type: float var: gamma <- 0.01 category: "Parameters";
	parameter 'Size of the neighbours' type: int var: neighbours_size <- 1 min: 1 max: 5 category: "Infection";
	output {
		display sir_display { 
			grid sir_grid lines: #black;
			species Host aspect: basic;	
		}
		display ABM { 
			chart 'Susceptible' type: series background: #lightgray style: exploded {
				data 'susceptible' value: (Host as list) count (each.is_susceptible) color: #green;
				data 'infected' value: (Host as list) count (each.is_infected) color: #red;
				data 'immune' value: (Host as list) count (each.is_immune) color: #blue;
			}
		}
		display EBM { 
			chart "SIR" type: series background: #white {
				data 'S' value: first(node_agent).S color: #green;
				data 'I' value: first(node_agent).I color: #red;
				data 'R' value: first(node_agent).R color: #blue;
			}
		}
		display ABM_EBM  { 
			chart 'Susceptible' type: series background: #lightgray style: exploded {
				data 'susceptible' value: (Host as list) count (each.is_susceptible) color: #green-50;
				data 'infected' value: (Host as list) count (each.is_infected) color: #red-50;
				data 'immune' value: (Host as list) count (each.is_immune) color: #blue-50;
				data 'S' value: first(node_agent).S color: #green+50;
				data 'I' value: first(node_agent).I color: #red+50;
				data 'R' value: first(node_agent).R color: #blue+50;
			}
		}
	}

}
