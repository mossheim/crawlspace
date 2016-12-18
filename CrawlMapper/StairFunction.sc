// a "stair" or "terraced" function - leftwise step function where the difference between contiguous plateaus is constant. represented as a list of positions and step directions
StairFunction {
	var <>startValue, <stepPositions, <stepDirections;

	*new {
		arg startValue, stepPositions = [], stepDirections = [];
		^super.new.pr_init(startValue, stepPositions, stepDirections);
	}
}

// a "stair" or "terraced" function with an integer interval domain
DiscreteStairFunction : StairFunction{

}