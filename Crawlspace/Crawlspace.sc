Crawlspace {
	classvar <sr = 48000;
	classvar <fftsize = 2048;

	var <server;
	var <earPath, <mapPath, <painterPath;
	var <inChs, <outChs;
	var <tDur;


	*new {
		arg server, earPath, mapPath, painterPath, inChs, outChs, tDur;
		^super.newCopyArgs(server, earPath, mapPath, painterPath, inChs, outChs, tDur).pr_init;
	}

	pr_init {
		CrawlEar.new(server, earPath, inChs);
		CrawlMapper.new(mapPath, tDur, outChs);
		// CrawlSamplePainter.new(painterPath, asInteger(dur * sr), outChs);
	}
}
