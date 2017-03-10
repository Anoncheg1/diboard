Protocol
==========
See [nntpchan protocol](https://github.com/majestrate/nntpchan/blob/master/doc/developer/protocol.md)

Message-ID
-----------
Only minimal character set allowed, anything else will be rejected:
<[\w.)]+@[\w.-]+> - < 0-9, A-Z, a-z, dot @ 0-1, A-Z, a-z, -, dot >

new command NEWTHREADS to getting missing thread and pull at start
-----------
To get thread we utilize NEWTHREADS command with date of replay.


New format:
```
NEWTHREADS wildmat UnixTime
```
Where UnixTime in seconds. Example:
```
NEWTHREADS news,sci 1464210306
```

NEWTHREADS compare time with thread last_post_time (thread.last_post_time >= ?) for which the condition is satisfied returned fully ordered by time in following form:
```
230    List of new threads follows (multi-line)
thread-message-id
replay-message-id (\tab or \space) thread-message-id
.
```
Note: threads return fully.
