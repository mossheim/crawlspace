// everything will run at 48k for sanity's sake
(
fork {
	var playbuf, recordbufs, synth, def, krbus;
	var maxdur = 60;
	// var thresh = 0.3387; // 2-sig for 25%-ile analysis func
	// var thresh = 0.3026; // 2-sig for 50%-ile analysis func
	// var thresh = 0.8059; // 3-sig for 50%-ile
	// var thresh = 0.5065; // 2.5-sig for 50%-ile
	var threshdur = 0.5;
	var offsetframes = 0.08*s.sampleRate;
	var path = "test_audio/analysis_test_writes/".resolveRelative;
	var audiofolder = "test_audio".resolveRelative;

	playbuf = Buffer.read(s,audiofolder+/+"drift.wav");
	recordbufs = Buffer.allocConsecutive(2, s, maxdur*s.sampleRate, 1);
	~recordbufs = recordbufs;
	krbus = Bus.control(s, 5);
	SynthDef(\cut_test_1, {
		var sig = PlayBuf.ar(2,playbuf,BufRateScale.ir(playbuf))[0];
		var delaysig = DelayN.ar(sig, offsetframes/SampleRate.ir, offsetframes/SampleRate.ir);
		var chain = FFT(LocalBuf(4096), BHiPass.ar(sig, 60));
		var ctrig = chain > 0;
		var anal = SpecPcile.kr(chain, 0.50, 1).log2;

		var localbuf = LocalBuf(3);
		var prevmeanbuf = LocalBuf(2);
		var bufindex = PulseCount.kr(ctrig);
		var read = #[0,0,0], write, mean, prevmean, deriv;
		var threshtrig, trigcount;
		var writephase;

		// store & get values out of buffer
		write = Dbufwr(anal, localbuf, bufindex);
		read = Demand.kr(ctrig, 0, Dbufrd(localbuf, [0,1,2]));
		Demand.kr(ctrig, 0, write);

		// calculate derivative
		mean = read.sum/3;
		prevmean = Demand.kr(ctrig, 0, Dbufrd(prevmeanbuf, bufindex+1));
		deriv = prevmean - mean;
		deriv = deriv.abs;
		Demand.kr(ctrig, 0, Dbufwr(mean, prevmeanbuf, bufindex));

		// trigger on threshold
		threshtrig = Trig1.kr((deriv >= thresh) * (Sweep.kr > 1), threshdur); // ignore garbage in first second
		trigcount = PulseCount.kr(threshtrig);
		writephase = Phasor.ar(K2A.ar(threshtrig), 1, 0, maxdur * SampleRate.ir);
		SendReply.kr(threshtrig, '/thresh_trig', [trigcount, writephase]);

		// output sigs for monitoring
		Out.kr(krbus, [anal, mean, prevmean, deriv, threshtrig]);
		Out.ar(0, sig.dup);
		SendReply.kr(ctrig, '/ctrig', [mean, threshtrig, trigcount]);
		RunningMax.kr(deriv, Impulse.kr(1)).poll(3); // for looking at threshold levels

		// write to buffer
		BufWr.ar(delaysig, recordbufs[0].bufnum+(trigcount % 2), writephase);

	}).add;

	//OSCdef(\cut_test_1_ctrig, _.postln, '/ctrig');
	OSCdef(\cut_test_1_thresh, {
		|msg|
		var index = msg[3];
		var numframes = msg[4];
		var whichbuf = (index + 1 % 2);
		msg.postln;
		recordbufs[whichbuf].write(path +/+ format("%.wav", index.asString.padLeft(3, "000")), headerFormat:"wav", sampleFormat:"float", numFrames:numframes, startFrame:0, completionMessage:{"write for segment % is done".format(index).postln});
	}, '/thresh_trig');
	s.sync(Condition());

	synth = Synth(\cut_test_1);
}
)

