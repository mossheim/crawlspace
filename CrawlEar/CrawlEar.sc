CrawlEar {
	classvar <smootherWidth = 3;
	classvar <segmentTriggerOscPath = '/segment_trigger';
	classvar <segmentMasterTriggerOscPath = '/segment_master_trigger';
	classvar <segmentInfoOscPath = '/segment_info';
	classvar <blocksize = 256;
	classvar <maxSegDur = 60.0;
	classvar <hpf = 50.0;

	var <server;
	var <writepath;
	var <nch;

	*new {
		arg server, writepath, nch;
		^super.newCopyArgs(server, writepath, nch).pr_init;
	}

	pr_init {
		fork {
			server = Server.local;
			server.options.blockSize_(blocksize);
			server.options.sampleRate_(Crawlspace.sr);
			server.bootSync(Condition());

			this.registerSynthDefs();
		}
	}

	registerSynthDefs {
		// smooths by calculating the average of the previous nsamps samples collected when triggered by trigsig
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
		}, nil, [this.class.smootherWidth]).add;

		// calculate the abs value of the derivative of the signal
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

		// output a trigger signal when the input signal is greater than thresh. ignore multiple triggers within the duration trigdur
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

		// sends trigger messages to client to create new audio files.
		// in other words, uses analysis trigger signals to delimit the incoming audio stream.
		SynthDef(\segmenter, {
			arg maxdur, trigdur, bufnum, in_sig, in_threshtrigs, trig_source_en = #[1,1,1,1,1,1,1,1];
			var trigsigs, phase, trig_master, trig_counts, trig_master_count, bufindex, delaysig;

			if(trig_source_en.size != CrawlEar_Analysis.analyses.size) {
				Error("segmenter synthdef: num analysis sigs is not equal to trig source enable size").throw;
			};

			trigsigs = In.kr(in_threshtrigs, CrawlEar_Analysis.analyses.size);
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
			CrawlEar_Analysis.analyses.size.do {
				|i|
				SendReply.kr(trigsigs[i], segmentTriggerOscPath, [i, trig_counts[i], phase]);
			};
			SendReply.kr(trig_master, segmentMasterTriggerOscPath, [phase]++trigsigs++trig_counts++[trig_master_count, 1-bufindex]);
			//SendReply.kr(Impulse.kr(1), ~segmentInfoOscPath, trigsigs);
		}, nil, [maxSegDur]).add;

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
