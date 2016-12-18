// a "stair" or "terraced" function - leftwise step function where the difference between contiguous plateaus is constant. represented as a list of positions and step directions
StairFunction {
	var <>startValue, <stepPositions, <stepDirections, <stepCount;

	*new {
		arg startValue, stepPositions = [], stepDirections = [];
		^super.new.pr_init(startValue, stepPositions, stepDirections);
	}

	pr_init {
		arg startValue, stepPositions = [], stepDirections = [];
		if(stepPositions.isKindOf(Array).not) {
			Error("stepPositions must be an Array").throw;
		};
		if(stepDirections.isKindOf(Array).not) {
			Error("stepDirections must be an Array").throw;
		};
		if(stepDirections.size != stepPositions.size) {
			Error("stepPositions and stepDirections must be the same size").throw;
		};
		this.startValue = startValue;
		this.stepPositions = stepPositions;
		this.stepDirections = stepDirections;
		stepCount = stepPositions.size;
		this.sortSteps();
	}

	sortSteps {
		var order = stepPositions.order;
		stepPositions = stepPositions[order];
		stepDirections = stepDirections[order];
	}

	add {
		arg pos, dir;
		stepPositions = stepPositions.add(pos);
		stepDirections = stepDirections.add(dir);
		this.sortSteps();
		stepCount = stepCount + 1;
	}

	addAll {
		arg poss, dirs;
		if(poss.size != dirs.size) {
			Error("stepPositions and stepDirections must be the same size").throw;
		};
		stepPositions = stepPositions.addAll(poss);
		stepDirections = stepDirections.addAll(dirs);
		this.sortSteps();
		stepCount = stepCount + poss.size;
	}

	remove {
		arg pos;
		var index = stepPositions.indexOf(pos);
		if(index!=nil) {
			stepPositions.removeAt(index);
			stepDirections.removeAt(index);
		} {
			Error("remove: position not found").throw;
		}
	}

	removeAt {
		arg index;
		if(index < 0 || index >= stepCount) {
			Error("index out of range").throw;
		}
	}
}

// a "stair" or "terraced" function with an integer interval domain
DiscreteStairFunction : StairFunction{

}