(
~max_seg_dur = 60;
~smooth_width = 3;
~num_analyses = 8;
~segment_trigger_osc_path = '/segment_trigger';
~segment_master_trigger_osc_path = '/segment_master_trigger';
~segment_info_osc_path = '/segment_info';

SynthDef(\n_samp_smoother, {
	arg nsamps, in_sig, in_trig, out;
	var ansig, trigsig, buf, count, write, read, mean;

	buf = LocalBuf(nsamps);
	ansig = In.kr(in_sig);
	trigsig = In.kr(in_trig);
	count = Stepper.kr(trigsig, 0, 0, nsamps-1);
	write = Dbufwr(ansig, buf, count);
	read = Demand.kr(trigsig, 0, Dbufrd(buf, [0,1,2]));
	Demand.kr(trigsig, 0, write);

	mean = read.sum/3;
	ReplaceOut.kr(out, mean);
}, nil, [~smooth_width]).add;

SynthDef(\deriv_calc, {
	arg in_sig, in_trig, out;
	var buf, mean, prevmean, deriv, trigsig, count;

	buf = LocalBuf(2);
	mean = In.kr(in_sig);
	trigsig = In.kr(in_trig);
	count = Stepper.kr(trigsig, 0, 0, 1);
	prevmean = Demand.kr(trigsig, 0, Dbufrd(buf, count+1));
	Demand.kr(trigsig, 0, Dbufwr(mean, buf, count));
	deriv = prevmean - mean;
	deriv = deriv.abs;
	ReplaceOut.kr(out, deriv);
}, nil).add;

SynthDef(\thresh_trig, {
	arg thresh, trigdur, initblockdur, in_sig, out;
	var deriv, thresh_trig, trig_count;

	deriv = In.kr(in_sig);
	deriv = deriv * (Sweep.kr > initblockdur);
	thresh_trig = Trig1.kr((deriv >= thresh), trigdur);
	// (thresh-deriv).poll(1);
	ReplaceOut.kr(out, thresh_trig);
}, nil).add;

SynthDef(\analysis, {
	arg offset_dur, in_sig, out_ansig, out_sig, out_ctrig;

	var sig = In.ar(in_sig);
	var chain = FFT(LocalBuf(4096), BHiPass.ar(sig, 60));
	var ctrig = chain > 0;
	var ansigs = [
		SpecPcile.kr(chain, 0.25, 1).log2,
		SpecPcile.kr(chain, 0.50, 1).log2,
		SpecPcile.kr(chain, 0.75, 1).log2,
		SpecPcile.kr(chain, 0.90, 1).log2,
		SpecFlatness.kr(chain),
		SpecCentroid.kr(chain).log2,
		Amplitude.kr(sig, 0.01, 0.1),
		Amplitude.kr(sig, 0.25, 0.3)
	];

	ReplaceOut.kr(out_ansig, ansigs);
	ReplaceOut.kr(out_ctrig, ctrig);
	ReplaceOut.ar(out_sig, DelayN.ar(sig, offset_dur, offset_dur));
}, nil).add;

SynthDef(\segmenter, {
	arg maxdur, trigdur, bufnum, in_sig, in_threshtrigs, trig_source_en = #[1,1,1,1,1,1,1,1];
	var trigsigs, phase, trig_master, trig_counts, trig_master_count, bufindex, delaysig;

	if(trig_source_en.size != ~num_analyses) {
		Error("segmenter synthdef: num analysis sigs is not equal to trig source enable size").throw;
	};

	trigsigs = In.kr(in_threshtrigs, ~num_analyses);
	trigsigs = trigsigs * trig_source_en;

	trig_master = Trig1.kr(trigsigs.sum, trigdur);
	trig_master_count = PulseCount.kr(trig_master);
	trig_counts = PulseCount.kr(trigsigs);
	phase = Phasor.ar(K2A.ar(trig_master), 1, 0, maxdur * SampleRate.ir)-1;

	// buffer writing
	delaysig = In.ar(in_sig);
	bufindex = trig_master_count % 2;
	BufWr.ar(delaysig, bufnum+bufindex, phase);

	// osc outputs
	~num_analyses.do {
		|i|
		SendReply.kr(trigsigs[i], ~segment_trigger_osc_path, [i, trig_counts[i], phase]);
	};
	SendReply.kr(trig_master, ~segment_master_trigger_osc_path, [phase]++trigsigs++trig_counts++[trig_master_count, 1-bufindex]);
	//SendReply.kr(Impulse.kr(1), ~segment_info_osc_path, trigsigs);
}, nil, [~max_seg_dur]).add;

SynthDef(\playbuf, {
	arg bufnum, out;
	var sig = PlayBuf.ar(2, bufnum, BufRateScale.ir(bufnum));
	ReplaceOut.ar(out, sig[0]);
}).add;

// )(
~thresholds = [
	[0.3387, 0.5089, 0.7383, 1.0627, 1.3348],
	[0.3026, 0.5065, 0.8059, 1.1154, 1.4638],
	[0.2755, 0.5576, 0.9134, 1.1951, 1.4450],
	[0.2604, 0.5157, 0.8798, 1.0468, 1.1994],
	[0.0259, 0.0703, 0.1428, 0.1794, 0.1956],
	[0.1884, 0.3558, 0.6258, 0.8133, 0.9553],
	[0.0320, 0.0551, 0.0792, 0.1111, 0.1573],
	[0.0170, 0.0280, 0.0426, 0.0626, 0.0876]
];
~measure_names = [\p25, \p50, \p75, \p90, \flatness, \centroid, \amp1, \amp2];
~sigma_index = 1;
~trigdur = 1.0;
~initblockdur = 1.0;
~write_path = "/Users/brianheim/Desktop/mu_test/analysis_test_writes/";
s.options.blockSize_(64);
fork {
	s.bootSync(Condition());

	// create buffers
	~playbuf.free;
	~recordbufs.do(_.free);

	~playbuf = Buffer.read(s,~read_path +/+ "stephen_4_pizz.wav");
	~recordbufs = Buffer.allocConsecutive(2, s, ~max_seg_dur * s.sampleRate, 1);

	// create buses
	~bus_anals.free;
	~bus_ctrig.free;
	~bus_signal.free;

	~bus_anals = Bus.control(s, ~num_analyses);
	~bus_ctrig = Bus.control(s, 1);
	~bus_signal = Bus.audio(s, 1);

	s.sync(Condition());

	Synth(\playbuf, [\bufnum, ~playbuf, \out, ~bus_signal], s, \t);
	Synth(\analysis, [\offset_dur, 0.1, \in_sig, ~bus_signal, \out_ansig, ~bus_anals, \out_sig, ~bus_signal, \out_ctrig, ~bus_ctrig], s, \t);

	~num_analyses.do {
		|i|
		var bindex = ~bus_anals.index+i;
		var thresh = ~thresholds[i][~sigma_index];
		Synth(\n_samp_smoother, [\in_sig, bindex, \in_trig, ~bus_ctrig, \out, bindex], s, \t);
		Synth(\deriv_calc, [\in_sig, bindex, \in_trig, ~bus_ctrig, \out, bindex], s, \t);
		Synth(\thresh_trig, [\thresh, thresh, \trigdur, ~trigdur, \initblockdur, ~initblockdur, \in_sig, bindex, \out, bindex], s, \t);
	};

	Synth(\segmenter, [\trigdur, ~trigdur, \in_threshtrigs, ~bus_anals, \in_sig, ~bus_signal, \bufnum, ~recordbufs[0].bufnum], s, \t);

	OSCdef(\any_trig_receiver, {
		|msg|
		"component triggered:\t%\t%  phase: %".format(~measure_names[msg[3]].asString.padLeft(8, "        "), msg[4].asString.padLeft(5, "     "), msg[5].asString.padLeft(7, "     ")).postln;
	}, ~segment_trigger_osc_path);
	OSCdef(\master_trig_receiver, {
		|msg|
		var segnum = msg[20];
		var bufindex = msg[21];
		var path = ~write_path +/+ "%.wav".format(segnum.asString.padLeft(4, "000"));
		var phase = msg[3];
		"master trig % received. phase: %\n\ttrig states: %\n\ttrig counts: %".format(segnum.asString.padLeft(4, "000"), phase, msg[4..11], msg[12..19]).postln;
		~recordbufs[bufindex].write(path, "WAV", "float", phase, 0, completionMessage: {"SEGMENT % WRITTEN".format(segnum).postln});
	}, ~segment_master_trigger_osc_path);
	OSCdef(\info_receiver, {
		|msg|
		msg.postln;
	}, ~segment_info_osc_path);
}
)










