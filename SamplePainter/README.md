# SamplePainter

`SamplePainter` is the oldest, most abstract, and most versatile component of *crawlspace*. At present it is simply an empty algorithmic framework with the following steps:

* Create a silent audio file (the output file)
* Main loop:
  * Choose an audio file to cut from
  * Cut a segment from the audio file
  * Perform modifications on the segment
  * Paste the transformed segment into the output file
  
Each step in this process can be implemented to be dependent on previous decisions. In the list below, each item can depend on the previous decisions, as well as the current cycle number. This also reflects the actual sequence of decisions within the main cycle:

- Choose a frame at which to paste the resulting audio
- Choose a source audio file
- Choose a duration to cut
- Choose a location to cut
- For each modification, decide whether or not to apply it
  - Note: it is not possible for these decisions to depend on what happened in the sequence of modifications, other than the direct output of the previously applied modification
- Run the modification function
- Choose a method to use for pasting (examples: replace, add, multiply)
  
## Implementation stack

### 1. `AbstractSamplePainter`

Contains basic methods for the framework, with no implementations. Also includes the `SamplePainterFunc` class and its subclasses `PasteFunc` and `ModifyFunc`, which encapsulate information about the functions that are used to modify the cut audio segment and combine it with existing data in the output file.

### 2. `SimpleSamplePainter`

Provides a basic, functioning product. Most decision-making methods work on uniform distributions.

### 3. `RealTimeSamplePainter`

Allows SamplePainter to easily use Synths to perform modifications. Provides factory methods that allow concise Synth definitions.

### 4. `ThreadedSamplePainter`

Adds "multithreading". Since SC is not a multithreading language, the parallel processing is only happening on the Server. For certain conditions this is a huge benefit, since the next cycles of the program can run their slow real-time processing modifications on the Server alongside the current cycle. In other words, a long audio segment that requires multiple passes on the Server will not make as terrible a bottleneck.
