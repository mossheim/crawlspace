AbstractSamplePainter_Test : UnitTest {
	testPath {
		^"/Users/brianheim/Desktop/mu_test/test.wav";
	}

	testFolder {
		^"/Users/brianheim/Desktop/mu_test/";
	}

	path_2ch96 {^"/Users/brianheim/Desktop/mu_test/bach_stereo_96.wav"}
	path_2ch44 {^"/Users/brianheim/Desktop/mu_test/beet_stereo_44.wav"}
	path_1ch96 {^"/Users/brianheim/Desktop/mu_test/bach_mono_96.wav"}
	path_1ch44 {^"/Users/brianheim/Desktop/mu_test/beet_mono_44.wav"}

	setUp {
		if(File.exists(this.testPath)) {File.delete(this.testPath)};
		AbstractSamplePainter.nChannels_(2);
		AbstractSamplePainter.sr_(44100);
	}

	tearDown {
		if(File.exists(this.testPath)) {File.delete(this.testPath)};
		AbstractSamplePainter.nChannels_(2);
		AbstractSamplePainter.sr_(44100);
	}

	test_checkBasicConstructorFunctionality1000Frames {
		// test basic output of the constructor.
		// first with 1000 frames, then with 50000 frames
		var outpath = this.testPath();
		var nframes = 1000;
		var mu = AbstractSamplePainter(outpath, nframes, [this.path_2ch44 -> 1], [], []);
		var sf, arr, runningtot = 0;

		// test constructor assignments
		this.assertEquals(mu.outFilename, outpath, "MU filename should match");
		this.assert(mu.outFrames == 1000, "MU frames should match");
		this.assertEquals(mu.sourceFileList, [this.path_2ch44 -> 1], "MU source file name should match");
		this.assert(mu.modifyFuncList == [], "MU modify func list should match");
		this.assert(mu.pasteFuncList == [], "MU paste func list should match");

		// test that the created file is the correct number of channels and frames.
		sf = SoundFile.openRead(outpath);
		arr = FloatArray.newClear(100);
		runningtot = 0;
		this.assertEquals(sf.numChannels, 2, "Output channels should be 2");
		this.assertEquals(sf.numFrames(), nframes, "Output frames should be %".format(nframes));
		this.assertEquals(sf.sampleRate(), 44100, "Output sample rate should be 44100");
		while {arr.size > 0} {
			sf.readData(arr);
			if(arr.size > 0) {
				runningtot = arr.abs.maxItem;
			}
		};
		this.assertEquals(runningtot, 0, "Output must be completely silent");
		sf.close;
	}

	test_checkBasicConstructorFunctionality50000Frames {
		// test basic output of the constructor.
		// first with 1000 frames, then with 50000 frames
		var outpath = this.testPath();
		var nframes = 50000;
		var mu = AbstractSamplePainter(outpath, nframes, [this.path_2ch44 -> 1], [], []);
		var sf, arr, runningtot;

		// test constructor assignments
		this.assertEquals(mu.outFilename, outpath, "MU filename should match");
		this.assert(mu.outFrames == 50000, "MU frames should match");
		this.assertEquals(mu.sourceFileList, [this.path_2ch44 -> 1], "MU source file name should match");
		this.assert(mu.modifyFuncList == [], "MU modify func list should match");
		this.assert(mu.pasteFuncList == [], "MU paste func list should match");

		// test that the created file is the correct number of channels and frames.
		sf = SoundFile.openRead(outpath);
		arr = FloatArray.newClear(100);
		runningtot = 0;
		this.assertEquals(sf.numChannels, 2, "Output channels should be 2");
		this.assertEquals(sf.numFrames(), nframes, "Output frames should be %".format(nframes));
		this.assertEquals(sf.sampleRate(), 44100, "Output sample rate should be 44100");
		while {arr.size > 0} {
			sf.readData(arr);
			if(arr.size > 0) {
				runningtot = runningtot + arr.abs.maxItem;
			}
		};
		this.assertEquals(runningtot, 0, "Output must be completely silent");
		sf.close;
	}

	test_checkConstructorFileConstruction_variousParameters {
		var nframes = 50000;

		var srs = [44100, 48000, 96000];
		var chs = [1, 2, 4, 8];

		srs.do {
			|sr|
			chs.do {
				|ch|
				var mu, sf, rtot = 0, arr = FloatArray.newClear(100);
				// create mu
				this.assert(File.exists(this.testPath).not);
				AbstractSamplePainter.nChannels_(ch);
				AbstractSamplePainter.sr_(sr);
				mu = AbstractSamplePainter(this.testPath, nframes, [this.path_2ch44 -> 1], [], []);

				// new sound file
				sf = SoundFile.openRead(this.testPath);
				this.assertEquals(sf.numChannels, ch);
				this.assertEquals(sf.numFrames, nframes);
				this.assertEquals(sf.sampleRate, sr);
				while {arr.size > 0} {
					sf.readData(arr);
					if(arr.size > 0) {
						rtot = rtot + arr.abs.maxItem;
					}
				};
				this.assertEquals(rtot, 0, "Output must be silent: % %".format(ch, sr));
				sf.close;
				File.delete(this.testPath);
				this.assert(File.exists(this.testPath).not, "delete worked");
			}
		}

	}

	test_doCutStereoReturnArrayHasCorrectSize {
		var mu = AbstractSamplePainter(this.testPath, 10000, [], [], []);
		var sf = SoundFile.openRead(this.path_2ch44);
		var data = mu.doCut(sf, 0, 1000);
		this.assertEquals(data.size, 2, "data size = channels");
		data.do {|chdata,i| this.assertEquals(chdata.size, 1000, "data ch size = cut frames")};
		data = mu.doCut(sf, 1000, 3000);
		this.assertEquals(data.size, 2, "data size = channels");
		data.do {|chdata,i| this.assertEquals(chdata.size, 3000, "data ch size = cut frames")};
		data = mu.doCut(sf, sf.numFrames - 1000, 1000);
		this.assertEquals(data.size, 2, "data size = channels");
		data.do {|chdata,i| this.assertEquals(chdata.size, 1000, "data ch size = cut frames")};
	}

	test_doCutMonoReturnArrayHasCorrectSize {
		var mu, sf, data;
		AbstractSamplePainter.nChannels_(1);
		mu = AbstractSamplePainter(this.testPath, 10000, [], [], []);
		sf = SoundFile.openRead(this.path_1ch44);
		data = mu.doCut(sf, 0, 1000);
		this.assertEquals(data.size, 1, "data size = channels");
		data.do {|chdata,i| this.assertEquals(chdata.size, 1000, "data ch size = cut frames")};
		data = mu.doCut(sf, 1000, 3000);
		this.assertEquals(data.size, 1, "data size = channels");
		data.do {|chdata,i| this.assertEquals(chdata.size, 3000, "data ch size = cut frames")};
		data = mu.doCut(sf, sf.numFrames - 1000, 1000);
		this.assertEquals(data.size, 1, "data size = channels");
		data.do {|chdata,i| this.assertEquals(chdata.size, 1000, "data ch size = cut frames")};
	}

}