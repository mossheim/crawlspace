(
~stairs = [];
~min_stair_interval = 3;
~duration = 600;

~stair_possibilities = [[~min_stair_interval, ~duration - ~min_stair_interval]];

~place_stair = {
	var ranges = ~stair_possibilities.collect({|pair| pair.last - pair.first + 1});
	var range_sum = ranges.sum;
	var ranges_integrated = ranges.integrate.addFirst(0);
	var stair_range_pos = range_sum - range_sum.linrand.floor; // choose a number that fits within the sum of all possible ranges
	var stair_range_index = ranges_integrated.indexInBetween(stair_range_pos).floor.asInteger; // returns the index of the possible range within which the choice lies
	var stair_pos = stair_range_pos - ranges_integrated[stair_range_index] + ~stair_possibilities[stair_range_index][0]; // [index][0] is the low end of the current index, [0][0] is the total offset

	// split the interval in question into two halves. if either has a range less than 0, don't add it
	var interval = ~stair_possibilities[stair_range_index];

	~stair_possibilities.removeAt(stair_range_index);
	if((stair_pos+~min_stair_interval) <= interval[1]) {
		~stair_possibilities = ~stair_possibilities.insert(stair_range_index, [stair_pos+~min_stair_interval, interval[1]]);
	};
	if((stair_pos-~min_stair_interval) >= interval[0]) {
		~stair_possibilities = ~stair_possibilities.insert(stair_range_index, [interval[0], stair_pos-~min_stair_interval]);
	};

	stair_pos;
};

18.do {
	var stair1 = ~place_stair.value();
	var stair2 = ~place_stair.value();
	if(stair1 < stair2) {
		~stairs[\down] = ~stairs[\down].add(stair1);
		~stairs[\up] = ~stairs[\up].add(stair2);
	} {
		~stairs[\down] = ~stairs[\down].add(stair2);
		~stairs[\up] = ~stairs[\up].add(stair1);
	}
};

~curr_depth = 1;
~depth_arr = [];
for(0, ~duration-1) {
	|t|
	if(~stairs[\down].includes(t)) {
		~curr_depth = ~curr_depth + 1;
	};
	if(~stairs[\up].includes(t)) {
		~curr_depth = ~curr_depth - 1;
	};
	~depth_arr = ~depth_arr.add(~curr_depth);
};

~depth_arr.plot;

// how to calculate max depth?
// use flat array of [pos, dir] pairs
)