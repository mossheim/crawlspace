// this code tests the idea of using two alternating buffers to write segments of audio out on trigger events

(
var folder = "test_audio".resolveRelative;
~maxdur = 60;
~bs = Buffer.allocConsecutive(4, s, s.sampleRate * ~maxdur, 2);
~bs.do({
	|buf, i|
	buf.write(folder +/+ "write_test_%.wav".format(i), "wave", "float", leaveOpen:true);
});
SynthDef(\write_test, {
	var sig = Saw.ar([20,21]*LFNoise0.kr(3).exprange(1,50),0.5);
	var trig = Dust.ar(0.1);
	var phasor = Phasor.ar(trig, SampleDur.ir, 0, ~maxdur);

	var seq = Dseq((0..(~bs.size-1)), inf);
	var num = Duty.ar(3.0, 0, seq);
	SendReply.ar(Changed.ar(num), '/switch_file', num);
	Line.ar(dur:10, doneAction:2);
}).add();
)

(
var bufnum = 0;
var filenum = 2;
OSCdef(\write_test, {
	|msg|
	msg.postln;
	~bs[bufnum].close;
	~bs[bufnum].write(folder +/+ "write_test_%.wav".format(filenum), "wave", "float", leaveOpen:true);
	filenum = filenum + 1;
	bufnum = 1 - bufnum;
}, '/switch_file');
Synth(\write_test);
)


~bu = Buffer.alloc(s, 131072, 2);
{DiskOut.ar(~bu.bufnum, Saw.ar([20,21]))}
