StairFunction_Test : UnitTest {
	test_init {
		var startValue = 0;
		var sf = StairFunction.new();

		this.assertEquals(sf.startValue, startValue, report:false);
		this.assertEquals(sf.stepPositions, [], report:false);
		this.assertEquals(sf.stepDirections, [], report:false);
	}

	test_init2 {
		var startValue = -5;
		var sf = StairFunction.new(-5);

		this.assertEquals(sf.startValue, startValue, report:false);
		this.assertEquals(sf.stepPositions, [], report:false);
		this.assertEquals(sf.stepDirections, [], report:false);
	}
}

DiscreteStairFunction_Test : UnitTest {

}
