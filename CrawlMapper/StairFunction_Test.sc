StairFunction_Test : UnitTest {
	test_init {
		var startValue = 0;
		var sf = StairFunction.new();

		this.assertEquals(sf.startValue, startValue);
		this.assertEquals(sf.stepPositions, []);
		this.assertEquals(sf.stepDirections, []);
	}

	test_init2 {
		var startValue = -5;
		var sf = StairFunction.new(-5);

		this.assertEquals(sf.startValue, startValue);
		this.assertEquals(sf.stepPositions, []);
		this.assertEquals(sf.stepDirections, []);
	}

	test_directions {
		var d1 = StairFunction.directions();
		var d2 = StairFunction.directions();

		this.assert(d1 == [\up, \down], "directions array must be correct");
		this.assert(d1 == d2, "directions arrays must be equal");
		this.assert(d1 === d2, "directions arrays must be identical");
	}

	test_add {
		var sf = StairFunction.new(0);
		var firstPos = 1.0;
		var firstDir = \down;
		sf.add(firstPos, firstDir);

		this.assertEquals(sf.stepPositions, [firstPos]);
		this.assertEquals(sf.stepDirections, [firstDir]);
	}

	test_addError_duplicate {
		100.do {
			try {
				var sf = StairFunction.new(0);
				var value = 10000.rand;
				sf.add(value, \up);
				sf.add(value.asFloat, \down);
				this.failed(thisMethod, "Adding a step to an already-occupied position should result in an error.");
			} {
				|e|
				// do nothing
			}
		}
	}

	test_addError_badDirection {
		try {
			var sf = StairFunction.new(0);
			sf.add(3, \sideways);
			this.failed(thisMethod, "Using unequal position & direction array sizes should result in an error.");
		} {
			|e|
			// do nothing
		};

		try {
			var sf = StairFunction.new(0);
			sf.add(3, 0);
			this.failed(thisMethod, "Using unequal position & direction array sizes should result in an error.");
		} {
			|e|
			// do nothing
		}
	}

	test_addAll {
		100.do {
			var sf = StairFunction.new(0);
			var positions = Array.series(50, 0, 0.02).scramble;
			var directions = Array.fill(50, {[\down, \up].choose});
			var order = positions.order;
			sf.addAll(positions, directions);

			this.assertEquals(sf.stepPositions, positions[order]);
			this.assertEquals(sf.stepDirections, directions[order]);
			this.assertEquals(sf.stepCount, 50);
		}
	}

	test_addAllError_badSizes {
		try {
			var sf = StairFunction.new(0);
			sf.addAll([1,2,3], [\down, \up]);
			this.failed(thisMethod, "Using unequal position & direction array sizes should result in an error.");
		} {
			|e|
			// do nothing
		}
	}

	test_addAllError_overlappedSets {
		try {
			var sf = StairFunction.new(0);
			var positions = Array.fill(10, {1000.rand});
			var directions = Array.fill(10, {[\up, \down].choose});
			sf.addAll(positions, directions);
			sf.addAll(positions.collect(_.asFloat), directions);
			this.failed(thisMethod, "Using overlapping sets should result in an error.");
		} {
			|e|
			// do nothing
		}
	}

	test_addAllError_badDirection {
		try {
			var sf = StairFunction.new(0);
			var positions = Array.fill(10, {1000.rand});
			var directions = Array.fill(10, {[\up, \down].choose});
			directions[5] = \sideways;
			sf.addAll(positions, directions);
			sf.addAll(positions.collect(_.asFloat), directions);
			this.failed(thisMethod, "Using overlapping sets should result in an error.");
		} {
			|e|
			// do nothing
		}
	}

	test_removeTrue {
		var sf = StairFunction.new(0);
		var step = [2, \down];
		sf.add(*step);
		this.assert(sf.remove(step[0]));
		this.assertEquals(sf.stepCount, 0);
	}

	test_removeFalse {
		var sf = StairFunction.new(0);
		var step = [2, \down];
		sf.add(*step);
		this.assert(sf.remove(1).not);
		this.assertEquals(sf.stepCount, 1);
	}

	test_removeAt {
		var sf = StairFunction.new(0);
		sf.addAll((0..100).scramble, 'up'!101);
		forBy(100, 0, -1) {
			|i|
			sf.removeAt(i);
		};
		this.assertEquals(sf.stepCount, 0);
		this.assert(sf.stepPositions.isEmpty);
		this.assert(sf.stepDirections.isEmpty);
	}

	test_removeAtError_negativeIndex {
		try {
			var sf = StairFunction.new(0);
			sf.addAll((0..100).scramble, 'up'!101);
			sf.removeAt(-1);
			this.failed(thisMethod, "Index out of range should give an error.");
		} {
			|e|
			// do nothing
		}
	}

	test_removeAtError_overflowIndex {
		try {
			var sf = StairFunction.new(0);
			sf.addAll((0..100).scramble, 'up'!101);
			sf.removeAt(101);
			this.failed(thisMethod, "Index out of range should give an error.");
		} {
			|e|
			// do nothing
		}
	}

	test_clear {
		var sf = StairFunction.new(200);
		sf.addAll((0..100).scramble, 'up'!101);
		sf.clear;
		this.assert(sf.stepCount == 0);
		this.assert(sf.stepPositions == []);
		this.assert(sf.stepDirections == []);
		this.assert(sf.startValue == 200);
	}

	test_heightAt_1 {
		var sf = StairFunction.new(0);
		sf.add(0, 'up');

		this.assertEquals(sf.heightAt(-1), 0, "height at -1 shold be 0");
		this.assertEquals(sf.heightAt(0), 1, "height at 0 shold be 1");
		// remember, StairFunction is right-continuous
		this.assertEquals(sf.heightAt(1), 1, "height at 1 should be 1");
	}

	test_heightAt_2 {
		var sf = StairFunction.new(0);
		var positions = (0..999);
		var directions = Array.fill(1000, {|i| ['up', 'down']@@i});
		sf.addAll(positions, directions);

		1000.do {
			|x|
			// test on step endpoints. remember, StairFunction is right-continuous
			this.assertEquals(sf.heightAt(x), [1, 0]@@x, report:false);
			// test mid-step
			this.assertEquals(sf.heightAt(x+0.5), [1, 0]@@x, report:false);
		}
	}

	test_heightAt_3 {
		var sf = StairFunction.new(50);
		var positions = (0..999);
		var directions = 'up'!1000;
		sf.addAll(positions, directions);

		1000.do {
			|x|
			// test on step endpoints. remember, StairFunction is right-continuous
			this.assertEquals(sf.heightAt(x), x+51, report:false);
			// test mid-step
			this.assertEquals(sf.heightAt(x+0.5), x+51, report:false);
		}
	}

	test_stepAt {
		var sf = StairFunction.new(200);
		var order = (0..100).scramble;
		sf.addAll((0..100)[order], 'up'!101);
		100.do {
			|x|
			this.assertEquals(sf.stepAt(x), [x, 'up']);
		};
	}

	test_stepAtError_negativeIndex {
		try {
			var sf = StairFunction.new(0);
			sf.addAll((0..100).scramble, 'up'!101);
			sf.stepAt(-1);
			this.failed(thisMethod, "Index out of range should give an error.");
		} {
			|e|
			// do nothing
		}
	}

	test_stepAtError_overflowIndex {
		try {
			var sf = StairFunction.new(0);
			sf.addAll((0..100).scramble, 'up'!101);
			sf.stepAt(101);
			this.failed(thisMethod, "Index out of range should give an error.");
		} {
			|e|
			// do nothing
		}
	}

	test_maxHeight {
		[-10, 0, 10].do {
			|startValue|

			var sf = StairFunction(startValue);
			sf.addAll((0..100).scramble, 'up'!101);
			this.assertEquals(sf.maxHeight, 101 + startValue);

			sf = StairFunction(startValue);
			sf.addAll((0..49), ['up', 'down'].wrapExtend(50));
			this.assertEquals(sf.maxHeight, 1 + startValue);

			sf = StairFunction(startValue);
			sf.addAll((0..9), [\up, \down, \up, \up, \down, \up, \up, \up, \down, \down]);
			// 1 0 1 2 1 2 3 4 3 2
			this.assertEquals(sf.maxHeight, 4 + startValue);

			sf = StairFunction(startValue);
			sf.addAll((0..9), [\down, \down, \up, \up, \down, \down, \down, \up, \up, \down]);
			// -1 -2 -1 0 -1 -2 -3 -2 -1 -2
			this.assertEquals(sf.maxHeight, startValue);
		}
	}

	test_minHeight {
		[-10, 0, 10].do {
			|startValue|

			var sf = StairFunction(startValue);
			sf.addAll((0..100).scramble, 'down'!101);
			this.assertEquals(sf.minHeight, -101 + startValue);

			sf = StairFunction(startValue);
			sf.addAll((0..49), ['down', 'up'].wrapExtend(50));
			this.assertEquals(sf.minHeight, -1 + startValue);

			sf = StairFunction(startValue);
			sf.addAll((0..9), [\up, \down, \up, \up, \down, \up, \up, \up, \down, \down]);
			// 1 0 1 2 1 2 3 4 3 2
			this.assertEquals(sf.minHeight, startValue);

			sf = StairFunction(startValue);
			sf.addAll((0..9), [\down, \down, \up, \up, \down, \down, \down, \up, \up, \down]);
			// -1 -2 -1 0 -1 -2 -3 -2 -1 -2
			this.assertEquals(sf.minHeight, -3 + startValue);
		}
	}

	test_finalHeight {
		[-10, 0, 10].do {
			|startValue|

			var sf = StairFunction(startValue);
			sf.addAll((0..100).scramble, 'down'!101);
			this.assertEquals(sf.finalHeight, -101 + startValue);

			sf = StairFunction(startValue);
			sf.addAll((0..49), ['down', 'up'].wrapExtend(50));
			this.assertEquals(sf.finalHeight, startValue);

			sf = StairFunction(startValue);
			sf.addAll((0..9), [\up, \down, \up, \up, \down, \up, \up, \up, \down, \down]);
			// 1 0 1 2 1 2 3 4 3 2
			this.assertEquals(sf.finalHeight, 2 + startValue);

			sf = StairFunction(startValue);
			sf.addAll((0..9), [\down, \down, \up, \up, \down, \down, \down, \up, \up, \down]);
			// -1 -2 -1 0 -1 -2 -3 -2 -1 -2
			this.assertEquals(sf.finalHeight, -2 + startValue);
		}
	}

}

DiscreteStairFunction_Test : UnitTest {

}
