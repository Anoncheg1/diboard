Config files:

`peers.conf`
`groups.conf`

feeding, peering are synonyms

Requirements:
---------

* groups.conf must declare peers lists per group in format (where '|' is a separating symbol):
host.com|sdg2123.onion|asdasdjlk.i2p|127.0.0.5
* At least one of the peers in list must be declared in peers.conf
* dibd.peering=true


Recomendations:
---------

* Be especially carefull with writing peerhost names in peers.conf and groups.conf. If you made mistake in hostnames there is no warning message for it.
* Do not change id in groups.conf ever. Do not change group name if you have peers.
* Public list of peers per group/board at the main page. Strongly recommended.
* Watch for changes of that list at your peers.
* You should monitor every group of peers.
* Do not join to too large list of peers (group/board).
* If something go wrong in group all that you can do is accept it or disconnect from whole list (group/board).
* Time at your host should not differ by the perceived user value.

**peers per groups - groups.conf MUST be public**

nntpchan keep open connection without ping-pong. We close idling connections for DDoS protection after dibd.timeout seconds default is 180.

Possible errors/warning in net of peers:
------------------------
* new peer in group
* if nntpchan is peer there may be replays without threads in XOVER, wrong formatted articles, etc.
* "WARNING Can not create thumbnail" is normal. Ignore it.
* hostname/groupname was changed inside peer
* spoofing and spamming
* max replays per thread have different limit

Peering with TLS
----------
- dibd.conf: dibd.feed=true    tlsenabled=true    allow_unautorized_post=false
- add peer public certificates to peersTLSCertificates folder
- yourhost.crt file is your public certificate
- SelfKeyStore file keeps your private key

To regenerate new public certificate just delete yourhost.crt and SelfKeyStore files.

###Peering over Tor

Install Tor

    apt-get install tor

Make a tor hidden service point from outside port 119 to port 1199
Add to /etc/tor/torrc:

    HiddenServiceDir /var/lib/tor/nntp_feed
    HiddenServicePort 119 127.0.0.1:1199

restart/reload tor then

    cat /var/lib/tor/nntp_feed/hostname

This is your feed address
