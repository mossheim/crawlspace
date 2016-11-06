AbstractMixerUpper.nChannels_(8);
AbstractMixerUpper.sr_(48000);
a = AbstractMixerUpper("/users/brianheim/desktop/test.wav", 50000, ["/users/brianheim/desktop/mu_test/bach_stereo_96.wav"], [], []);

AbstractMixerUpper_Test.new.run
AbstractMixerUpper_DoCut_Test.new.run

d = SoundFile("/users/brianheim/desktop/test.wav")
File.exists("/users/brianheim/desktop/test.wav")
File.delete("/users/brianheim/desktop/test.wav")
d.openRead
d.numChannels

// benchmarking
~data = Array.fill(5, {FloatArray.fill(1000000, {1.0.rand2})});

// ~1.7
{
	~interdata = FloatArray.newClear(~data[0].size * ~data.size);
	~interdata.size.do {
		|i|
			var iFrame = i.div(~data.size);
			var iChan = i % ~data.size;
			~interdata[i] = ~data[iChan][iFrame];
	}
}.bench

// ~0.7!!!
{
	~size = ~data.size;
	~interdata = FloatArray.newClear(~data[0].size * ~data.size);
	~data.do {
		|subarr, offset|
		subarr.do {
			|subel, i|
			var index = i * ~size + offset;
			~interdata[index] = subel;
		}
	}
}.benchn(10)


~path = "/Users/brianheim/Desktop/mu_test/beet_mono_44 copy.wav";
a = SoundFile(~path)
a.numFrames
a.numChannels
a.openRead
a.close

x = a.numFrames * a.numChannels * a.sampleSize

b = File.new("/Users/brianheim/Desktop/mu_test/beet_mono_44 copy.wav", "r+");
b.length
b.close
b.seek(44)
b.putFloatLE(0.5) // worked!
b.copy

y = b.length

y - x
b.seek(0)
b.seek(-200, 2)
200.do{b.getChar.postln}

(y-x).do {
	|i|
	format("%\t%", i, b.getInt8).postln;
}

c = b.getInt32LE
c.asHexString

b = File.open(~testPath, "r")
// RIFF SPECIFICATION

b.seek(0);
// RIFF chunk

b.nextN(4); // "RIFF"
// 4
b.getInt32LE; // size in bytes - 8
// 8
b.nextN(4); // "WAVE"
// 12
// fmt subchunk

b.nextN(4); // "fmt "
// 16
b.getInt32LE; // size of the rest of this chunk (16 for PCM)
// 20
b.getInt16LE; // quantization = 1 for PCM
// 22
b.getInt16LE; // num channels
// 24
b.getInt32LE; // sample rate
// 28
b.getInt32LE; // byte rate
// 32
b.getInt16LE; // block align
// 34
b.getInt16LE; // bits per sample
// 36
// data subchunk

b.nextN(4); // "data"
// 40
b.getInt32LE; // rest of subchunk size in bytes
// 44

// TO SEEK CORRECTLY:
// 1. ensure that output file is always wave (abstract mixer upper should handle this)
// 2. go to byte 34 to get bits per sample
// 3. start seeking from byte 44

b.getFloatLE
b.close

b.seek(0)
a = b.nextN(100)

a.find("data")
a = SoundFile(~testPath)
a.close
a.sampleFormat







(
~data = [FloatArray.fill(44100, 1), FloatArray.fill(44100, 0)];
~func = {|sig| sig};

{~new = RealTimeMixerUpper.doModificationWithSynthDefWrap(~data, ~func, 0)}.fork
)

~new == ~data
