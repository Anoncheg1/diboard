[![Build Status](https://travis-ci.org/Anoncheg1/diboard.svg?branch=master)](https://travis-ci.org/Anoncheg1/diboard)

Use: ```$ git clone --recursive --depth=1 https://github.com/Anoncheg1/diboard.git```

diboard v1.0.8 Decentralized ImageBoard just like [nntpchan](https://github.com/majestrate/nntpchan) with following differences:
-----
- scientific research on this topic
- main goal of project is a quality of code
- the least possible amount of third-party libraries
- more accurate with rfc
- philosophy 
![peering philosophy](https://github.com/Anoncheg1/diboard/blob/master/peering%20philosophy.png "peering philosophy")

Structure:
--------

1. Import Existing Maven project diboard
2. Import Existing Maven project dibd - Main core NNTP module that can work independently.
3. Import Existing Maven project webspringboot - Web-frontend. Spring boot with embedded Tomcat.

System Requirements
-----
We are very flexible. 
Min RAM 500MB.
Java 8.

Installation
-----

1. Install RDBMS: postgresql (or mysql)
2. Create database
```
$ su
# su postgres
$ psql
postgres=# CREATE ROLE dibd WITH LOGIN PASSWORD 'mysecret';
postgres=# CREATE DATABASE dibd WITH OWNER dibd ENCODING 'UTF8';
postgres=# \q
$ psql dibd
dibd=# \i /home/user/diboard/dibd.SQL
```
3. configure webspringboot/dibd.conf
4. install ImageMagic (used for thumbnail)
5. Compile
```
$ cd diboard
$ mvn clean compile package
```
6. Run
```
$ cd webspringboot
$ java -jar target/diboard.jar -p 9119
```
To run as a daemon on port 119 see dibd/util

Port 119 may be accessed local for debug:
```
iptables -t nat -I OUTPUT -p tcp -d 127.0.0.1 --dport 119 -j REDIRECT --to-port 9119
```

Features:
----------
- No JavaScript.
- Short links works globally
- No message loss even with too large attachment.
- Fully compatible with nntpchan soft (except tripcodes, AUTH command, decentralized moderation).
- Only images supported for now. One attachment per post.
- SOCKS proxy connection for Tor OR i2p working.
- Flexible control of used disk space and used resources.

Knews issues:
----------
- Impossible to chage HTML templates without recompilation.
- HEAD and BODY NNTP commands do not read NNTPcache.
- For TLS: only DNS names in AltNames Subject field supported.
- It is possible to fake host in message-id and Path.(See "missing threads" in FullDocumentation)
- It is possible to flood/spam from peers, that is why we need networking groups.
- HTTP proxy not supported.

TODO:
----------
1. create new web-frontend with HTML cache
2. replace XOVER with new commands.

For nntpchan:
----------
- make one name for host: web, nntp and in Path header.
- do ping-pong to maintain stream connection.

jar verification:
----------
```
$ gpg2 --import pubkey.asc
$ cd webspringboot/target/
$ gpg --verify diboard.jar.sig diboard.jar
fingerprint 24B8 DB4A 109A 254C 1CD8  1629 BBC5 0968 5FC9 36DB
```