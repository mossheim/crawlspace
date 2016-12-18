// a "stair" or "terraced" function - leftwise step function where the difference between contiguous regions is 1. represented as a list of positions and step directions
StairFunction {
	var <>startValue, <stepPositions, <stepDirections, <stepCount;

	*new {
		arg startValue = 0;
		^super.new.pr_init(startValue);
	}

	pr_init {
		arg startValue;
		this.startValue = startValue;
		stepPositions = [];
		stepDirections = [];
		stepCount = 0;
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

	clear {
		stepPositions = [];
		stepDirections = [];
		stepCount = 0;
	}

	heightAt {
		arg x;
		var i = 0, value = startValue;
		while {i < stepPositions.size} {
			var pos, dir;
			pos = stepPositions[i];
			if(pos > x) {^value};
			dir = stepDirections[i];
			value = value + dir.sign;
			i = i+1;
		};
		^value;
	}
}

// a "stair" or "terraced" function with an integer interval domain
DiscreteStairFunction : StairFunction {
	var <leftBound, <rightBound, <minStepGap;

	*new {
		arg startValue = 0, leftBound = 0, rightBound, minStepGap = 1;
		super.new(startValue).pr_init(leftBound, rightBound, minStepGap);
	}

	pr_init {
		arg leftBound, rightBound, minStepGap;
		this.leftBound = leftBound;
		this.rightBound = rightBound;
		this.minStepGap = minStepGap;
	}


}