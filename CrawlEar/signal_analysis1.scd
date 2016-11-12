// this hunk of code analyzes the files in test_audio/input and places
// the result in test_audio/output. the analyses are exactly the same
// as those used in CrawlEar.sc to ensure conformance.

(
s.options.sampleRate_(Crawlspace.sr);
s.options.blockSize_(CrawlEar.blocksize);
fork {
	var dur, files, output_folder, input_folder, analyses;
	s.bootSync(Condition());
	output_folder = "test_audio/output".resolveRelative;
	input_folder = "test_audio/input".resolveRelative;

	files = PathName(input_folder).files;
	analyses = CrawlEar_Analysis.analyses;

	SynthDef(\analyze_buffer, {
		arg inbuf, outbuf;
		var sig = PlayBuf.ar(2, inbuf, BufRateScale.ir(inbuf), doneAction:2);
		var chain = FFT(LocalBuf(Crawlspace.fftsize), BHiPass.ar(sig, CrawlEar.hpf));
		var stats;

		stats = analyses.collect({
			|entry|
			var func = entry[2];
			SynthDef.wrap(func, entry[1].if(\kr, \ar), entry[1].if(chain, sig));
		});
		stats = K2A.ar(stats) ++ [sig[0]];
		DiskOut.ar(outbuf,stats);
	}).add;

	files.do {
		|filepath,i|
		var inbuf, outbuf, output_filename, id;

		inbuf = Buffer.read(s, filepath.fullPath);
		outbuf = Buffer.alloc(s,s.sampleRate.nextPowerOfTwo,analyses.size + 1);
		output_filename = output_folder +/+ filepath.fileNameWithoutExtension + "_analysis.wav";
		outbuf.write(output_filename, "wave", "float", leaveOpen:true);
		s.sync(Condition());
		dur = inbuf.duration;
		format("file % (%): % seconds", filepath.fileName, i, dur.round(0.01)).postln;
		id = Synth(\analyze_buffer, [\inbuf, inbuf.bufnum, \outbuf, outbuf.bufnum]).nodeID;

		OSCFunc({
			outbuf.close;
			outbuf.free;
			postln("Done: %".format(output_filename));
		}, path:'/n_end', argTemplate:[id]).oneShot;
	}
}
)
