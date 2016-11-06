# CrawlMapper

This is the real meat and bones of *crawlspace*. The way I think about it is that `CrawlMapper` is the thing that makes the paint-by-stochastic-numbers diagram that `SamplePainter` uses as a guide. In this case, the numbers are more like tags, small instructions that `SamplePainter` can interpret in a sort of semantic sense. A bit like "paint a cluster of trees there", "draw a winding river here".

As mentioned elsewhere, the primary inspiration for `CrawlMapper` is Stone Soup Dungeon Crawl, one of my absolute favorite video games and an excellent open source project.
