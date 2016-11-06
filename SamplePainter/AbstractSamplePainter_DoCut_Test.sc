AbstractSamplePainter_DoCut_Test : UnitTest {
	*pattern {
		^FloatArray[0, 0.5, 1, -1, -0.5, 0];
	}

	*pattern2 {
		^FloatArray[0.125, 0.25, 0.75, -0.125, -0.25, -0.75];
	}

	*precision_mono {
		^this.testDir +/+ "precision_test_mono.wav";
	}

	*precision_stereo {
		^this.testDir +/+ "precision_test_stereo.wav";
	}

	*precision_quad {
		^this.testDir +/+ "precision_test_quad.wav";
	}

	*isTestPatternMatch {
		arg pat;
		^pat == this.pattern;
	}

	*isTestPattern2Match {
		arg pat;
		^pat == this.pattern2;
	}

	testFile {
		^this.testDir +/+ "test.wav";
	}

	testDir {
		^"sp_test/".resolveRelative;
	}

	path_2ch96 {^this.testDir +/+ "bach_stereo_96.wav"}
	path_2ch44 {^this.testDir +/+ "beet_stereo_44.wav"}
	path_1ch96 {^this.testDir +/+ "bach_mono_96.wav"}
	path_1ch44 {^this.testDir +/+ "beet_mono_44.wav"}

	setUp {
		if(File.exists(this.testFile)) {File.delete(this.testFile)};
		AbstractSamplePainter.nChannels_(2);
		AbstractSamplePainter.sr_(44100);
	}

	tearDown {
		if(File.exists(this.testFile)) {File.delete(this.testFile)};
		AbstractSamplePainter.nChannels_(2);
		AbstractSamplePainter.sr_(44100);
	}

	createSP {
		var sp = AbstractSamplePainter(this.testFile, 10000, [], [], []);
		^sp;
	}

	makeCuts {
		arg iregs, lregs, file;
		var sp = this.createSP();
		var datas = [];
		var sf = SoundFile(file);

		iregs.do {
			|ireg, i|
			var lreg;
			lreg = lregs[i];
			sf.openRead();
			datas = datas add: sp.doCut(sf, ireg, lreg);
			sf.close;
		};

		^datas;
	}

	test_cutPrecision_mono {
		var iregs = [100, 300, 8189, 16381, 16394]; // region indices
		var lregs = [100, 6, 6, 6, 6]; // region lengths
		var datas;
		AbstractSamplePainter.nChannels_(1);
		datas = this.makeCuts(iregs, lregs, this.class.precision_mono);

		// test regions
		this.assert(this.isAllZero(datas[0]), "Region 1");
		this.assert(this.class.isTestPatternMatch(datas[1][0]), "Region 2");
		this.assert(this.class.isTestPatternMatch(datas[2][0]), "Region 3");
		this.assert(this.class.isTestPatternMatch(datas[3][0]), "Region 4");
		this.assert(this.class.isTestPatternMatch(datas[4][0]), "Region 5");
	}

	test_cutPrecision_stereo {
		var iregs = [100, 300, 500, 700, 806, 4994];
		var lregs = [100, 100, 100, 6, 6, 6];
		var datas;
		AbstractSamplePainter.nChannels_(2);
		datas = this.makeCuts(iregs, lregs, this.class.precision_stereo);

		// test regions
		this.assert(this.isAllZero(datas[0]), "Region 1");
		this.assert(this.isChZero(datas[1], 0), "Region 2");
		this.assert(this.isChZero(datas[2], 1), "Region 3");
		this.assert(this.class.isTestPatternMatch(datas[3][0]), "Region 4a");
		this.assert(this.class.isTestPatternMatch(datas[3][1]), "Region 4b");
		this.assert(this.class.isTestPatternMatch(datas[4][0]), "Region 5a");
		this.assert(this.class.isTestPattern2Match(datas[4][1]), "Region 5b");
		this.assert(this.class.isTestPatternMatch(datas[5][0]), "Region 6a");
		this.assert(this.class.isTestPatternMatch(datas[5][1]), "Region 6b");
	}

	test_cutPrecision_quad {
		var iregs = [100, 300, 500, 700, 900, 1100, 1206, 2994];
		var lregs = [100, 100, 100, 100, 100, 6, 6, 6];
		var datas;
		AbstractSamplePainter.nChannels_(4);
		datas = this.makeCuts(iregs, lregs, this.class.precision_quad);

		// test regions
		this.assert(this.isAllZero(datas[0]), "Region 1");
		this.assert(this.isChZero(datas[1], 0), "Region 2");
		this.assert(this.isChZero(datas[2], 1), "Region 3");
		this.assert(this.isChZero(datas[3], 2), "Region 4");
		this.assert(this.isChZero(datas[4], 3), "Region 5");

		this.assert(this.class.isTestPatternMatch(datas[5][0]), "Region 6a");
		this.assert(this.class.isTestPatternMatch(datas[5][1]), "Region 6b");
		this.assert(this.class.isTestPatternMatch(datas[5][2]), "Region 6c");
		this.assert(this.class.isTestPatternMatch(datas[5][3]), "Region 6d");

		this.assert(this.class.isTestPatternMatch(datas[6][0]), "Region 7a");
		this.assert(this.class.isTestPattern2Match(datas[6][1]), "Region 7b");
		this.assert(this.class.isTestPatternMatch(datas[6][2].reverse), "Region 7c");
		this.assert(this.class.isTestPattern2Match(datas[6][3].reverse), "Region 7d");

		this.assert(this.class.isTestPatternMatch(datas[7][0]), "Region 8a");
		this.assert(this.class.isTestPatternMatch(datas[7][1]), "Region 8b");
		this.assert(this.class.isTestPatternMatch(datas[7][2]), "Region 8c");
		this.assert(this.class.isTestPatternMatch(datas[7][3]), "Region 8d");
	}

	// all 0?
	isAllZero {
		arg data;
		^data.collect({|a| a.abs.sum}).sum == 0;
	}

	// check that one channel is 0 and the rest are 1
	isChZero {
		arg data, ch;
		var flag = true;
		data.do {
			|chdata, i|
			if(ch == i) {
				flag = flag && (chdata.abs.sum == 0);
			} {
				flag = flag && (chdata.sum == chdata.size);
			}
		};

		^flag;
	}

















}