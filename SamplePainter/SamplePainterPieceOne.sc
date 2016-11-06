SamplePainterPieceOne : ThreadedSamplePainter {
	classvar part1split = 0.382;
	classvar sEndingSilence = 5;
	classvar sOutput = 120;

	mkPasteFrame {
		var phase = if(coin(part1split)) {
			gauss(0.191, 0.06367/1).fold(0.0, 1.0);
		} {
			gauss(0.691, 0.103/1.1).fold(0.0, 1.0);
		};

		var frame = asInteger(phase * (outFrames - (sEndingSilence * sr)));
		this.out("pasteFrame: %".format(frame));

		^frame
	}

	*sourceFileProbs {
		arg index;

		var func = switch(index)
		{0} {{arg pasteFrame, outFrames; (pasteFrame <= (part1split*outFrames)).asInteger}}
		{1} {{arg pasteFrame, outFrames; (pasteFrame > (part1split*outFrames)).asInteger}}
		{"no prob function found for source file at index: %".format(index).warn; nil};

		^func;
	}

	mkCutDur {
		var classes = [\short, \long];
		var weights = [3, 1].normalizeSum;

		var durClass = classes.wchoose(weights);
		var dur = durClass.switch
		{\short} {gauss(0.18, 0.04).wrap(0.01, 0.5)}
		{\long} {gauss(3.0, 0.8).wrap(1.0, 5.0)};

		var nFrames = (dur * sr).round.asInteger;

		"cutFrames: %".format(nFrames).postln;

		^nFrames;
	}

	*pasteProbFunctions {
		arg which;
		var cycles = 20000;
		^(switch(which)
			{\add} {{
				arg data, pasteFrame, sf, outFrames, sr, iCycle;
				data[0].size.linlin(0,sr*3,1,2)
				}}
			{\replace} {{
				arg data, pasteFrame, sf, outFrames, sr, iCycle;
				data[0].size.linlin(0,sr*3,1,0)
				}}).clip(0,1);
	}

	*modifyTableParamFuncs {
		arg which;

		^switch(which)
		{\lowPass} {
			Dictionary[
				\freq -> {
					var freqTbl, freq0, freq1;
					freq0 = exprand(1000, 10000);
					// freq1 = freq0 * rrand(4.0,6);
					// "low pass freqs: %, %".format(freq0, freq1).postln;
					freqTbl = Env([freq0], [1], 'exp');
					freqTbl;
				},

				\rq -> {
					var rqTbl;
					rqTbl = Env.step([0.5],[1]);
					rqTbl;
				}
			]
		}

		{\hiPass} {
			Dictionary[
				\freq -> {
					var freqTbl, freq0, freq1;
					freq0 = exprand(100, 1000);
					// freq1 = freq0 * rrand(40,6);
					// "hi pass freqs: %, %".format(freq0, freq1).postln;
					freqTbl = Env([freq0], [1], 'exp');
					freqTbl;
				},

				\rq -> {
					var rqTbl;
					rqTbl = Env.step([0.5],[1]);
					rqTbl;
				}
			]
		}

		{\bandPass} {
			Dictionary[
				\freq -> {
					var freqTbl, freq0, freq1;
					freq0 = exprand(50, 80);
					freq1 = freq0 * rrand(40,6);
					// "band pass freqs: %, %".format(freq0, freq1).postln;
					freqTbl = Env([freq0, freq1], [1], 'exp');
					freqTbl;
				},

				\bw -> {
					var rqTbl;
					rqTbl = Env.step([0.1],[1]);
					rqTbl;
				}
			]
		}

		{\stretch} {
			Dictionary[
				\rate -> {
					var rateTbl, rate;
					rate = exprand(0.5,1.8);
					// "stretch rate: %".format(rate).postln;
					rateTbl = Env([rate, rate * exprand(1/1.25,1.25)], [1], 'exp');
				}
			]
		}

		{\tremolo} {
			Dictionary[
				\freq -> {
					var freqTbl, rate;
					rate = exprand(3.0,20.0);
					// "freq: %".format(rate).postln;
					freqTbl = Env.step([rate],[1]);
				},
				\depth -> {
					var depthTbl, depth;
					depth = rrand(0.2,1);
					// "depth: %".format(depth).postln;
					depthTbl = Env.step([depth],[1]);
				}
			]
		}

		{\choppy} {
			Dictionary[
				\freq -> {
					var freqTbl, rate;
					rate = exprand(5.0,15);
					freqTbl = Env.step([rate],[1]);
				},
				\dur -> {
					var durTbl, dur;
					dur = rrand(0.01,0.1);
					durTbl = Env.step([dur],[1]);
				},
				\durDispersion -> {
					var durDisTbl, durDis;
					durDis = 0.3;
					durDisTbl = Env.step([durDis],[1])
				}
			]
		}

		{\env_unpop} {
			Dictionary[
				\mul -> {
					Env([0,1,1,0],[1,0,1],'sin');
				}
			]
		}

		{\loop} {
			Dictionary[
				\loops -> {
					Env([rrand(3,8),0],[1]);
				}
			]
		}

		{\mul_offline_dimpercycle} {
			Dictionary[
				\coeff -> {
					arg data, pasteFrame, sf, outFrames, sr, iCycle;
					var minCycle = 1, maxCycle = 3000;
					var minDb = (-60).dbamp;
					var coeff = iCycle.linexp(minCycle, maxCycle, 1, minDb);
					Env.step([coeff],[1]);
				}
			]
		}
		{"table function for % not found".format(which).warn};
	}

	*modify_testFunc {
		^SamplePainter_SynthDefFactory.mkModFuncWithSynthDefWrap({SinOsc.ar(Rand(400, 450)!2,0,0.1)})
	}

	*modify_doNothing {
		^SamplePainter_SynthDefFactory.mkModFuncWithSynthDefWrap({|sig| sig}, 0.01, 0.01, []);
	}

	*modify_lowPass {
		var func = BLowPass.ar(_,_,_);

		^SamplePainter_SynthDefFactory.mkModFuncWithSynthDefWrap(func, 0.01, 0.01, [\freq, \rq]);
	}

	*modify_hiPass {
		// sig, freq, rq
		^SamplePainter_SynthDefFactory.mkModFuncWithSynthDefWrap(BHiPass.ar(_,_,_), 0.01, 0.01, [\freq, \rq]);
	}

	*modify_bandPass {
		// sig, freq, bw
		^SamplePainter_SynthDefFactory.mkModFuncWithSynthDefWrap(BBandPass.ar(_,_,_), 0.01, 0.01, [\freq, \bw]);
	}

	*modify_stretch {
		//
		^SamplePainter_SynthDefFactory.mkModFunc_timeStretch()
	}

	*modify_pitchShift {
		// sig | window | pitch ratio | pitch dispersion | time dispersion
		^SamplePainter_SynthDefFactory.mkModFuncWithSynthDefWrap(PitchShift.ar(_,_,_,_,_), 0, [\window, \pitchRatio, \pitchDispersion, \timeDispersion] );
	}

	*modify_fftScramble {
		^SamplePainter_SynthDefFactory.mkModFuncWithSynthDefWrap({
			|sig, wipe, width, trigFreq|
			var chain = FFT({LocalBuf(4096)} ! sig.size, sig);
			chain = PV_BinScramble(chain,wipe,width,Impulse.kr(trigFreq));
		}, 0.1, 0.1, [\wipe, \width, \trigFreq]);
	}

	*modify_loop {
		^{
			arg data, pasteFrame, sf, outFrames, sr, iCycle, paramTables;
			var nLoops = paramTables[\loops].at(0);
			var looped = data;
			(nLoops-1).do {
				looped = looped.collect({|arr, i| arr++data[i]})
			};

			looped;
		}
	}

	*modify_normalize {
		^{
			arg data, pasteFrame, sf, outFrames, sr, iCycle, paramTables;
			var fNorm = paramTables[\normalizeLevel].at(0);
			var max = data.collect(_.maxItem).maxItem;
			var ratio = fNorm / max;

			data = data.collect(_*ratio);

			data
		}
	}

	*modify_mul {
		^SamplePainter_SynthDefFactory.mkModFuncWithSynthDefWrap(_*_, 0, 0, [\mul]);
	}

	*modify_round {
		^{
			arg data, pasteFrame, sf, outFrames, sr, iCycle, paramTables;
			var toRound = paramTables[\round].at(0);
			data = data.collect(_.round(toRound));

			data
		}
	}

	*modify_tremolo {
		^SamplePainter_SynthDefFactory.mkModFuncWithSynthDefWrap({
			|sig, freq, depth|

			sig * LFCub.kr(freq, 0).range(1-depth, 1);
		}, 0, 0, [\freq, \depth])
	}

	*modify_choppy {
		^SamplePainter_SynthDefFactory.mkModFuncWithSynthDefWrap({
			|sig, freq, dur, durDispersion|
			var trig = GaussTrig.kr(freq);
			var gaussian = {TRand.kr(0.0,1.0,trig)}.dup(8).sum;
			var chopdur = dur * gaussian.linexp(0.0,8.0,1/(durDispersion+1),1*(durDispersion+1));
			var chopper = 1-Trig1.ar(trig,chopdur);

			sig * chopper.lag2(0.04);
		}, 0, 0, [\freq, \dur, \durDispersion])
	}

	*modify_trimEndingSilence {
		^{
			arg data;

			var nDrop, i = data[0].size;
			var flag = false;
			while {flag.not && (i > 0)} {
				i = i - 1;
				data.size.do {
					|j|
					flag = flag || (data[j][i] != 0);
				};
			};

			data = data.collect(_.keep(i));

			data
		}
	}

	*modify_truncate {
		^{
			arg data, pasteFrame, sf, outFrames;
			var toKeep = min(data[0].size, outFrames - pasteFrame);
			data = data.collect(_.keep(toKeep));
			data;
		}
	}

	*modify_unpop_offline {
		^{
			arg data, pasteFrame, sf, outFrames, sr, iCycles, paramTables;
			var dFade = 0.01;
			var nSamp = min(asInteger(sr * dFade), data[0].size.div(2));
			var kernel = FloatArray.fill(nSamp, {|i| (i * 0.5pi / nSamp).sin});
			var revKernel = kernel.reverse;
			data.do {
				|chandata, iChan|
				var startData = chandata[0..(nSamp-1)] * kernel;
				var endData = chandata[(data[0].size-nSamp)..(data[0].size-1)] * revKernel;
				startData = FloatArray.newFrom(startData);
				endData = FloatArray.newFrom(endData);
				chandata.overWrite(startData, 0);
				chandata.overWrite(endData, data[0].size - nSamp)
			};

			data;
		}
	}

	*modify_mul_offline {
		^{
			arg data, pasteFrame, sf, outFrames, sr, iCycles, paramTables;
			var coeff = paramTables[\coeff].at(0);
			(data * coeff).collect({|dataChan| FloatArray.newFrom(dataChan)});
		}
	}

	*paste_replace_unpop {
		var nSamp = sr.div(100); // 0.01 sec
		var sinKernel = Array.fill(nSamp, {|i| (0.5pi * i / (nSamp-1)).sin});
		var cosKernel = Array.fill(nSamp, {|i| (0.5pi * i / (nSamp-1)).cos});
		^{
			arg pasteData, iSamp, fileData;
			// pasteData is the entire 2d array of nChannels x nPasteFrames size, and fileData is an array of datablocksize size
			// iSamp ranges from 0 to (pasteData.size * pasteData[0].size - 1)
			for(iSamp, fileData.size + iSamp - 1) {
				|i, j|
				var iFrame = i.div(nChannels);
				var iChan = i % nChannels;
				case
				{iFrame < nSamp} {
					fileData[j] = (pasteData[iChan][iFrame] * sinKernel[iFrame]) + (fileData[j] * cosKernel[iFrame]);
				}
				{iFrame > (pasteData[0].size - nSamp - 1)} {
					var k = iFrame - pasteData[0].size + nSamp;
					fileData[j] = (pasteData[iChan][iFrame] * cosKernel[k]) + (fileData[j] * sinKernel[k]);
				}
				{fileData[j] = pasteData[iChan][iFrame]};
			};
			fileData;
		}
	}


}

