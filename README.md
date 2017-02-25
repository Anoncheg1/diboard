[![Build Status](https://travis-ci.org/Anoncheg1/diboard.svg?branch=master)](https://travis-ci.org/Anoncheg1/diboard)

To clone use: ```$ git clone --recursive```

Decentralized ImageBoard just like [nntpchan](https://github.com/majestrate/nntpchan) with following differences:
-----
- scientific research on this topic
- main goal of project is a quality of code
- the least possible amount of third-party libraries

Structure:
--------

1. Import Existing Maven project diboard
2. Import Existing Maven project dibd - Main core NNTP module that can work independently.
3. Import Existing Maven project webspringboot - Web-frontend. Spring boot with embedded Tomcat.

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
- Short links works globally
- No message loss, full synchronization.
- Fully compatible with nntpchan soft (except tripcodes, AUTH command, decentralized moderation).
- Only images supported for now.
- One attachment per post.
- SOCKS proxy connection for Tor and i2p peering

Knews bugs:
----------
- Impossible to chage HTML templates without recompilation.
- HEAD and BODY NNTP commands do not read NNTPcache.
- nntpchan peer (without newnews command) will slow down performance if your group have pages < pages of nntpchan
- For TLS: only DNS names in AltNames Subject field supported.
- It is possible to fake host in message-id and Path.(See "partial threads" in FullDocumentation)
- It is possible to flood/spam from peers.
- MIME implemented partially.
- HTTP proxy not supported.

Used libraries with licenses(dibd):
----------
- javax.mail GPL2
- org.im4java LGPL
- Java JDBC 4.2 (JRE 8+) driver for PostgreSQL database. PostgreSQL License

TODO:
----------
1. create new web-frontend with HTML cache
2. make separate implementation of pull and push for fast flow.

For nntpchan:
----------
- solve global and local short links in message.
- solve "partial threads" problem.
- make one name for web and nntp.
