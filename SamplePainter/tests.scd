(
~countdown = {
	arg time;
	post("paused for next test: ");
	forBy(time, 0, -1, {
		|i|
		post("% ".format(i));
		1.wait;
	});
	postln("");
};

Routine.run {
	var time = 3;
  AbstractSamplePainter_Test.new.run;
	~countdown.(time);
	AbstractSamplePainter_DoCut_Test.new.run;
	~countdown.(time);
	SimpleSamplePainter_Test.new.run;
	~countdown.(time);
	RealTimeSamplePainter_Test.new.run;
}
)
