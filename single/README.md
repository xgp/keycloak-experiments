# single node

A Keycloak image meant to be run as a single node (not clustered) that is resource constrained, but meant to maintain DB and cache state after restart.

- Runs the h2 database
  - Runs the periodic backup extension
  - Mounts a volume for the data dir. Should be backed up with a snapshot capable backup mechanism.
  - Mounts a volume for the backup dir.
    - Rotates files in the backup dir so that we only have a fixed #.

- Sets up persistence for Infinispan
  - Turns off jgroups for clustering Infinispan
  - Persistence writes to file/rocksdb. (need to find out why rocksdb is deprecated)
  - Mounts a volume for the cache dir.
  - Passivation set to keep minimal entries in RAM

- Contains Caddy so that SSL is done automatically
  - Letsencrypt
  - Don't use Keycloak/Quarkus SSL. proxy=edge instead

- Scripts to
  - Recover on restart from h2 corruption.
    - Run validation to detect h2 state before Keycloak start.
    - Automatically recover from the most recent zip backup if corrupt data files.
    - Compact the data files on startup https://www.h2database.com/html/features.html#compacting
  - New entrypoint for keycloak to strip out env vars?
    
- Runs supervisord
  - So we can do it in a single container

## Volume layout

In a single `/data` dir, so you can mount the whole thing to a persistent volume that supports snapshots and be done with it. Or, you can mount each one individually.
```
/opt/keycloak/data/
├── backup
├── cache
├── h2
└── log
```