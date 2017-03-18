Protocol
==========
See [nntpchan protocol](https://github.com/majestrate/nntpchan/blob/master/doc/developer/protocol.md)

Message-ID
-----------
Only minimal character set allowed, anything else will be rejected:
<[\w.)]+@[\w.-]+> - < 0-9, A-Z, a-z, dot @ 0-1, A-Z, a-z, -, dot >

Proposed new command THREAD and THREADS to get missing thread and pull threads at start
-----------
```
THREAD message-id
THREADS wildmat num
```
Parameters

	wildmat    Newsgroups of interest
	
	num is required number of threads with last date