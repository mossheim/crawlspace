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
		}
	}

	test_addAllError_badSizes {
		// TODO
	}

	test_addAllError_overlappedSets {
		// TODO
	}

	test_removeTrue {
		// TODO
	}

	test_removeFalse {
		// TODO
	}

	test_removeAt {
		// TODO
	}

	test_removeAtError {
		// TODO
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
