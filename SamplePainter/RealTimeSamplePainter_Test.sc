RealTimeSamplePainter_Test : UnitTest {
	setUp {
		Server.local.bootSync(Condition());
		SimpleSamplePainter.nChannels_(2);
		SimpleSamplePainter.sr_(44100);
	}

	tearDown {
		SimpleSamplePainter.nChannels_(2);
		SimpleSamplePainter.sr_(44100);
	}

	generateNoise {
		arg channels, frames, max = 1.0;
		^Array.fill(channels, {FloatArray.rand2(frames, max)});
	}

	test_modifyWrap_mono {
		var data = this.generateNoise(1, 44100*3, 1.0);
		var func = {|sig| sig};
		var processed = RealTimeSamplePainter.modifyWrap(data, func, 0.0);

		this.assert(data == processed, "values +/-1.0");

		data = this.generateNoise(1, 44100*3, 2.0);
		processed = RealTimeSamplePainter.modifyWrap(data, func, 0.0);

		this.assert(data == processed, "values +/-2.0");

		data = this.generateNoise(1, 44100, 1.0);
		processed = RealTimeSamplePainter.modifyWrap(data, func, 1.0);

		processed.do {
			|subarr, i|
			subarr.do {
				|subel, j|
				if(j < data[i].size) {
					subarr[j] = subarr[j] - data[i][j];
				}
			}
		};

		this.assert(processed.every(_.every(_==0)), "first half matches data exactly");
		this.assert(processed.collect(_.size).every(_==88200), "correct size for 2 seconds");
	}

	test_modifyWrap_stereo {
		var data = this.generateNoise(2, 44100*3, 1.0);
		var func = {|sig| sig};
		var processed = RealTimeSamplePainter.modifyWrap(data, func, 0);

		this.assert(data == processed, "values +/-1.0");

		data = this.generateNoise(2, 44100*3, 2.0);
		processed = RealTimeSamplePainter.modifyWrap(data, func, 0);

		this.assert(data == processed, "values +/-2.0");

		data = this.generateNoise(2, 44100, 1.0);
		processed = RealTimeSamplePainter.modifyWrap(data, func, 1);

		processed.do {
			|subarr, i|
			subarr.do {
				|subel, j|
				if(j < data[i].size) {
					subarr[j] = subarr[j] - data[i][j];
				}
			}
		};

		this.assert(processed.every(_.every(_==0)), "first half matches data exactly");
		this.assert(processed.collect(_.size).every(_==88200), "correct size for 2 seconds");
	}

	test_modifyWrap_quad {
		var data = this.generateNoise(4, 44100*3, 1.0);
		var func = {|sig| sig};
		var processed = RealTimeSamplePainter.modifyWrap(data, func, 0);

		this.assert(data == processed, "values +/-1.0");

		data = this.generateNoise(4, 44100*3, 2.0);
		processed = RealTimeSamplePainter.modifyWrap(data, func, 0);

		this.assert(data == processed, "values +/-2.0");

		data = this.generateNoise(4, 44100, 1.0);
		processed = RealTimeSamplePainter.modifyWrap(data, func, 1);

		processed.do {
			|subarr, i|
			subarr.do {
				|subel, j|
				if(j < data[i].size) {
					subarr[j] = subarr[j] - data[i][j];
				}
			}
		};

		this.assert(processed.collect(_.every(_==0)).every(_==true), "first half matches data exactly");
		this.assert(processed.collect(_.size).every(_==88200), "correct size for 2 seconds");
	}

	test_modifyWrap_octo {
		var data = this.generateNoise(8, 44100*3, 1.0);
		var func = {|sig| sig};
		var processed = RealTimeSamplePainter.modifyWrap(data, func, 0);

		this.assert(data == processed, "values +/-1.0");

		data = this.generateNoise(8, 44100*3, 2.0);
		processed = RealTimeSamplePainter.modifyWrap(data, func, 0);

		this.assert(data == processed, "values +/-2.0");

		data = this.generateNoise(8, 44100, 1.0);
		processed = RealTimeSamplePainter.modifyWrap(data, func, 1);

		processed.do {
			|subarr, i|
			subarr.do {
				|subel, j|
				if(j < data[i].size) {
					subarr[j] = subarr[j] - data[i][j];
				}
			}
		};

		this.assert(processed.collect(_.every(_==0)).every(_==true), "first half matches data exactly");
		this.assert(processed.collect(_.size).every(_==88200), "correct size for 2 seconds");
	}

	test_modifyWrap_durations {
		var data;
		var func = {|sig| sig};
		var processed;

		try {
			data = RealTimeSamplePainter.modifyWrap(this.generateNoise(2, 0, 1.0), func, 0);
		} {
			arg error;
			this.assert(error.isKindOf(InvalidInputError), "data size of 0 frames should throw error", false);
		};
		for(0, 16) {
			|i|
			data = RealTimeSamplePainter.modifyWrap(this.generateNoise(2, 2.pow(i), 1.0), func, 0);
			this.assert(data.size == 2, report:false);
			this.assert(data[0].size == 2.pow(i), "Output sizes match: %".format(2.pow(i)));
		}
	}
}

