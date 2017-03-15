Protocol
==========
See [nntpchan protocol](https://github.com/majestrate/nntpchan/blob/master/doc/developer/protocol.md)

Message-ID
-----------
Only minimal character set allowed, anything else will be rejected:
<[\w.)]+@[\w.-]+> - < 0-9, A-Z, a-z, dot @ 0-1, A-Z, a-z, -, dot >

Proposed new command NEWTHREADS to getting missing thread and pull at start
-----------
To get thread we utilize NEWTHREADS command with date of replay or message-id of thread.
It is much comfortable than using XOVER 0 command.

New format:
```
first form:
NEWTHREADS wildmat UnixTime
second form:
NEWTHREADS wildmat message-id
```
Where UnixTime in seconds. Example:
```
NEWTHREADS news,sci 1464210306
```

NEWTHREADS compare time with thread last_post_time (thread.last_post_time >= ?) for which the condition is satisfied returned fully ordered by time in following form:
```
230    List of new threads follows (multi-line)
thread-message-id TAB TAB epochtime
replay-message-id TAB thread-message-id TAB epochtime
.
```
Note: threads return fully.
