CrawlSamplePainter : ThreadedSamplePainter {
	*new {
		arg outFilename, outFrames, sourceFileList, modifyFuncList, pasteFuncList;
		^super.new(outFilename, outFrames, sourceFileList, modifyFuncList, pasteFuncList).init();
	}
}