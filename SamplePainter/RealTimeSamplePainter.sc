RealTimeSamplePainter : SimpleSamplePainter {
	var server;

	init {
		super.init;
		server = Server.local;
		server.options.sampleRate_(sr);
		server.bootSync(Condition());
		^this;
	}

	cycle {
		server.bootSync(Condition());
		super.cycle();
	}

	// this needs to be fixed
	*modifyWrap {
		arg data, function, sExtraWaitTime = 0;
		var cond = Condition.new, inbuffer, outbuffer, id, name;
		var interleavedData;

		if(data[0].size == 0) {
			this.invalidInput(thisMethod, "data", data, "a ")
		};
		if(sExtraWaitTime.isNegative) {
			this.invalidInput(thisMethod, "sExtraWaitTime", sExtraWaitTime, "a non-negative number");
		};
		interleavedData = AbstractSamplePainter.pr_laceData(data, FloatArray.newClear(data[0].size * data.size), data.size);
		inbuffer = Buffer.loadCollection(Server.local, interleavedData, data.size);
		outbuffer = Buffer.alloc(Server.local, data[0].size + (sr * sExtraWaitTime).asInteger, data.size);
		Server.local.sync(cond);
		inbuffer.sampleRate_(sr);
		name = this.hash.asString;
		id = Server.local.nextNodeID;
		OSCFunc({
			arg msg;
			outbuffer.loadToFloatArray(action:{
				arg array;
				if(sExtraWaitTime > 0) {
					data = Array.fill(data.size, {FloatArray.newClear(array.size.div(data.size))});
				};
				data = AbstractSamplePainter.pr_unlaceData(array, data, data.size);
				cond.unhang;
			});
		}, '/n_end', argTemplate:[id]).oneShot;
		SynthDef(name, {
			var sig = PlayBuf.ar(data.size, inbuffer, BufRateScale.ir(inbuffer));
			sig = sig * Delay1.ar(Trig1.ar(1,BufDur.ir(inbuffer)));
			sig = SynthDef.wrap(function, nil, [sig]);
			RecordBuf.ar(sig, outbuffer, loop:0, doneAction:2);
		}).send(Server.local, ["/s_new",name,id]);
		post("\thanging...");
		cond.hang;
		postln("done");
		inbuffer.free;
		outbuffer.free;
		^data;
	}

	// simple things

	/*
	modify_pulseEnv {
	arg data, pasteFrame, outFrames, sr;

	var pos = pasteFrame / outFrames;
	// var freq = pos.linlin(0.0,1,1,15).postln;
	var freq = exprand(2.0,10.0);
	var func = { arg sig; sig * LFPulse.kr(freq).range(0,1) };
	data = this.doModificationWithSynthDefWrap(data, func);

	^data;
	}

	modify_lpf {
	arg data, pasteFrame, outFrames, sr;
	var func = { arg sig; BLowPass.ar(sig, 500) };
	^this.doModificationWithSynthDefWrap(data, func);
	}

	modify_hpf {
	arg data;
	^this.doModificationWithSynthDefWrap(data, BHiPass.ar(_, 2000));
	}

	modify_bpf {
	arg data;
	^this.doModificationWithSynthDefWrap(data, BBandPass.ar(_, exprand(300,2000)))
	}
	*/

	// examples of how real-time parameter indexing could be done. another option is to load tables into buffers, implemented later

	/*
	modify_lpf_lineDown {
	arg data, pasteFrame, outFrames, sr;
	var startPoint = pasteFrame / outFrames;
	var endPoint = (pasteFrame + data[0].size) / outFrames;
	var startFreq = startPoint.linexp(0.0,1.0,10000,60);
	var endFreq = endPoint.linexp(0.0,1.0,10000,60);
	var dur = data[0].size / sr;
	var func = {arg sig; BLowPass.ar(sig, Line.ar(startFreq, endFreq, dur)) };
	^this.doModificationWithSynthDefWrap(data, func);
	}

	modify_pulseEnv_lineUp {
	arg data, pasteFrame, outFrames, sr;
	var startPoint = pasteFrame / outFrames;
	var endPoint = (pasteFrame + data[0].size) / outFrames;
	var startFreq = startPoint.linexp(0.0,1.0,1,20);
	var endFreq = endPoint.linexp(0.0,1.0,1,20);
	var dur = data[0].size / sr;
	var func = {arg sig; sig * LFPulse.ar(Line.ar(startFreq, endFreq, dur)).range(0,1) };
	^this.doModificationWithSynthDefWrap(data, func);
	}
	*/



}
