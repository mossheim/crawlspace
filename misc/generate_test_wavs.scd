// creating test file

// specifications
// 0..99 = 1
// 100..199 = 0
// 200..299 = 1
// 300..305 = test pattern
// 306..8188 = noise
// 8189..8194 = test pattern
// 8195..16380 = noise
// 16381..16386 = test pattern
// 16387..16393 = noise
// 16394..16399 = test pattern

(
// create mono
~testp = FloatArray[0, 0.5, 1, -1, -0.5, 0];
~path = "/users/brianheim/desktop/mu_test/precision_test_mono.wav";
~sf = SoundFile(~path);
~sf.numChannels_(1);
~sf.sampleRate_(44100);
~sf.openWrite();

// test region 1
~sf.writeData(FloatArray.fill(100, 1));
~sf.writeData(FloatArray.fill(100, 0));
~sf.writeData(FloatArray.fill(100, 1));

// test region 2
~sf.writeData(~testp);
~sf.writeData(FloatArray.rand(7883, -0.9, 0.9));

// test region 3
~sf.writeData(~testp);
~sf.writeData(FloatArray.rand(8186, -0.9, 0.9));

// test region 4
~sf.writeData(~testp);
~sf.writeData(FloatArray.rand(7, -0.9, 0.9));

// test region 5
~sf.writeData(~testp);

~sf.close;

~sf.openRead();
~sf.numFrames.postln;
~sf.close;
)

(
// create stereo
~testp = FloatArray[0, 0.5, 1, -1, -0.5, 0];
~testp2 = FloatArray[0.125, 0.25, 0.75, -0.125, -0.25, -0.75];
~path = "/users/brianheim/desktop/mu_test/precision_test_stereo.wav";
~sf = SoundFile(~path);
~sf.numChannels_(2);
~sf.sampleRate_(44100);
~sf.openWrite();

// test region 1: both chans 0 amid 1
~sf.writeData(FloatArray.fill(200, 1));
~sf.writeData(FloatArray.fill(200, 0));
~sf.writeData(FloatArray.fill(200, 1));

// test region 2: chan 0 = 0 amid 1
~sf.writeData(FloatArray.fill(200, {|x| x.odd.binaryValue}));
~sf.writeData(FloatArray.fill(200, 1));

// test region 3: chan 1 = 0 amid 1
~sf.writeData(FloatArray.fill(200, {|x| x.even.binaryValue}));
~sf.writeData(FloatArray.fill(200, 1));

// test region 4: both chans have test pattern
~sf.writeData(FloatArray.fill(12, {|i| ~testp[i.div(2)]}));
~sf.writeData(FloatArray.rand(200, -0.9, 0.9));

// test region 5: chan 0 = testp, chan 1 = testp2
~sf.writeData(FloatArray.fill(12, {|i|
	i.even.if {~testp[i/2]} {~testp2[i-1/2]};
}));
~sf.writeData(FloatArray.rand(8364, -0.9, 0.9));

// test region 6: both chans have test pattern
~sf.writeData(FloatArray.fill(12, {|i| ~testp[i.div(2)]}));

~sf.close;

~sf.openRead();
~sf.numFrames.postln;
~sf.close;
)

(
// create quad
~testp = FloatArray[0, 0.5, 1, -1, -0.5, 0];
~testp2 = FloatArray[0.125, 0.25, 0.75, -0.125, -0.25, -0.75];
~path = "/users/brianheim/desktop/mu_test/precision_test_quad.wav";
~sf = SoundFile(~path);
~sf.numChannels_(4);
~sf.sampleRate_(44100);
~sf.openWrite();

// test region 1: all chans 0 amid 1
~sf.writeData(FloatArray.fill(400, 1));
~sf.writeData(FloatArray.fill(400, 0));
~sf.writeData(FloatArray.fill(400, 1));

// test region 2: chan 0 = 0 amid 1
~sf.writeData(FloatArray.fill(400, {|x| (x % 4 != 0).binaryValue}));
~sf.writeData(FloatArray.fill(400, 1));

// test region 3: chan 1 = 0 amid 1
~sf.writeData(FloatArray.fill(400, {|x| (x % 4 != 1).binaryValue}));
~sf.writeData(FloatArray.fill(400, 1));

// test region 4: chan 2 = 0 amid 1
~sf.writeData(FloatArray.fill(400, {|x| (x % 4 != 2).binaryValue}));
~sf.writeData(FloatArray.fill(400, 1));

// test region 5: chan 3 = 0 amid 1
~sf.writeData(FloatArray.fill(400, {|x| (x % 4 != 3).binaryValue}));
~sf.writeData(FloatArray.fill(400, 1));

// test region 6: all chans have testp
~sf.writeData(FloatArray.fill(24, {|x| ~testp[x.div(4)]}));
~sf.writeData(FloatArray.rand(400, -0.9, 0.9));

// test region 7: chans have test1, test2, revtest1, revtest2
~quadchpat = ~testp ++ ~testp2 ++ ~testp.reverse ++ ~testp2.reverse;
~quadchpat = ~quadchpat.clump(6).flop;
~quadchpat = FloatArray.newFrom(~quadchpat.flat);
~sf.writeData(~quadchpat);
~sf.writeData(FloatArray.rand(2994-1212*4, -0.9, 0.9));

// test region 8: all chans have testp at end of file
~sf.writeData(FloatArray.fill(24, {|x| ~testp[x.div(4)]}));

~sf.close;

~sf.openRead();
~sf.numFrames.postln;
~sf.close;
)
















