Config files:

`peers.conf`
`groups.conf`

feeding, peering are synonyms

Requirements:
---------

* groups.conf must declare peers lists per group in format (where '|' is a separating symbol):
host.com|sdg2123.onion|asdasdjlk.i2p|127.0.0.5
* At least one of the peers in list must be declared in peers.conf
* sonews.peering=true


Recomendations:
---------

* Public list of peers per group at the main page. Strongly recommended.
* Watch for changes of that list at your peers.
* Do not join to too large list of peers.
* You must monitor every group of peers.
* If something go wrong in group all that you can do is accept it or disconnect from whole list.
* Do not change id in groups.conf ever. Do not change group name if you have peers.
* Be especially carefull with writing peerhost names in peers.conf and groups.conf. If you made mistake in hostnames there is no warning message for it.

**peers per groups - groups.conf MUST be public**


Possible errors/warning in net of peers:
------------------------
* new peer in group
* max posts per thread have different limit
* hostname/groupname was changed inside peer
* spoofing and spamming
* nntpchan errors

Peering with TLS
----------
- dibd.conf tlsenabled=ture allow_unautorized_post=false
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
