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
  - HOSTNAME env var
  - Don't use Keycloak/Quarkus SSL. proxy=edge instead

- Scripts to
  - Recover on restart from h2 corruption.
    - Compact the data files on startup
      - https://www.h2database.com/html/features.html#compacting
      - https://github.com/h2database/h2database/blob/master/h2/src/test/org/h2/samples/Compact.java
    - Run validation to detect h2 state before Keycloak start. (implicit in compaction)
      - Automatically recover from the most recent zip backup if corrupt data files. (`org.h2.tools.Restore`)
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

## Notes

- Why not postgres?
- Try doing this with ubi-micro https://github.com/keycloak/keycloak/blob/main/docs/guides/server/containers.adoc#installing-additional-rpm-packages
```
FROM registry.access.redhat.com/ubi9 AS ubi-micro-build
RUN mkdir -p /mnt/rootfs
RUN dnf install --installroot /mnt/rootfs <package names go here> --releasever 9 --setopt install_weak_deps=false --nodocs -y; dnf --installroot /mnt/rootfs clean all

FROM quay.io/keycloak/keycloak
COPY --from=ubi-micro-build /mnt/rootfs /
```
