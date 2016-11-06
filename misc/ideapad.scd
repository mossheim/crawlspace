// basic ideas:
/*
partition

a = NetAddr

OSCdef(\test, {
	|msg|
	msg[0].do {
		|x|
		x.ascii.postln;
	};
	"done".postln;
});
OSCdef.trace(true,true)

"\u0"