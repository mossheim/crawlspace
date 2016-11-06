(
fork {
	s.bootSync(Condition());
	b = Buffer.read(s,"/Users/brianheim/Desktop/mu_test/drift.wav");
	c = Buffer.read(s,"/Users/brianheim/Desktop/mu_test/petals.wav");
	d = Buffer.read(s,"/Users/brianheim/Desktop/mu_test/bach_stereo_96.wav");
	e = Buffer.alloc(s,s.sampleRate.nextPowerOfTwo.nextPowerOfTwo,8);
	e.write("/Users/brianheim/Desktop/mu_test/bufwrite_test.wav", "wave", "float", leaveOpen:true);
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


(
s.options.sampleRate_(48000);
fork {
	var dur, files;
	s.bootSync(Condition());
	files = [
"/Users/brianheim/Desktop/mu_test/bach_stereo_96_normalized.wav",
"/Users/brianheim/Desktop/mu_test/drift.wav",
"/Users/brianheim/Desktop/mu_test/petals.wav",
"/Users/brianheim/Desktop/mu_test/stephen_1_nodes.wav",
"/Users/brianheim/Desktop/mu_test/stephen_2_drag.wav",
"/Users/brianheim/Desktop/mu_test/stephen_3_scratch.wav",
"/Users/brianheim/Desktop/mu_test/stephen_4_pizz.wav"
];
	files.do {
		|filename,i|
		var playbuf = Buffer.read(s, filename);
		s.sync(Condition());
		dur = playbuf.duration.postln;
		e = Buffer.alloc(s,s.sampleRate.nextPowerOfTwo.nextPowerOfTwo,9);
		e.write("/Users/brianheim/Desktop/mu_test/bufwrite_test_%.wav".format(i), "wave", "float", leaveOpen:true);
		s.sync(Condition());
		{
			var recordbuf = e;
			var sig = PlayBuf.ar(playbuf.numChannels, playbuf, BufRateScale.ir(playbuf), doneAction:2);
			var chain = FFT(LocalBuf(4096), BHiPass.ar(sig[0], 60));
			var flatness, centroid, ampl, ampl2;
			var pcile25, pcile50, pcile75, pcile90;
			var stats;
			Line.kr(0,1,dur,doneAction:2);

			flatness = SpecFlatness.kr(chain);
			centroid = SpecCentroid.kr(chain);
			pcile25 = SpecPcile.kr(chain, 0.25, 1);
			pcile50 = SpecPcile.kr(chain, 0.5, 1);
			pcile75 = SpecPcile.kr(chain, 0.75, 1);
			pcile90 = SpecPcile.kr(chain, 0.9, 1);
			ampl = Amplitude.kr(sig[0], 0.01, 0.1);
			ampl2 = Amplitude.kr(sig[0], 0.25, 0.3);

			stats = [pcile25, pcile50, pcile75, pcile90, flatness, centroid, ampl, ampl2];
			stats = K2A.ar(stats) ++ [sig[0]];
			DiskOut.ar(recordbuf,stats);
			Silent.ar(2);
		}.play;
		(dur+2).wait;
		e.close;
		e.free;

	}
}
)