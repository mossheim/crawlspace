
(
~allchs = [];
7.do {
	|index|
	~labels = [ 'pcile25', 'pcile50', 'pcile75', 'pcile90', 'flatness', 'centroid', 'ampl', 'ampl2', 'sig.left' ];
	~sf = SoundFile.openRead("test_audio".resolveRelative +/+ "bufwrite_test_%.wav".format(index));
	~sf.numChannels;
	~sf.numFrames;

	~ds = 128*16;
	~frame = FloatArray.newClear(~sf.numChannels);
	~numframes = ~sf.numFrames.div(~ds);
	~chs = {FloatArray.newClear(~numframes)}!8;

	~numframes.do {
		|i|
		~sf.readData(~frame);
		for(0, ~chs.size-1) {
			|j|
			~chs[j][i] = ~frame[j];
		};
		if(i % 1000 == 0) {
			postln(i/~chs[0].size);
		};
		~sf.seek(~ds-1,1);
	};
	~sf.close;
	~allchs = ~allchs.add(~chs);
};
)

~reduced = ~allchs.flop.collect(_.reduce('++'))

~reducedlog = ~reduced.collect({|arr, i| if([0,1,2,3,5].includes(i)) {arr.reject(_==0).log2} {Array.newFrom(arr)} });

~reducedloginfo = ~reducedlog.collect({|arr,i| arr = arr.reject(_==0); [~labels[i], arr.minItem, arr.maxItem, arr.mean, arr.stdDev]})

~reducedloginfo.do({|arr| arr.collect({|x| if(x.isKindOf(Number)) {x.round(0.0001)} {x}}).postln})

~reducedlogsmooth = ~reducedlog.collect(~smooth.(_,3))

~reducedlogsmoothinfo = ~reducedlogsmooth.collect({|arr,i| arr = arr.reject(_==0); [~labels[i], arr.minItem, arr.maxItem, arr.mean, arr.stdDev]})

~reducedlogsmoothinfo.do({|arr| arr.collect({|x| if(x.isKindOf(Number)) {x.round(0.0001)} {x}}).postln});

~reducedlogsmoothderiv = ~reducedlogsmooth.collect(_.differentiate).collect(_[1..])

~reducedlogsmoothderiv.collect(_.abs).collect(_[1000..200]).plot

~reducedlogderiv = ~reducedlog.collect(_.differentiate).collect(_[1..])

~reducedlogderiv.collect(_.abs).collect(_[1000..2000]).plot

~reducedlogderiv.collect({|arr| arr.abs.sort.keep(-100)}).plot
~reducedlogderiv.collect({|arr| arr.abs.sort[arr.size*[0.95450,0.98758,0.99730,0.99953, 0.99994]]}).round(0.0001).do(_.postln)

~reducedlogsmoothderiv.collect({|arr| arr.abs.sort[arr.size*[0.95450,0.98758,0.99730,0.99953, 0.99994]]}).round(0.0001).do(_.postln)

(
~smooth = {
	|arr, n|
	var res = Array.newClear(arr.size-n+1);
	var sum = arr[0..(n-1)].sum;
	for(0, arr.size-n) {
		|i|
		res[i] = sum/n;
		sum = sum - arr[i] + (arr[i+n]?0);
	};
	res;
}
)

~smooth.([0,0.2,0,3,0,5],3)

~chs.plot
~chslog = ~chs.collect({|arr, i| if([0,1,2,3,5].includes(i)) {arr.reject(_==0).log2} {Array.newFrom(arr)} })

~chslog2 = ~chslog.collect({|arr,i| [~labels[i], arr.minItem, arr.maxItem, arr.mean, arr.stdDev]})
~chslog2.do(_.postln)


~chs2 = ~chs.collect({|arr,i| arr = arr.reject(_==0); [~labels[i], arr.minItem, arr.maxItem, arr.mean, arr.stdDev]})
~chs2.do(_.postln)
~ch.plot
~ch.count(_==0)
~ch.count
~ch.log2.plot

~ch2 = ~ch.reject(_==0)
~ch2.minItem
~ch2.log2.plot