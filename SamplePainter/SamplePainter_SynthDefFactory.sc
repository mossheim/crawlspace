SamplePainter_SynthDefFactory {
	classvar prefix = "wrapdef";
	classvar globalID = 0;

	*nextID {
		^(prefix ++ (globalID = globalID + 1)).asSymbol;
	}

	*mkModFuncWithSynthDefWrap {
		arg function, sInitialWait = 0, sFinalWait = 0, paramLabels = [], bRecordInit = false;

		if(sInitialWait == 0) {bRecordInit = true};

		^{
			arg data, pasteFrame, sf, outFrames, sr, iCycle, paramTables;

			var cond = Condition.new, inbuffer, outbuffer, env, id, name, laced, server = Server.local;

			// error checking
			if(paramLabels.size > paramTables.size) {Error("mkModFuncWithSynthDefWrap: paramLabels size exceeds paramTables size").throw};
			// end error checking

			laced = FloatArray.newClear(data[0].size * data.size);
			laced.size.do {
				|i|
				var iFrame = i.div(data.size);
				var iChan = i % data.size;
				laced[i] = data[iChan][iFrame];
			};
			inbuffer = Buffer.loadCollection(server, laced, data.size);
			outbuffer = Buffer.alloc(server, data[0].size + (sr * (sFinalWait + bRecordInit.if {sInitialWait} {0})).asInteger, data.size);
			server.sync(cond);
			"\t%: ".format(name = this.nextID).post;
			// paramTables.do({|a| a.asArray.postln});

			// to avoid the 1-sample delay that would be caused by EnvGen given a 0-duration initial segment
			env = (sInitialWait > 0).if {
				Env.step([0,1,0],[sInitialWait,inbuffer.duration,sFinalWait])
			} {
				Env.step([1,0],[inbuffer.duration,sFinalWait])
			};
			// "envs done".postln;

			SynthDef(name, {
				var line = EnvGen.ar(env,doneAction:2);
				var sig = PlayBuf.ar(data.size, inbuffer, line) * line;
				var recLine = bRecordInit.if {1} {EnvGen.ar(Env.step(times:[sInitialWait,1]))};
				var tables = paramLabels.collect {|label| EnvGen.ar(paramTables[label],line,timeScale:BufDur.ir(inbuffer)/paramTables[label].duration)};
				// var tables = [EnvGen.ar(paramTables[0],line,timeScale:inbuffer.duration)];
				sig = SynthDef.wrap(function, nil, [sig] ++ tables);
				RecordBuf.ar(sig, outbuffer, run:recLine, loop:0);
			}).send(server, ["/s_new", name, id = Server.local.nextNodeID, 0, 1]);

			// "synthdef sent".postln;

			OSCFunc({
				arg msg;
				outbuffer.loadToFloatArray(action:{
					arg array;
					if(data.size * data[0].size < array.size) {
						data = Array.fill(data.size, {FloatArray.newClear(array.size.div(data.size))});
					};
					array.do {
						|elem, i|
						var iFrame = i.div(data.size);
						var iChan = i % data.size;
						data[iChan][iFrame] = elem;
					};

					cond.unhang;
				})
			}, '/n_end', argTemplate:[id]).oneShot;

			post("\thanging...");
			cond.hang;
			postln("done");
			inbuffer.free;
			outbuffer.free;
			server.sendMsg("/d_free", name);
			data;
		}
	}

	*mkModFunc_timeStretch {
		^{
			arg data, pasteFrame, sf, outFrames, sr, iCycle, paramTables;

			var cond = Condition.new, inbuffer, outbuffer, id, name, laced, server = Server.local, rateEnv, recFrames;

			if(paramTables.size != 1) {Error("mkModFunc_timeStretch: number of params needs to be 1").throw};

			rateEnv = paramTables.getPairs[1];
			recFrames = rateEnv.discretize(data[0].size).reciprocal.sum * 1.005;
			recFrames = recFrames.asInteger;
			"recFrames: %".format(recFrames).postln;

			laced = FloatArray.newClear(data[0].size * data.size);
			laced.size.do {
				|i|
				var iFrame = i.div(data.size);
				var iChan = i % data.size;
				laced[i] = data[iChan][iFrame];
			};

			inbuffer = Buffer.loadCollection(server, laced, data.size);
			outbuffer = Buffer.alloc(server, recFrames, data.size);

			server.sync(cond);
			// "sync".postln;
			"\t%: ".format(name = this.nextID).post;
			// (name = this.nextID).postln;

			SynthDef(name, {
				var playbackRate = EnvGen.ar(rateEnv,timeScale:BufDur.ir(outbuffer)/paramTables.getPairs[1].duration);
				var sig = PlayBuf.ar(data.size, inbuffer, playbackRate, doneAction:2);
				//		Out.ar(0, sig);
				RecordBuf.ar(sig, outbuffer, loop:0);
			}).send(server, ["/s_new", name, id = Server.local.nextNodeID, 0, 1]);

			// "synthdef sent".postln;
			OSCFunc({
				arg msg;
				outbuffer.loadToFloatArray(action:{
					arg array;
					data = Array.fill(data.size, {FloatArray.newClear(array.size.div(data.size))});
					array.do {
						|elem, i|
						var iFrame = i.div(data.size);
						var iChan = i % data.size;
						data[iChan][iFrame] = elem;
					};

					cond.unhang;
				})
			}, '/n_end', argTemplate:[id]).oneShot;

			post("\thanging...");
			cond.hang;
			postln("done");
			inbuffer.free;
			outbuffer.free;
			server.sendMsg("/d_free", name);
			data;
		}
	}
}