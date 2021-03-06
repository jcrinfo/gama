model tuto_segregation_03

global {
	// parameters
	int number_of_people <- 200 ; 
	int dimensions <- 20 ; 
	
	rgb color_1 <- rgb('yellow'); 
	rgb color_2 <- rgb('red');
	
	int neighbours_distance <- 2;
	float percent_similar_wanted <- 0.7;
		
	// Other global variables
	int number_of_groups <- 2;	
	list colors <- [color_1, color_2] of: rgb;
		
	init {
		create people number: number_of_people;
	}
}

environment width: dimensions height: dimensions;

entities {
	species people  { 
		int group_id;
		rgb color;		
		bool is_happy <- false;
		int similar_nearby <- 0;
		
		init {
			set group_id <- rnd(number_of_groups - 1);		 // rnd(int) returns a random integer in [0,int]
			set color <- colors at (group_id);
		} 
				
		reflex compute_state {
			let my_neighbours type: list of: people value: self compute_neighbors [];
			let total_nearby type: int value: length(my_neighbours);			
			
			set similar_nearby <- (my_neighbours count (each.color = color)); 
			set is_happy <- (similar_nearby >= (percent_similar_wanted * total_nearby )) ;			
		}
		
		reflex migrate when: !is_happy {
			do move;
		}							
		
		action compute_neighbors type: list of: people {
			return ((self neighbours_at neighbours_distance) of_species people);
		}	
			
		action move {
			set location <- {(rnd(dimensions * 100) / 100), (rnd(dimensions * 100) / 100)};			
		} 	
								
		aspect default { 
			draw circle(0.5) color: rgb('blue'); 
		}
		
		aspect with_group_color { 
			draw circle(0.5) color: color; 
		}		
	}
} 

experiment schelling type: gui {	
	parameter 'Number of people' var: number_of_people min: 1 max: 2000 category:'Init Environment';
	parameter 'Dimensions of the space' var: dimensions min: 5 max: 100 category: 'Init Environment';
	parameter 'Color of group 1:' var: color_1 category: 'Configuration of groups';
	parameter 'Color of group 2:' var: color_2 category: 'Configuration of groups';
		
	parameter 'Distance of perception:' var: neighbours_distance max: 10 min: 1 category: 'Population' ;
	parameter 'Desired percentage of similarity:' var: percent_similar_wanted min: float(0) max: float(1) category: 'Population' ;	
		
	
	output {		
		display Segregation {
			species people aspect: with_group_color;
		}	
		
		monitor 'number_of_happy' value: length(list(people) where (each.is_happy = true));		
	}
}