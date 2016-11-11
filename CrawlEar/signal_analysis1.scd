(
fork {
	var folder = "test_audio".resolveRelative;
	s.bootSync(Condition());
	b = Buffer.read(s,folder+/+"drift.wav");
	c = Buffer.read(s,folder+/+"petals.wav");
	d = Buffer.read(s,folder+/+"bach_stereo_96.wav");
	e = Buffer.alloc(s,s.sampleRate.nextPowerOfTwo.nextPowerOfTwo,8);
	e.write(folder+/+"bufwrite_test.wav", "wave", "float", leaveOpen:true);
};
)

(
var buf = b;
{
	var sig = PlayBuf.ar(buf.numChannels, buf, BufRateScale.ir(buf),  startPos:48000*0, doneAction:2);
	var chain = FFT(LocalBuf(4096), sig[0]);
	// var unp = UnpackFFT(chain, 4096, 1, 128);
	// var res = Demand.kr(chain > 0, 0, unp).clump(2).collect(_.first);
	var div, normalized, peroct;
	// res = res.clump(2).collect({|el, i| el[0]});
	// res = res[0];
	// normalized = res.abs * Array.fill(res.size, {|i| (2.pow((i+1).log2.floor))}).postln;
	// div = normalized / max(normalized.sum, 0.00000001);
	Out.ar(0, sig);
	// peroct = res.clumps(Array.fill(div.size.log2.ceil, {|i| pow(2,i)})).collect(_.sum);
	[(SpecFlatness.kr(chain)*10).poll, (SpecCentroid.kr(chain).log2/10).poll, (SpecPcile.kr(chain, 0.5, 1).log2/10).poll, Amplitude.kr(sig,0.01,0.1)];
	DiskOut.ar()
	// sig;
}.scope;
)

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