# crawlspace

*crawlspace* is a digital music work written in SuperCollider and designed for any live concert situation. The basic idea is that the program runs during an entire concert, listening and updating its sound canvas. The resulting file is then played back as the final piece of the program. crawlspace is designed to be scalable, modular, and versatile.

The code itself is divided into three major components:

1. SamplePainter, a modular, open framework for algorithmic and stochastic audio creation that works by painting segments of sound into an initially empty, fixed-length audio file.
2. CrawlMapper, which drives SamplePainter with generative patterns inspired by the open-source game Dungeon Crawl
3. CrawlEar, the "ear" that splices and analyzes incoming audio and keeps track of it in a database for use by SamplePainter

For more details, see the readmes in the individual component folders.

The first presentation of *crawlspace* is planned for January 6, 2017 in a concert partially organized by Stephen Marrotto.

## ideas for far future development

+ visualization of some kind (graphic score or video)
+ allowance for interaction with musicians
