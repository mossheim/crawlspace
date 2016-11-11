// this hunk of code analyzes the files in test_audio/input and places
// the result in test_audio/output. the analyses are exactly the same
// as those used in CrawlEar.sc to ensure conformance.

(
s.options.sampleRate_(CrawlEar.sr);
s.options.blockSize_(CrawlEar.blocksize);
fork {
	var dur, files, output_folder, input_folder, analyses;
	s.bootSync(Condition());
	output_folder = "test_audio/output".resolveRelative;
	input_folder = "test_audio/input".resolveRelative;

	files = PathName(input_folder).files;
	analyses = CrawlEar.analyses;

	SynthDef(\analyze_buffer, {
			arg inbuf, outbuf;
			var sig = PlayBuf.ar(inbuf.numChannels, inbuf, BufRateScale.ir(inbuf), doneAction:2);
			var chain = FFT(LocalBuf(CrawlEar.fftsize), BHiPass.ar(sig.first, CrawlEar.hpf));
			var stats;

			stats = analyses.collect({
				|entry|
				var func = entry[2];
				SynthDef.wrap(func, entry[1].if(\kr, \ar), entry[1].if(chain, sig.first));
			});
			stats = K2A.ar(stats) ++ [sig[0]];
			DiskOut.ar(outbuf,stats);
	}).add;

	files.do {
		|filename,i|
		var inbuf, outbuf, output_filename;
		dur = inbuf.duration.postln;
		inbuf = Buffer.read(s, filename),
		outbuf = Buffer.alloc(s,s.sampleRate.nextPowerOfTwo,analyses.size + 1);
		output_filename = output_folder +/+ PathName(filename).fileNameWithoutExtension + "_analysis.wav";
		outbuf.write(output_filename, "wave", "float", leaveOpen:true);
		s.sync(Condition());
		Synth(\analyze_buffer, [\inbuf, inbuf, \outbuf, outbuf]);

		(dur+2).wait;
		outbuf.close;
		outbuf.free;
	}
}
)