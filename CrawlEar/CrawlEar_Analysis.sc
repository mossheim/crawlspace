CrawlEar_Analysis {
	classvar analysis_data;
	classvar sigma_data;

	// [name, uses_fft, analysis function, [thresholds (2, 2.5, 3, 3.5, 4 sigma)]]
	*analyses {
		analysis_data = analysis_data ? [
			[\p25, true, {arg chain; SpecPcile.kr(chain, 0.25, 1).log2}, [0.3387, 0.5089, 0.7383, 1.0627, 1.3348]],
			[\p50, true, {arg chain; SpecPcile.kr(chain, 0.50, 1).log2}, [0.3026, 0.5065, 0.8059, 1.1154, 1.4638]],
			[\p75, true, {arg chain; SpecPcile.kr(chain, 0.75, 1).log2}, [0.2755, 0.5576, 0.9134, 1.1951, 1.4450]],
			[\p90, true, {arg chain; SpecPcile.kr(chain, 0.90, 1).log2}, [0.2604, 0.5157, 0.8798, 1.0468, 1.1994]],
			[\flat, true, {arg chain; SpecFlatness.kr(chain)}, [0.0259, 0.0703, 0.1428, 0.1794, 0.1956]],
			[\cent, true, {arg chain; SpecCentroid.kr(chain).log2}, [0.1884, 0.3558, 0.6258, 0.8133, 0.9553]],
			[\amp1, false, {arg sig; Amplitude.kr(sig, 0.01, 0.1)}, [0.0320, 0.0551, 0.0792, 0.1111, 0.1573]],
			[\amp2, false, {arg sig; Amplitude.kr(sig, 0.25, 0.3)}, [0.0170, 0.0280, 0.0426, 0.0626, 0.0876]]
		];
		^analysis_data;
	}

	*analyses_names {
		^this.analyses.flop[0];
	}

	*analyses_fftUse {
		^this.analyses.flop[1];
	}

	*analyses_funcs {
		^this.analyses.flop[2];
	}

	*analyses_allThreshes {
		^this.analyses.flop[3];
	}

	*analyses_threshes {
		arg sigma;
		sigma = sigma - 2 * 2;
		if(sigma < 0 || sigma > this.analyses[0][4].size) {
			this.invalidInput(thisMethod, "sigma", sigma, "Between 0 and % inclusive.".format(this.analyses[0][4].size));
		};

		^this.analyses.flop[4].flop.blendAt(sigma);
	}

	*input_dir {
		^"test_audio/input".resolveRelative;
	}

	*output_dir {
		^"test_audio/output".resolveRelative;
	}

	// 2, 2.5, 3, 3.5, 4, 4.5
	*sigmas {
		sigma_data = sigma_data ? [0.954499736,0.987580669,0.997300204,0.999534742,0.999936658,0.99993204];
		^sigma_data;
	}

	*performAnalysis {
		var server = Server.local;
		server.options.sampleRate_(Crawlspace.sr);
		server.options.blockSize_(CrawlEar.blocksize);
		fork {
			var dur, files, analyses;
			server.bootSync(Condition());

			files = PathName(this.input_dir).files;
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

				inbuf = Buffer.read(server, filepath.fullPath);
				outbuf = Buffer.alloc(server,server.sampleRate.nextPowerOfTwo,analyses.size + 1);
				output_filename = this.outpu_dir +/+ filepath.fileNameWithoutExtension + "_analysis.wav";
				outbuf.write(output_filename, "wave", "float", leaveOpen:true);
				server.sync(Condition());
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
	}
}
