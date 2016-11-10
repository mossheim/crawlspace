CrawlEar {
	var <server;
	var <writepath;
	var <nch;

	*new {
		arg server, writepath, nch;
		^super.newCopyArgs(server, writepath, nch).pr_init;
	}

	pr_init {
		fork {
			server = Server.local;
			server.options.blockSize_(64); // use block size of 64
			server.bootSync(Condition());
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