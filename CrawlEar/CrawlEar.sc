CrawlEar {
	var <server;
	var <writepath;
	var <nch;
	var analyses;

	*new {
		arg server, writepath, nch;
		^super.newCopyArgs(server, writepath, nch).pr_init;
	}

	pr_init {
		fork {
			server = Server.local;
			server.options.blockSize_(64); // use block size of 64
			server.bootSync(Condition());

			analyses = [
				[\p25, {arg chain; SpecPcile.kr(chain, 0.25, 1).log2}, [0.3387, 0.5089, 0.7383, 1.0627, 1.3348]],
				[\p50, {arg chain; SpecPcile.kr(chain, 0.50, 1).log2}, [0.3026, 0.5065, 0.8059, 1.1154, 1.4638]],
				[\p75, {arg chain; SpecPcile.kr(chain, 0.75, 1).log2}, [0.2755, 0.5576, 0.9134, 1.1951, 1.4450]],
				[\p90, {arg chain; SpecPcile.kr(chain, 0.90, 1).log2}, [0.2604, 0.5157, 0.8798, 1.0468, 1.1994]],
				[\flat, {arg chain; SpecFlatness.kr(chain)}, [0.0259, 0.0703, 0.1428, 0.1794, 0.1956]],
				[\cent, {arg chain; SpecCentroid.kr(chain).log2}, [0.1884, 0.3558, 0.6258, 0.8133, 0.9553]],
				[\amp1, {arg chain; Amplitude.kr(sig, 0.01, 0.1)}, [0.0320, 0.0551, 0.0792, 0.1111, 0.1573]],
				[\amp2, {arg chain; Amplitude.kr(sig, 0.25, 0.3)}, [0.0170, 0.0280, 0.0426, 0.0626, 0.0876]]
			];

			this.registerSynthDefs();
		}
	}

	registerSynthDefs {
		// TODO
	}


	///// HELPERS /////

	outln {
		arg o;
		println(o);
	}
}