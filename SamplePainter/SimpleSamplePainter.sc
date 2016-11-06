SimpleSamplePainter : AbstractSamplePainter {
	// classvar <>sEndingSilence = 10; // to allow for tails
	classvar <>sCutDur_low = 1;
	classvar <>sCutDur_hi = 10;
	// this implementation does uniform distributions for most methods

	var <cutFrame; // for testing only
	var <cutDur; // for testing only

	mkPasteFrame {
		arg iCycle;
		var endFrame = outFrames - (sr * sCutDur_hi);
		var pasteFrame = max(endFrame, 0).rand;
		this.out("pasteFrame: %".format(pasteFrame));
		^pasteFrame;
	}

	chooseCutFile {
		arg pasteFrame, iCycle;
		var probs = sourceFileList.collect({|assoc| assoc.value.value(pasteFrame, outFrames, sr, iCycle)});
		var filename = sourceFileList.collect(_.key).wchoose(probs.normalizeSum);
		this.out("cutFile: %".format(filename));
		^filename;
	}

	mkCutDur {
		arg sf, pasteFrame, iCycle;
		var low = sCutDur_low * sr;
		var hi = sCutDur_hi * sr;
		cutDur = exprand(low, hi).asInteger;
		cutDur = min(cutDur, sf.numFrames());
		this.out("file has % frames".format(sf.numFrames()));
		this.out("cutDur: %".format(cutDur));
		^cutDur;
	}

	mkCutFrame {
		arg sf, pasteFrame, cutDur, iCycle;
		var maxFrame = sf.numFrames() - cutDur;
		cutFrame = maxFrame.asInteger.rand;
		this.out("cutFrame: %".format(cutFrame));
		^cutFrame;
	}

	choosePasteFunc {
		arg data, pasteFrame, sf, iCycle;
		var probs = pasteFuncList.collect(_.probability(data, pasteFrame, sf, outFrames, sr, iCycle));
		var index = probs.normalizeSum.windex;
		//this.out("pasteFunc: %".format(pasteFuncList[index].name));
		^pasteFuncList[index];
	}

	// modify methods

	*modify_doNothing {
		^{arg data, pasteFrame, outFrames, sr; data}
	}

	// paste methods

	*paste_replace {
		^{
			arg pasteData, fileData;
			pasteData;
		}
	}

	*paste_add {
		^{
			arg pasteData, fileData;
			pasteData + fileData;
		}
	}
}