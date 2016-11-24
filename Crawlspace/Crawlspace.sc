Crawlspace {
	classvar <sr = 48000;
	classvar <fftsize = 2048;

	var <server;
	var <earPath, <mapPath, <painterPath;
	var <inChs, <outChs;
	var <dur;


	*new {
		arg server, earPath, mapPath, painterPath, inChs, outChs, dur;
		^super.newCopyArgs(server, earPath, mapPath, painterPath, inChs, outChs, dur).pr_init;
	}

	pr_init {
		CrawlEar.new(server, earPath, inChs);
		CrawlMapper.new(mapPath, asInteger(dur * sr), outChs);
		// CrawlSamplePainter.new(painterPath, asInteger(dur * sr), outChs);
	}
}
