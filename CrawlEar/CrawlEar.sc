CrawlEar {
	classvar <smoother_width = 3;
	classvar <segment_trigger_osc_path = '/segment_trigger';
	classvar <segment_master_trigger_osc_path = '/segment_master_trigger';
	classvar <segment_info_osc_path = '/segment_info';
	classvar <fftsize = 2048;
	classvar <sr = 96000;
	classvar <blocksize = 256;
	classvar <max_seg_dur = 60.0;

	var <server;
	var <writepath;
	var <nch;
	var analyses;

	*new {
		arg server, writepath, nch;
		^super.newCopyArgs(server, writepath, nch).pr_init;
	}

	pr_init {
		fork {
			server = Server.local;
			server.options.blockSize_(64); // use block size of 64
			server.bootSync(Condition());

			analyses = [
				[\p25, {arg chain; SpecPcile.kr(chain, 0.25, 1).log2}, [0.3387, 0.5089, 0.7383, 1.0627, 1.3348]],
				[\p50, {arg chain; SpecPcile.kr(chain, 0.50, 1).log2}, [0.3026, 0.5065, 0.8059, 1.1154, 1.4638]],
				[\p75, {arg chain; SpecPcile.kr(chain, 0.75, 1).log2}, [0.2755, 0.5576, 0.9134, 1.1951, 1.4450]],
				[\p90, {arg chain; SpecPcile.kr(chain, 0.90, 1).log2}, [0.2604, 0.5157, 0.8798, 1.0468, 1.1994]],
				[\flat, {arg chain; SpecFlatness.kr(chain)}, [0.0259, 0.0703, 0.1428, 0.1794, 0.1956]],
				[\cent, {arg chain; SpecCentroid.kr(chain).log2}, [0.1884, 0.3558, 0.6258, 0.8133, 0.9553]],
				[\amp1, {arg chain; Amplitude.kr(sig, 0.01, 0.1)}, [0.0320, 0.0551, 0.0792, 0.1111, 0.1573]],
				[\amp2, {arg chain; Amplitude.kr(sig, 0.25, 0.3)}, [0.0170, 0.0280, 0.0426, 0.0626, 0.0876]]
			];


			this.registerSynthDefs();
		}
	}

	registerSynthDefs {
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
		}, nil, [this.class.smoother_width]).add;

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

			if(trig_source_en.size != analyses.size) {
				Error("segmenter synthdef: num analysis sigs is not equal to trig source enable size").throw;
			};

			trigsigs = In.kr(in_threshtrigs, analyses.size);
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
			analyses.size.do {
				|i|
				SendReply.kr(trigsigs[i], segment_trigger_osc_path, [i, trig_counts[i], phase]);
			};
			SendReply.kr(trig_master, segment_master_trigger_osc_path, [phase]++trigsigs++trig_counts++[trig_master_count, 1-bufindex]);
			//SendReply.kr(Impulse.kr(1), ~segment_info_osc_path, trigsigs);
		}, nil, [max_seg_dur]).add;

		SynthDef(\playbuf, {
			arg bufnum, out;
			var sig = PlayBuf.ar(2, bufnum, BufRateScale.ir(bufnum));
			Poll.kr(Done.kr(sig), sig, "Buffer is done playing.");
			ReplaceOut.ar(out, sig[0]);
		}).add;
	}


	///// HELPERS /////

	outln {
		arg o;
		postln(o);
	}
}