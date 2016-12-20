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

	test_add {
		var sf = StairFunction.new(0);
		var firstPos = 1.0;
		var firstDir = -1.0;
		sf.add(firstPos, firstDir);

		this.assertEquals(sf.stepPositions, [firstPos]);
		this.assertEquals(sf.stepDirections, [firstDir]);
	}

	test_addError_duplicate {
		100.do {
			try {
				var sf = StairFunction.new(0);
				var value = 10000.rand;
				sf.add(value, 1);
				sf.add(value.asFloat, -1);
				this.failed(thisMethod, "Adding a step to an already-occupied position should result in an error.");
			} {
				|e|
				// do nothing
			}
		}
	}

	test_addError_directionZero {
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
			var directions = Array.fill(50, {[-1, 1].choose});
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
			sf.addAll([1,2,3], [-1, 1]);
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
			var directions = Array.fill(10, {[1, -1].choose});
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
		var step = [2, -1];
		sf.add(*step);
		this.assert(sf.remove(step[0]));
		this.assertEquals(sf.stepCount, 0);
	}

	test_removeFalse {
		var sf = StairFunction.new(0);
		var step = [2, -1];
		sf.add(*step);
		this.assert(sf.remove(1).not);
		this.assertEquals(sf.stepCount, 1);
	}

	test_removeAt {
		var sf = StairFunction.new(0);
		sf.addAll((0..100).scramble, 1!101);
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
			sf.addAll((0..100).scramble, 1!101);
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
			sf.addAll((0..100).scramble, 1!101);
			sf.removeAt(101);
			this.failed(thisMethod, "Index out of range should give an error.");
		} {
			|e|
			// do nothing
		}
	}

	test_clear {
		// TODO
	}

	test_heightAt {
		// TODO
	}

	test_stepAt {
		// TODO
	}
}

DiscreteStairFunction_Test : UnitTest {

}
