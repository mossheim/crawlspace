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
		^startValue + stepDirections.sum(this.class.directionValue(_));
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

		leftBound = this.pr_castPosition(leftBoundIn);
		rightBound = this.pr_castPosition(rightBoundIn);

		if(leftBound >= rightBound) {
			Error("leftBound must be strictly less than rightBound").throw;
		};

		minStepGap = this.pr_castPosition(minStepGapIn);

		if(minStepGapIn <= 0) {
			Error("minStepGap must be at least 1").throw;
		};

		freeIntervals = this.pr_gapShrinkInterval([leftBound, rightBound]) !? _.bubble ? [];
	}

	pr_castPosition {
		arg value;

		^value.asInteger;
	}

	pr_gapShrinkInterval {
		arg interval;

		interval = interval + [minStepGap, minStepGap.neg];
		^if(interval[1] < interval[0]) {nil} {interval};
	}

	pr_intervalSize {
		arg interval;

		^interval.last - interval.first + 1;
	}

	freeSlotCount {
		^freeIntervals.sum(this.pr_intervalSize(_));
	}

	isSlotFree {
		arg position;

		position = this.pr_castPosition(position);

		^freeIntervals.any(position.inclusivelyBetween(*_));
	}

	// get the position of the nth free slot in the function graph
	positionAtFreeSlotIndex {
		arg index = 0;
		var position = 0, intervalIndex = 0;

		if(index < 0) {
			Error("positionAtFreeSlotIndex: index must be nonnegative").throw;
		};

		freeIntervals.do {
			arg interval;
			var intervalSize = this.pr_intervalSize(interval);

			if(index < 0) {
				Error("positionAtFreeSlotIndex: index exceeds number of free slots").throw;
			};
			if(index < intervalSize) {^interval.first + index};
			index = index - intervalSize;
		};

		Error("positionAtFreeSlotIndex: index exceeds number of free slots").throw;
	}

	// returns a list of all the intervals that are at a height == target
	intervalsAtHeight {
		arg target = 0;
		var intervalList = [], height = startValue;

		if(height == target) {
			if(stepCount == 0) {
				intervalList = intervalList.add([leftBound, rightBound]);
			} {
				intervalList = intervalList.add([leftBound, stepPositions[0]]);
			}
		};

		stepPositions.do {
			arg position, i;
			var direction = stepDirections[i];

			height = height + this.class.directionValue(direction);

			if(height == target) {
				if(i + 1 < stepCount) {
					intervalList = intervalList.add([position, stepPositions[i + 1]]);
				} {
					intervalList = intervalList.add([position, rightBound]);
				}
			}
		};

		^intervalList;
	}

	add {
		arg position, direction;
		var intervalIndex, interval, leftInterval, rightInterval;

		// should check first that the slot exists
		position = this.pr_castPosition(position);

		this.isSlotFree(position).not.if {
			Error("add: slot % is not free".format(position)).throw;
		};

		super.add(position, direction);

		intervalIndex = freeIntervals.selectIndices(position.inclusivelyBetween(*_)).first;

		interval = freeIntervals.removeAt(intervalIndex);

		leftInterval = [interval.first, position - minStepGap];
		rightInterval = [position + minStepGap, interval.last];

		if(this.pr_intervalSize(rightInterval) > 0) {
			freeIntervals = freeIntervals.insert(intervalIndex, rightInterval);
		};
		if(this.pr_intervalSize(leftInterval) > 0) {
			freeIntervals = freeIntervals.insert(intervalIndex, leftInterval);
		};
	}

	addAll {
		arg positions, directions;

		this.shouldNotImplement(thisMethod);
	}

	remove {
	  // TODO
	}

	removeAt {
		// TODO
	}

	intervalsAtHeightOfAtLeast {
		// TODO?
	}

	intervalsAtHeightOfAtMost {
		// TODO?
	}

	freeSlotsAtHeight {
		// TODO?
	}
}
