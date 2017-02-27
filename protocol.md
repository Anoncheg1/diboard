Protocol
==========
See [nntpchan protocol](https://github.com/majestrate/nntpchan/blob/master/doc/developer/protocol.md)


Getting missing thread with NEWNEWS command
-----------
To get thread we utilize NEWNEWS command with date of replay.


New format:
```
NEWNEWS wildmat UnixTime
```
Where UnixTime in seconds. Example:
```
NEWNEWS news,sci 1464210306
```

Old format supported:
```
NEWNEWS wildmat date time [GMT]
```
Parameters
 *    wildmat    Newsgroups of interest
 *    date       Date in yymmdd or yyyymmdd format
 * 	  time       Time in hhmmss format


**Difference in implementation:** NEWNEWS now compare time with thread last_post_time (thread.last_post_time >= ?) for which the condition is satisfied returned fully ordered by time.

Note: threads returned fully.
