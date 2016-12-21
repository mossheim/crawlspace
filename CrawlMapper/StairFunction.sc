// A "stair" or "terraced" function.
// Right-continuous step function where the difference between contiguous regions is 1. The function is represented as a list of positions and step directions.
StairFunction {
	var <>startValue, <stepPositions, <stepDirections, <stepCount;
	classvar stepDirectionList;

	*new {
		arg startValue = 0;
		^super.new.pr_init_stairFunction(startValue);
	}

	*directions {
		^stepDirectionList ? stepDirectionList = [\up, \down];
	}

	*directionValue {
		arg direction;

		^switch(direction)
		{\up} {1}
		{\down} {-1}
		{0};
	}

	pr_init_stairFunction {
		arg startValue;
		this.startValue = startValue;
		stepPositions = [];
		stepDirections = [];
		stepCount = 0;
		this.pr_sortSteps();
	}

	pr_sortSteps {
		var order = stepPositions.order;
		stepPositions = stepPositions[order];
		stepDirections = stepDirections[order];
	}

	pr_castPosition {
		arg value;
		^value.asFloat;
	}

	add {
		arg position, direction;
		position = this.pr_castPosition(position);
		if(stepPositions.includes(position)) {
			Error("add: requested step position already occupied").throw;
		};
		if(this.class.directions.includes(direction).not) {
			Error("add: unrecognized direction symbol").throw;
		};
		stepPositions = stepPositions.add(position);
		stepDirections = stepDirections.add(direction);
		this.pr_sortSteps();
		stepCount = stepCount + 1;
	}

	addAll {
		arg positions, directions;
		positions = positions.collect(this.pr_castPosition(_));
		if(positions.size != directions.size) {
			Error("addAll: stepPositions and stepDirections must be the same size").throw;
		};
		if(stepPositions.includesAny(positions)) {
			Error("addAll: sets must be disjoint. Common elements: %".format(stepPositions.sect(positions))).throw;
		};
		if(directions.isSubsetOf(this.class.directions).not) {
			Error("addAll: unrecognized direction symbol.").throw;
		};
		stepPositions = stepPositions.addAll(positions);
		stepDirections = stepDirections.addAll(directions);
		this.pr_sortSteps();
		stepCount = stepCount + positions.size;
	}

	remove {
		arg position;
		var index = stepPositions.indexOf(this.pr_castPosition(position));
		if(index!=nil) {
			this.removeAt(index);
			^true;
		};
		^false;
	}

	removeAt {
		arg index;
		if(index < 0 || index >= stepCount) {
			Error("removeAt: index out of range").throw;
		};
		stepPositions.removeAt(index);
		stepDirections.removeAt(index);
		stepCount = stepCount - 1;
	}

	clear {
		stepPositions = [];
		stepDirections = [];
		stepCount = 0;
	}

	heightAt {
		arg x;
		var height = startValue;

		stepPositions.do {
			arg position, i;

			if(position > x)
			  { ^height };

			height = height + this.class.directionValue(stepDirections[i]);
		};
		^height;
	}

	stepAt {
		arg i;
		if(i < 0 || (i >= stepCount)) {
			Error("stepAt: index out of range").throw;
		};
		^[stepPositions[i], stepDirections[i]];
	}

	maxHeight {
		var runningMax = startValue, height = startValue;

		stepPositions.do {
			arg position, i;

			height = height + this.class.directionValue(stepDirections[i]);

			if(height > runningMax)
				{ runningMax = height };
		};

		^runningMax;
	}

	minHeight {
		var runningMin = startValue, height = startValue;

		stepPositions.do {
			arg position, i;

			height = height + this.class.directionValue(stepDirections[i]);

			if(height < runningMin)
				{ runningMin = height };
		};

		^runningMin;
	}

	finalHeight {
		^startValue + stepDirections.collect(this.class.directionValue(_));
	}
}

// A "stair" or "terraced" function with an integer interval domain.
// A minimum step gap is added to limit the distance between any two steps.
DiscreteStairFunction : StairFunction {
	var <leftBound, <rightBound, <minStepGap, <freeIntervals;

	*new {
		arg startValue = 0, leftBound = 0, rightBound, minStepGap = 1;
		^super.new(startValue).pr_init_discreteStairFunction(leftBound, rightBound, minStepGap);
	}

	pr_init_discreteStairFunction {
		arg leftBoundIn, rightBoundIn, minStepGapIn;

		if(leftBoundIn >= rightBoundIn) {
			Error("leftBound must be strictly less than rightBound").throw;
		};
		leftBound = leftBoundIn;
		rightBound = rightBoundIn;
		if(minStepGapIn <= 0) {
			Error("minStepGap must be at least 1").throw;
		};
		minStepGap = minStepGapIn;
		if(minStepGap > (rightBound - leftBound)) {
			freeIntervals = [];
		} {
			freeIntervals = [[leftBound+minStepGap, rightBound-minStepGap]];
		}
	}

	pr_castPosition {
		arg value;
		^value.asInteger;
	}

	emptySlotCount {
		var count = 0;
		freeIntervals.do {
			|interval|
			count = count + interval.last - interval.first + 1;
		};
		^count;
	}

	// get the position of the nth free slot in the function graph
	positionAtFreeSlotIndex {
		arg index = 0;
		var position = 0;
		var intervalIndex = 0;
		if(index < 0) {
			Error("positionAtFreeSlotIndex: index must be nonnegative");
		};
		while { (intervalIndex < stepCount) && (index >= 0) } {
			var interval = freeIntervals[intervalIndex];
			var intervalSize = interval.last - interval.first + 1;
			if(index < intervalSize) {^interval.first + index};
			index = index - intervalSize;
		};
		if(index < 0) {
			Error("positionAtFreeSlotIndex: index exceeds number of free slots");
		}
	}

	intervalsAtDepth {
		// TODO
	}

	intervalsAtDepthOfAtLeast {
		// TODO
	}

	intervalsAtDepthOfAtMost {
		// TODO
	}

	growToHeight {
		// TODO
	}

	growByHeight {
		// TODO
	}
}
