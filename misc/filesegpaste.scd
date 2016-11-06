/*
benchmarking:

two ways of doing a segment paste
1. use File (writeLE)
2. use SoundFile (writeData)

*/
(
// utility methods:
~testPath = "/users/brianheim/desktop/bench_test.wav";
~createFile = {
	arg nFrames;
	var sf = SoundFile(~testPath);
	sf.numChannels_(1);
	sf.headerFormat_("WAV");
	sf.sampleFormat_("float");
	sf.openWrite();
	sf.writeData(FloatArray.rand2(nFrames,0.001));
	sf.close;
	nil
};

~generateNoise = {
	arg nFrames;
	FloatArray.newFrom(FloatArray.geom(nFrames, 1, 0.999) * Int8Array.fill(nFrames, {[-1,1].choose}));
};

// test func 1:
~testSegPaste_file = {
	arg nFrames, pos;
	var fl = File.open(~testPath, "r+").seek(36);
	var seq = 72.collect({fl.getInt8});
	var datapos = seq.find([$d,$a,$t,$a].ascii);

	if(datapos.isNil) {
		Error("Could not find \"data\" in file header.").throw;
	};

	fl.seek(datapos + 36 + 8 + (pos * 4), 0);
	fl.writeLE(~generateNoise.(nFrames));
	fl.close;
};

// test func 2:
~testSegPaste_sf = {
	arg nFrames, pos;
	var sffrom, sfto, arr;

	File.copy(~testPath, ~testPath++"_temp");
	File.delete(~testPath);
	sffrom = SoundFile.openRead(~testPath++"_temp");
	sfto = SoundFile.openWrite(~testPath);
	arr = FloatArray.newClear(pos);
	sffrom.readData(arr);
	sfto.writeData(arr);
	sfto.writeData(~generateNoise.(nFrames));
	sffrom.seek(nFrames, 1);
	arr = FloatArray.newClear(sffrom.numFrames - nFrames - pos);
	sffrom.readData(arr);
	sfto.writeData(arr);
	sffrom.close;
	sfto.close;
	File.delete(~testPath++"_temp");
}
)

(
// benching
~lens = [100, 1000, 10000, 100000, 1000000, 10000000];
~sizes = [10, 100, 1000, 10000, 100000];
~methods = [~testSegPaste_file, ~testSegPaste_sf];
a = ~methods.collect {
	|method, imethod|
	~lens.collect {
		|the_len|
		~len = the_len;
		~createFile.(~len);
		~sizes.select(_<=~len).collect {
			|size, isize|
			format("benching #%: % frames, chunk size %", imethod+1, ~len, size).postln;
			{
				10.do {
					var pos;
					pos = rrand(0,~len-size);
					method.value(size, pos);
				}
			}.bench;
		}
	}
}
)


(
var file = File.open(~testPath, "r+");
var seq, datapos, pastepos, prevdata, writedata, writedatasize;
var nChannels = 8;

file.seek(36);
seq = Int8Array.fill(72, {file.getInt8});
datapos = seq.find(Int8Array[100,97,116,97]); // "data"
if(datapos.isNil) {
	Error("Could not find \"data\" in file header.").throw;
};
datapos.postln;

)
