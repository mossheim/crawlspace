CrawlMapper {
	var <path, <tDur, <nChs;

	*new {
		arg path, tDur, nChs;
		^super.newCopyArgs(path, tDur, nChs).pr_init;
	}

	pr_init {

	}
}