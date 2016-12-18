// a "stair" or "terraced" function - leftwise step function where the difference between contiguous plateaus is constant. represented as a list of positions and step directions
StairFunction {
	var <>startValue, <stepPositions, <stepDirections;

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
		this.startValue = startValue;
		this.stepPositions = stepPositions;
		this.stepDirections = stepDirections;
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
	}

	addAll {
		arg poss, dirs;
		stepPositions.addAll(poss);
		stepDirections.addAll(dirs);
		this.sortSteps();
	}
}

// a "stair" or "terraced" function with an integer interval domain
DiscreteStairFunction : StairFunction{

}