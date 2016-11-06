// sept 23: this is not my best idea. removing in favor of factory methods

/*
TabledSamplePainter : RealTimeSamplePainter {
	var modifyFuncParamFuncs; // format: dictionary of (FuncName -> dictionary [param -> func])

	*new {
		arg outFilename, outFrames, sourceFileList, modifyFuncList, pasteFuncList, modifyFuncParamFuncs;
		^super.new(outFilename, outFrames, sourceFileList, modifyFuncList, pasteFuncList).initFuncs(modifyFuncParamFuncs);
	}

	initFuncs {
		arg inFuncs;
		modifyFuncParamFuncs = inFuncs;
		^this;
	}

	modify {
		arg data, pasteFrame, sf;

		modifyFuncList.do {
			arg triple;
			var name = triple[0];
			var func = triple[1];
			var probFunc = triple[2];
			var prob = probFunc.value(data, pasteFrame, sf, outFrames, sr, iCycle);
			var bool = coin(prob);
			//this.out(format("probfunc for % produced % - %", name, prob, bool));
			bool.if {
				var tableFuncs = modifyFuncParamFuncs[name] ?? {/*"no func found for %".format(name).warn;*/ Dictionary[]};
				var paramTables = Dictionary[];
				tableFuncs.keysValuesDo({ |k,v| paramTables = paramTables.add(k -> v.value(data, pasteFrame, sf, outFrames, sr, iCycle))});
				// paramTables is now a dictionary of param -> env
				data = func.value(data, pasteFrame, sf, outFrames, sr, iCycle, paramTables);
				//"data size: %".format(data[0].size).postln;
			}
		};

		^data
	}

	*modify_doNothing {
		^{arg data, pasteFrame, sf, outFrames, sr, iCycle, paramTables;
		postln("doNothing's paramtables: %".format(paramTables));
			data}
	}


}
*/