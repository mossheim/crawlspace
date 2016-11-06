ThreadedSamplePainter : RealTimeSamplePainter {
	classvar <>maxThreads = 20;

	var cPause;
	var cStop;
	var iNextCycle = 0;
	var workers;

	init {
		super.init;
		workers = LinkedList[];
		cPause = Condition(true);
		cStop = Condition(true);
	}

	start {
		fork {
			while {cStop.test} {
				this.cycle();
				if(cPause.test) {0.1.wait} {cPause.wait};
			}
		}
	}

	pause {
		cPause.test = false;
	}

	stop {
		cPause.test = true;
		cPause.signal;
		cStop.test = false;
	}

	resume {
		cPause.test = true;
		cPause.signal;
	}

	// overwrites AbstractSamplePainter.cycle() to offload most work to worker threads
	cycle {
		var pasteFunc, pasteFrame, data;
		var firstWorker;

		this.out("master thread: cycle % start".format(iCycle));

		// spawn new threads
		if(maxThreads < 1) {Error("master thread: maxThreads must be at least 1").throw};
		while {workers.size < maxThreads} {
			var thread = ThreadedSamplePainterWorker(this, iNextCycle);
			this.out("master: spawning thread number: %".format(iNextCycle));
			iNextCycle = iNextCycle + 1;
			workers.add(thread);
			thread.play;
		};

		// hang on first done-condition
		firstWorker = workers.popFirst;
		//this.out("master: waiting for next worker to complete");
		firstWorker.cDone.wait;
		//this.out("master: worker % is done, performing paste".format(iCycle));

		// get info from first thread
		pasteFunc = firstWorker.pasteFunc;
		pasteFrame = firstWorker.pasteFrame;
		data = firstWorker.data;

		// do paste
		this.doPaste(pasteFunc, pasteFrame, data);

		// update cycle number
		this.out("master: cycle % done".format(iCycle));
		iCycle = iCycle + 1;
	}

	cut {
		// cut cycle calls for using internal variables, can't allow that for volatility reasons
		this.shouldNotImplement(thisMethod);
	}

	paste {
		// broken up by worker/master division of labor
		this.shouldNotImplement(thisMethod);
	}

	*modify_doNothing_wait {
		^{
			arg data, pasteFrame, sf, outFrames, sr, iCycle, tables;
			var toWait = 10.0.rand;
			postln("doNothing_wait: cycle %, waiting %".format(iCycle, toWait));
			toWait.wait;
			postln("doNothing_wait: done waiting");
			data;
		}
	}
}

ThreadedSamplePainterWorker : Routine {
	var master; // master thread
	var iCycle; // which cycle's data am I working with?
	var <cDone; // am I done and should I terminate myself for the GC
	var <pasteFunc, <pasteFrame, <data; // master thread will need these values for pasting!

	*new {
		arg master, iCycle;
		^super.new({}).initWorker(master, iCycle);
	}

	init {
		super.init({/*"attempting to wait".postln; 1.0.wait; "done waiting".postln; */this.workerCycle});
	}

	initWorker {
		arg inmaster, iniCycle;
		master = inmaster;
		iCycle = iniCycle;
		cDone = Condition();
	}

	run {
		super.run;
	}

	workerCycle {
		var filename, sf;
		var startFrame, nCutFrames;

		// analogue to AbstractSamplePainter.cycle();
		postln("worker %: workerCycle start".format(iCycle));
		pasteFrame = master.mkPasteFrame(iCycle);

		// modified cut to avoid using internal variables
		filename = master.chooseCutFile(master.sourceFileList, pasteFrame, iCycle);
		sf = SoundFile(filename);
		sf.openRead;

		nCutFrames = master.mkCutDur(sf, pasteFrame, iCycle);
		startFrame = master.mkCutFrame(sf, pasteFrame, nCutFrames, iCycle);
		data = master.doCut(sf, startFrame, nCutFrames, iCycle);
		sf.close;

		// modify
		postln("worker %: starting modifications".format(iCycle));
		data = master.modify(data, pasteFrame, sf, iCycle); // doesn't rely on internal variables

		// pasteFunc
		pasteFunc = master.choosePasteFunc(master.pasteFuncList, data, pasteFrame, sf, iCycle);

		// notify master of completion
		postln("worker %: complete. unhanging".format(iCycle));
		cDone.test = true;
		cDone.signal;
	}
}