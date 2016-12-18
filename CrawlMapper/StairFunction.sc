// representation of a "stair function" or "terraced function" - rightwise step function where the "steps" are of constant value.

StairFunction {
	var <>startingValue, <>stairPositions, <>stairDirections, <>minStairGap;

	*new {
		arg startingValue, stairPositions = [], stairDirections = [], minStairGap = 0;
		^super.new.pr_init(startingValue, stairPositions, stairDirections);
	}

	pr_init {
		arg startingValue, stairPositions = [], stairDirections = [], minStairGap = 0;
		if(stairPositions.isKindOf(Array).not) {
			Error("stairPositions must be an Array").throw;
		};
		if(stairDirections.isKindOf(Array).not) {
			Error("stairDirections must be an Array").throw;
		};
		this.startingValue = startingValue;
		this.stairPositions = stairPositions;
		this.stairDirections = stairDirections;
		this.minStairGap = minStairGap;
	}





}