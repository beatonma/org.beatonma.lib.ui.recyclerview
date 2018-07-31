# RecyclerView
## org.beatonma.lib.ui.recyclerview

**This module is not intended for 3rd party use**, although you are free to use any parts of it
you like (with attribution). Be aware that APIs are subject to change without notice and
support will be limited.

This module is part of a larger group of libraries that I use via a private Artifactory repository.
It is not currently available on any public repositories.

The parent project holds most of the build configuration but is not available publicly. If you
really want to build this module you will need to write your own `build.gradle` configuration.
The dependencies you need to include can be found listed in `dependencies.txt`, but some of those
are private too so I'm afraid you will need to go through the same process for each module - like
I said, not intended for 3rd party use!

----

This module aims to remove boilerplate code when working with RecyclerView and associated components.

### Examples
recyclerView.setup(adapter)
recyclerView.setupGrid(adapter, targetColumnWidth)

EmptyBaseRecyclerViewAdapter automatically shows loading view when data is null, or empty data
message when data is empty. Loading view layout can be set in project theme so that all
RecyclerViews use the same animation when loading data.
