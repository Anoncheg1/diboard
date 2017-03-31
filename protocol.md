Protocol
==========
See [nntpchan protocol](https://github.com/majestrate/nntpchan/blob/master/doc/developer/protocol.md)

Message-ID
-----------
Only minimal character set allowed, anything else will be rejected:

```<[\w.)]+@[\w.-]+> - < 0-9, A-Z, a-z, dot, $ @ 0-1, A-Z, a-z, -, dot >```

I have doubts about $ but I hope it will not harm.

Local short links implementations in global NNTP exchanging
-----------
Every nntp-imageboard should map their Short Links to global message-id in the body om message.

if you have >>asd2f in body it should be ```<message-id>```

Proposed new command Proposed new command THREAD and THREADS to get missing thread and pull threads at start(not implemented yet)
-----------
```
THREAD message-id
THREADS wildmat num
```
Parameters

	wildmat    Newsgroups of interest
	
	num is required number of threads with last date
