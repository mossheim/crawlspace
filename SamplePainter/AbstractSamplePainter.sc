AbstractSamplePainter {
	classvar <>sr = 44100;
	classvar <dataBlockSize = 8192;
	classvar <>nChannels = 2;
	classvar <>bVerbose = true;

	var <outFilename; // output filename
	var <outFrames; // size of output file in frames
	var <sourceFileList; // list of <filename -> probFunc>
	var <modifyFuncList; // list of [name, func, probFunc]
	var <pasteFuncList; // list of [name, func, probFunc]

	var <iCycle = 0; // number of cycles completed

	var <data;
	var <sf; // sound file = source file chosen for use
	var <pasteFrame;

	*new {
		arg outFilename, outFrames, sourceFileList, modifyFuncList, pasteFuncList;
		^super.newCopyArgs(outFilename, outFrames, sourceFileList, modifyFuncList, pasteFuncList).init();
	}

	init {
		// input format check
		this.pr_checkInput_outFrames(outFrames);
		this.pr_checkInput_sourceFileList(sourceFileList);
		this.pr_checkInput_modifyFuncList(modifyFuncList);
		this.pr_checkInput_pasteFuncList(pasteFuncList);
		// create output file if it doesn't exist
		if(File.exists(outFilename).not) {
			this.out("creating new output file");
			this.pr_createNewOutputFile();
		} {
			this.out("trying to use preexisting file");
			this.pr_formatExistingOutputFile();
		};
		this.out("loaded.");
		^this;
	}

	pr_createNewOutputFile {
		var arr, nread, iSamp = 0, file = SoundFile(outFilename);
		file.numChannels_(nChannels);
		file.sampleRate_(sr);
		file.sampleFormat_("float");
		file.headerFormat_("WAVE");
		file.openWrite();
		arr = FloatArray.newClear(dataBlockSize);
		while { iSamp < (outFrames * nChannels) } {
			nread = min(dataBlockSize, (outFrames * nChannels) - iSamp);
			if(nread < dataBlockSize) {arr = FloatArray.newClear(nread)};
			file.writeData(arr);
			iSamp = iSamp + nread;
		};
		file.close;
	}

	pr_formatExistingOutputFile {
		var file = SoundFile(outFilename);
		file.openRead();
		if(file.numChannels != nChannels) {
			Error("Number of channels does not match: % should be %".format(file.numChannels, nChannels)).throw;
		};
		if(file.numFrames != outFrames) {
			Error("Number of frames does not match: % should be %".format(file.numFrames, outFrames)).throw;
		};
		if(file.sampleRate != sr) {
			Error("Sample rate does not match: % should be %".format(file.sampleRate, sr)).throw;
		};
		if(file.sampleFormat != "float") {
			Error("Number of channels does not match: % should be %".format(file.sampleFormat.cs, "float".cs)).throw;
		};
		if(file.headerFormat.beginsWith("WAV").not && (file.headerFormat != "RIFF")) {
			Error("Header format is incorrect: % should be %".format(file.headerFormat.cs, "WAVE".cs)).throw;
		};
	}

	pr_checkInput_outFrames {
		arg outFrames;
		// outFrames: expecting positive integer
		if(outFrames.isKindOf(Integer).not) {
			Error("outFrames must be an integer").throw;
		} {
			if(outFrames <= 0) {
				Error("outFrames must be > 0").throw;
			}
		};
	}

	pr_checkInput_sourceFileList {
		arg sourceFileList;
		// sourceFileList: expecting list of associations
		if(sourceFileList.isKindOf(SequenceableCollection).not) {
			Error("sourceFileList expecting a sequenceable collection").throw;
		} {
			sourceFileList.do {
				|assoc|
				if(assoc.isKindOf(Association).not) {
					Error("sourceFileList expecting a sequence of associations").throw;
				}
			}
		};
	}

	pr_checkInput_modifyFuncList {
		arg modifyFuncList;
		// modifyFuncList: expecting list of name, func, probfunc
		if(modifyFuncList.isKindOf(SequenceableCollection).not) {
			Error("modifyFuncList expecting a sequenceable collection").throw;
		} {
			modifyFuncList.do {
				|mf|
				if(mf.class != ModifyFunc) {
					Error("modifyFuncList must contain only modifyFuncs").throw;
				}
			}
		};
	}

	pr_checkInput_pasteFuncList {
		arg pasteFuncList;
		// pasteFuncList: expecting list of name, func, probfunc
		if(pasteFuncList.isKindOf(SequenceableCollection).not) {
			Error("pasteFuncList expecting a sequenceable collection").throw;
		} {
			pasteFuncList.do {
				|pf|
				if(pf.class != PasteFunc) {
					Error("pasteFuncList must contain only pasteFuncs").throw;
				}
			}
		};
	}

	addSourceFile {
		arg filename, probFunc;
		sourceFileList = sourceFileList add: (filename -> probFunc);
	}

	addModifyFunc {
		arg mf;
		modifyFuncList = modifyFuncList add: mf;
	}

	addPasteFunc {
		arg pf;
		pasteFuncList = pasteFuncList add: pf;
	}

	cycle {
		this.out("cycle %".format(iCycle));
		pasteFrame = this.mkPasteFrame(iCycle);
		data = this.cut();
		//AppClock.sched(0, {data.plot(name:"cutdata"); nil});
		data = this.modify(data, pasteFrame, sf, iCycle);
		//AppClock.sched(0, {data.plot(name:"modifydata"); nil});
		this.paste(data, pasteFrame);
		this.out("end of cycle %".format(iCycle));
		iCycle = iCycle + 1;
	}

	mkPasteFrame { arg iCycle; ^this.subclassResponsibility(thisMethod) }

	cut {
		var filename, data, startFrame, nCutFrames;

		filename = this.chooseCutFile(sourceFileList, pasteFrame, iCycle);
		sf = SoundFile(filename);
		sf.openRead;

		nCutFrames = this.mkCutDur(sf, pasteFrame, iCycle);
		startFrame = this.mkCutFrame(sf, pasteFrame, nCutFrames, iCycle);

		data = this.doCut(sf, startFrame, nCutFrames);

		sf.close;

		^data;
	}

	chooseCutFile { arg sourceFileList, pasteFrame, iCycle; ^this.subclassResponsibility(thisMethod) }
	mkCutDur { arg sf, pasteFrame, iCycle; ^this.subclassResponsibility(thisMethod) }
	mkCutFrame { arg sf, pasteFrame, nCutFrames, iCycle; ^this.subclassResponsibility(thisMethod) }

	doCut {
		arg sf, startFrame, nCutFrames;
		var data = Array.fill(nChannels, {FloatArray.newClear(nCutFrames)});
		var nSourceChans = sf.numChannels(), iSamp = 0;
		var arr = FloatArray.newClear(dataBlockSize);
		var chanOffset = 0; /*case
		{nSourceChans < nChannels} {nChannels.rand}
		{nSourceChans > nChannels} {(nSourceChans - nChannels + 1).rand}
		{0};*/

		//this.out("chanOffset: %".format(chanOffset));

		sf.seek(startFrame); // found this out the hard way

		while {iSamp < (nCutFrames * nSourceChans)} {
			var arrsize = min(dataBlockSize, (nCutFrames * nSourceChans) - iSamp);
			(arrsize < dataBlockSize).if {arr = FloatArray.newClear(arrsize)};
			sf.readData(arr);
			arr.do {
				|elem, i|
				var iChan = (iSamp + i) % nSourceChans;
				var iFrame = (iSamp + i).div(nSourceChans);
				if(iChan < nChannels) {
					data[iChan][iFrame] = elem;
				}
			};
			iSamp = iSamp + arr.size;
		};

		^data;
	}

	// subsumed under modifications framework
	/*normalize {
	arg data;
	data = data.collect({
	|chan| chan.every(_==0).if
	{chan}
	{chan.normalize(normalizeLevel.neg,normalizeLevel)}
	});
	data = data - data.collect(_.mean); // weak DC removal
	^data;
	}*/

	modify {
		arg data, pasteFrame, sf, iCycle;

		modifyFuncList.do {
			arg mf;
			var prob = mf.probability(data, pasteFrame, sf, outFrames, sr, iCycle);
			var bool = coin(prob);
			this.out(format("probfunc for % produced % - %", mf.name, prob, bool));
			bool.if {
				data = mf.evaluate(data, pasteFrame, sf, outFrames, sr, iCycle);
			}
		};

		^data
	}

	paste {
		arg data, pasteFrame;
		var func;

		func = this.choosePasteFunc(pasteFuncList, data, pasteFrame, sf, iCycle);

		this.doPaste(func, pasteFrame, data);
	}

	choosePasteFunc { arg pasteFuncList, data, pasteFrame, sf, iCycle; ^this.subclassResponsibility(thisMethod) }

	doPaste {
		arg pf, pasteFrame, data;

		var file = File.open(outFilename, "r+");
		var dataFrames = data[0].size;
		var datapos, pastepos, prevdata, writedata, writedatasize;

		datapos = AbstractSamplePainter.pr_findDataPos(file);
		pastepos = datapos + (pasteFrame * nChannels * 4);

		file.seek(pastepos, 0);
		prevdata = Array.fill(nChannels, {FloatArray.newClear(dataFrames)});
		dataFrames.do {
			|i|
			prevdata.do {
				|chan, j|
				chan[i] = file.getFloatLE; // read in the correct order
			}
		};
		data = pf.evaluate(data, prevdata);
		// data at this point is not guaranteed to have FloatArrays as members
		writedata = FloatArray.newClear(dataFrames * nChannels);
		data.do {
			|subarr, offset|
			subarr.do {
				|subel, i|
				var index = i * nChannels + offset;
				writedata[index] = subel;
			}
		};

		file.seek(pastepos, 0);
		file.writeLE(writedata);
		file.close;
		^this
	}

	*pr_findDataPos {
		arg file; // an open file
		var datapos, dataposflag = false, ata = Int8Array[97, 116, 97];
		var startpos = 36; // 36 is the first place "data" can occur in the standard RIFF/WAVE PCM header

		file.seek(startpos, 0);
		datapos = startpos;
		try {
			while {dataposflag.not} {
				var char = file.getInt8;
				if(char == $d.ascii)  {
					var chararr = Int8Array.fill(3, {file.getInt8});
					if(chararr == ata) {
						dataposflag = true;
					} {
						file.seek(-3, 1); // walkback
						datapos = datapos + 1;
					}
				} {
					datapos = datapos + 1;
				}
			}
			^datapos + 8; // 8 is the data subchunk size
		} {
			Error("Could not find data subchunk").throw;
		}
	}

	*pr_laceData {
		arg toLace, array, nChannels;

		toLace.do {
			|subarr, offset|
			subarr.do {
				|subel, i|
				var index = i * nChannels + offset;
				array[index] = subel;
			}
		}

		^array;
	}

	*pr_unlaceData {
		arg toUnlace, array, nChannels;

		^array.do {
			|subarr, offset|
			subarr.do {
				|subel, i|
				var index = i * nChannels + offset;
				subarr[i] = toUnlace[index];
			}

		}
	}

	mkSFCopy {
		arg file;
		var copyname = file.path++"_temp";
		var copiedfile;
		//this.out("copyname: %".format(copyname));
		File.delete(copyname);
		//this.out("copying % to %".format(file.path, copyname));
		File.copy(file.path, copyname);
		copiedfile = SoundFile(copyname);

		^copiedfile;
	}

	out {
		arg string, isVerbose = true;
		if((isVerbose && bVerbose.not).not) { string.postln }
	}
}

SamplePainterFunc {
	var name, func, probFunc;

	*new {
		arg name, func, probFunc;
		^super.newCopyArgs(name, func, probFunc).pr_init;
	}

	pr_init {
		if(name.isKindOf(Symbol).not) {
			Error("name should be a symbol").throw;
		};
		if(func.isKindOf(Function).not) {
			Error("func should be a function").throw;
		};
	}

	probability {
		|... args|
		^probFunc.value(*args);
	}

	evaluate {
		|... args|
		^func.value(*args);
	}

	function {
		^func;
	}

	name {
		^name;
	}
}

ModifyFunc : SamplePainterFunc {
	asString {
		^format("ModifyFunc[%]", name);
	}
}

// evaluate takes pasteData, fileData (both arrays of FloatArrays)
// returns new pasteData (an array of arrays of floats, could be FloatArrays)
PasteFunc : SamplePainterFunc {
	asString {
		^format("PasteFunc[%]", name);
	}
}