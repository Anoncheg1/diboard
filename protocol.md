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


Web-fronted message filtrating
-----------------
Suggestion how to filter empty articles:
If Message is empty there is must be Subject and Attachment.
If Message is not empty: for thread there is must Subject or Attachment,
for replay Message is enough.


new replay:
```
1 message.length() != 0
2 subject.length() != 0
3 file.getSize() != 0
( 1 or (2 and 3) ) or (2 and 3)
( 1 or (2 and 3) )
-( 1 and (2 or 3) )    negative form for "== 0"
```

new thread:
```
1 message.length() == 0
2 subject.length() == 0
3 file.getSize() == 0
( 1 AND (2 OR 3) ) OR (2 AND 3) 
```