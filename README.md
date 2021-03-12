APISJ / APIS Core / APIS Core for Docker
================

[![Docker Stats](http://dockeri.co/image/apisplatform/apisj)](https://hub.docker.com/r/apisplatform/apisj/)

Docker image to run APIS Core node in a container.

Supports APIS Masternode & Mining & RPC

[한국어판](https://gist.github.com/Oxchild/c146887ed71ab5a3eda3197cfdb72af5)

Index
------------
[1. Recommended System Requirements](#recommended-system-requirements)

[2. Recommended VPS Products](#recommended-vps-products)

[3. Setting Up With Docker Image](#setting-up-with-docker-image)

[4. Setting Up With Preloaded Docker Image](#setting-up-with-preloaded-docker-image)

[5. Setting Up With Preloaded APIS AMI](#setting-up-with-preloaded-apis-ami)

[6. Starting APIS Core](#starting-apis-core)

[7. Updating APIS Core](#updating-apis-core)

[8. Updating APIS Core to Preloaded APIS Core](#updating-apis-core-to-preloaded-apis-core)

[9. Removing APIS Core](#removing-apis-core)

[10. Roadmap](#roadmap)

[11. Credits](#credits)


Recommended System Requirements
------------

* PC, cloud platform instance, or VPS with Ubuntu OS and Docker installed.
* Stable network with average speed of 400kbps
* Running Ubuntu 16.04 LTS or later
* Processor performance equivant to 1 vCPU (AWS)
* At least 2 GB of RAM
* At least 50 GB of storage to store the block chain files

Recommended VPS Products
------------

VPS Provider Name | Service Name | System Spec
---------|--------------|------------
Amazon Web Services (AWS) | t2.small (EC2) | 1 CPU / 2GB RAM
Microsoft Azure | D1 (Linux VM) | 1 Core / 3.5GB RAM / 60GB Storage
Google Cloud Platform | n1-standard-1 (Compute Engine) | 1 vCPU / 3.75GB RAM
Vultr | VC2 | 1 vCPU / 2048MB RAM

Setting Up With Docker Image
-----------------------

Anyone can pull APIS Core docker image for Ubuntu 16.04 LTS machines with this command :

    $ sudo docker pull apisplatform/apisj
    ...
    Status: Downloaded newer image for apisplatform/apisj:latest

    $ cd ~
    $ mkdir apisData
    $ sudo docker run --net=host -it --name apisj -v ~/apisData:/apis/apisData apisplatform/apisj /bin/bash
    ...
    root@abcdef012345:/#
After this step, you can manage your own APIS Core docker container with downloaded image.

You can also see ~/apisData directory which includes blockchain data and your keystore data.


Setting Up With Preloaded Docker Image
-----------------------

For everyone who needs fast sync, we made option to get pre-synced version.

Anyone can pull Pre-Synced APIS Core docker image for Ubuntu 16.04 LTS machines with this command :

    $ sudo docker pull apisplatform/apisj:preloaded
    ...
    Status: Downloaded newer image for apisplatform/apisj:preloaded

    $ cd ~
    $ mkdir apisData
    $ sudo docker run --net=host -it --name apisj -v ~/apisData:/apis/apisData apisplatform/apisj:preloaded /bin/bash
    ...
    root@abcdef012345:/#
After this step, you can manage your own Pre-Synced APIS Core docker container with downloaded image.

Setting Up With Preloaded APIS AMI
-----------------------

We provide Preloaded APIS AMI(Amazon Machine Image)

Anyone can use APIS Preloaded AMI from Amazon Marketplace. Just create Amazon EC2 instance and use APIS Preloaded AMI.

On AWS instance with APIS Preloaded AMI, just use following command :

    $ sudo docker restart apisj
    $ sudo docker attach apisj
    ...
    root@abcdef012345:/#
After this step, you can manage your own Pre-Synced APIS Core docker container with downloaded image.

Starting APIS Core
-----------

1. Run `apis-core` script with `sh` command

        root@abcdef012345:/# sh ./apis-core

2. APIS Core setup panel will be shown

        APIS Core Settings ==========
        v0.8.820

        [0]  Network             : Mainnet
        [1]  Max Peers           : 30

        [2]  Miner               : -

        [3]  masternode          : -
        [4]  Reward Recipient    : -

        [5]  RPC(WS) Enabled     : false
        [6]  RPC(WS) Port        : 44445
        [7]  RPC(WS) Max Connections: 1
        [8]  RPC(HTTP) Enabled   : false
        [9]  RPC(HTTP) Port      : 44446
        [A]  RPC(HTTP) nThreads  : 8
        [B]  RPC ID              : d4d761908d76981d5a4a8a7b8b5902a3
        [C]  RPC Password        : ec176fe186bee83944a9d848c4855d31
        [D]  RPC Allowed IP      : 127.0.0.1

        [E]  Update APIS Core    : 0.8.820 => 0.8.810

        Input other key to start APIS Core
        Or if no key is entered, APIS Core will start automatically after 10 seconds.
        >> 


3. [Optional] Setting up PoS mining config (Press '2')

        You can get rewards through APIS Block mining.
        You should input Private key of a miner to start mining.
        The chance of getting reward goes higher with the registered miner's balance.
        --
        Miner :  Not set
        --

        --
        [1]  Select miner from locked private key file
        [2]  Deactivate mining (Clear miner setting)
        [3]  Done                
        >> 

4. Loading miner wallet (Importing private key from PC wallet is recommended)

        Which address would you like to mining?

        [A]  Generate a new private key
        [B]  Import private key  
        [C]  Cancel              

        >> B
        Please input your Private key(Hex).
        >> d4e68558977bc0aaaaeced983c574bffd0e2d98123a7a687adbba58c8fbfffff

5. Select miner wallet

        Which address would you like to mining?

        [A]  Generate a new private key
        [B]  Import private key  
        [C]  Cancel              

        [1]  9f47869b3a469c27d8b3069c9a9fb2deb294580d (0 APIS)
        >> 1
        Please enter the password of [9f47869b3a469c27d8b3069c9a9fb2deb294580d]
        >> 

        ...

        You can get rewards through APIS Block mining.
        You should input Private key of a miner to start mining.
        The chance of getting reward goes higher with the registered miner's balance.
        --
        Miner :  9f47869b3a469c27d8b3069c9a9fb2deb294580d (0 APIS)
        --

        --
        [1]  Select miner from locked private key file
        [2]  Deactivate mining (Clear miner setting)
        [3]  Done                
        >> 

6. [Optional] Setting up Masternode wallet (Press '3')

        APIS Core Settings ==========
        v0.8.820

        [0]  Network             : Mainnet
        [1]  Max Peers           : 30

        [2]  Miner               : 9f47869b3a469c27d8b3069c9a9fb2deb294580d (0 APIS)

        [3]  masternode          : -
        [4]  Reward Recipient    : -

        [5]  RPC(WS) Enabled     : false
        [6]  RPC(WS) Port        : 44445
        [7]  RPC(WS) Max Connections: 1
        [8]  RPC(HTTP) Enabled   : false
        [9]  RPC(HTTP) Port      : 44446
        [A]  RPC(HTTP) nThreads  : 8
        [B]  RPC ID              : d4d761908d76981d5a4a8a7b8b5902a3
        [C]  RPC Password        : ec176fe186bee83944a9d848c4855d31
        [D]  RPC Allowed IP      : 127.0.0.1

        [E]  Update APIS Core    : 0.8.820 => 0.8.810

        Input other key to start APIS Core

        >> 3

7. Loading & Selecting Masternode wallet

        Which address would you like to masternode?

        [A]  Generate a new private key
        [B]  Import private key  
        [C]  Cancel              

        [1]  9f47869b3a469c27d8b3069c9a9fb2deb294580d (0 APIS)
        >> 1
        Please enter the password of [9f47869b3a469c27d8b3069c9a9fb2deb294580d]
        >> 

        ...

        You should input Private key of a masternode to staking.
        The balance of the Masternode must be exactly 50,000, 200,000, and 500,000 APIS.
        --
        Masternode :  9f47869b3a469c27d8b3069c9a9fb2deb294580d (0 APIS)
        --

        [1]  Select masternode from locked private key file
        [2]  Deactivate masternode (Clear masternode & recipient setting)
        [3]  Done                
        >> 

8. Selecting Masternode receipent

        You should input address of a recipient to receive the Masternode's reward        instead.
        --
        Recipient :  Not set
        --

        [1]  Select recipient from locked private key file
        [2]  Deactivate masternode (Clear masternode & recipient setting)
        [3]  Done                
        >> 

        ...

        You should input address of a recipient to receive the Masternode's reward instead.
        --
        Recipient :  9f47869b3a469c27d8b3069c9a9fb2deb294580d (0 APIS)
        --

        [1]  Select recipient from locked private key file
        [2]  Deactivate masternode (Clear masternode & recipient setting)
        [3]  Done                
        >> 

9. Set RPC config and Update if needed

        [5]  RPC(WS) Enabled     : false
        [6]  RPC(WS) Port        : 44445
        [7]  RPC(WS) Max Connections: 1
        [8]  RPC(HTTP) Enabled   : false
        [9]  RPC(HTTP) Port      : 44446
        [A]  RPC(HTTP) nThreads  : 8
        [B]  RPC ID              : d4d761908d76981d5a4a8a7b8b5902a3
        [C]  RPC Password        : ec176fe186bee83944a9d848c4855d31
        [D]  RPC Allowed IP      : 127.0.0.1

        [E]  Update APIS Core    : 0.8.820 => 0.8.810

Press other keys to start APIS Core
-------------

    20:45:27.428 INFO  [ApisFactory.java:62]	  Starting APIS...
    20:45:29.421 INFO  [Initializer.java:49]	  Running apis-mainnet.json, core version: ...
    ...

Updating APIS Core
-----------
1. Stop apis core in docker container by hitting `CTRL + C`.
2. Stop docker container by typing `exit`.

        root@abcdef012345:/# exit
        exit

        username@ubuntu:~$

3. Remove old container (Keystore and block data **will not be deleted** but we recommend backing up files)

        $ sudo docker rm apisj

4. Pull updated docker image from docker hub and restart your container.

        $ sudo docker pull apisplatform/apisj
        $ sudo docker run --net=host -it --name apisj -v ~/apisData:/apis/apisData apisplatform/apisj /bin/bash
        ...
        root@abcdef012345:/#

5. Restart apis-core

        root@abcdef012345:/# sh apis-core

Updating APIS Core to Preloaded APIS Core
-----------
1. Stop apis core in docker container by hitting `CTRL + C`.
2. Stop docker container by typing `exit`.

        root@abcdef012345:/# exit
        exit

        username@ubuntu:~$

3. Remove old container (Keystore and block data **will not be deleted** but we recommend backing up files)

        $ sudo docker rm apisj

4. Pull updated docker image from docker hub and restart your container.

        $ sudo docker pull apisplatform/apisj:preloaded
        $ sudo docker run --net=host -it --name apisj -v ~/apisData:/apis/apisData apisplatform/apisj:preloaded /bin/bash
        ...
        root@abcdef012345:/#

5. Restart apis-core

        root@abcdef012345:/# sh apis-core


Removing APIS Core
-----------
1. Stop apis core in docker container by hitting `CTRL + C`.
2. Stop docker container by typing `exit`.

        root@abcdef012345:/# exit
        exit

        username@ubuntu:~$

3. Remove old container (Keystore and block data **will not be deleted** but we recommend backing up files)

        $ sudo docker rm apisj

4. Remove `apisData` directory

        $ sudo docker rm apisj


Roadmap
-------

**Project Nile**

>Project Nile is a long-term, business-oriented project that contains detailed plan to construct better mainnet ecosystem by supporting third-party development on APIS Mainnet and to create various business opportunities using APIS Mainnet and infrastructure with better scalablity.

**Project Nile - Mainnet Scalablity**
1. Establishing **APIS Labs**
2. Releasing **APIS Open API**
3. APIS Labs opens APIS dev. community
4. APIS Open API Beta starts
5. **APIS Smart contract IDE** Beta open
6. Delivering **AC/DC (APIS Code/DApp Convention)** consensus of core contracts
7. APIS Open API official launch
8. AC/DC confirm, disclosure
9. AC/DC suggestion system open
10. APIS smart contract IDE official launch
11. Launch Enterprise APIS Open API

**Project Nile - Business Opportunity**
1. Releasing **On-demand APIS Masternode platform**
2. Releasing **On-chain Asset / Exchange**
3. APIS Labs opens **DApp Incubator program**
4. On-chain Asset launch
5. On-chain Exchange (DEX) launch
6. On-demand APIS Masternode platform Beta open
7. Releasing APIS Masternode platform for firms
8. Disclose **APIS Hashpower platform**
9. Partnership as APIS Masternode platform for firms 
10. Disclosing APIS hash power platform partner
11. **SPHINX** licensing and partnership
12. APIS hash power platform Beta open
13. Additional listing of On-chain Exchange(DEX) 
14. Official launch of On-demand APIS Masternode platform
15. Releasing APIS Hashpower platform 
16. Releasing APIS Masternode platform for enterprises


Credits
-------

APIS Development Team & The Oxchild Pte.Ltd.

Document written by Ryan.
