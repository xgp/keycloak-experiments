

### Why is Infinispan the bane of Keycloak adoption?

"You know that you will lose sessions on restart and upgrade."

"Wait. WHAT?!?"

Infinispan uses JGroups for internal communication. When JGroups or Infinispan is upgraded, new cluster members cannot be added because of protocol incompatibility. This effects adding nodes of different versions of Keycloak, and when using external Infinispan.

The recommended(?) workaround is to use external Infinispan. This has the advantage of decoupling Keycloak upgrades from Infinispan/JGroups protocol incompatibility. It also has the side benefit of allowing Keycloak to start faster. The downside is increased cluster/deployment complexity.

It may be necessary to upgrade external Infinispan, either because of vulnerability or HotRod protocol incompatibility. Upgrading external Infinispan has the same problem as Keycloak, as it is also dependent on JGroups and version changes produce incompatibility. The workaround for that is to use the [Infinispan kubernetes operator](https://infinispan.org/docs/infinispan-operator/main/operator.html#cluster-upgrades_upgrading-clusters) to do an upgrade which creates a parallel cluster, creates a remote cache store with that cluster that is the target of the "old" cluster, and then redirecting clients when the "new" cluster has fully replicated the "old" cluster data.


### Workaround 

- Make all currently "distributed" caches into "local" caches.
- Turn off Infinispan/JGroups discovery and clustering. Each Keycloak node thinks it's a singleton.
- Make a JDBC cache store for previously "distributed" caches, where all entries are stored immediately (i.e. set passivation=false). This store is shared by all Keycloak nodes.

This seems to solve both the JGroups communication incompatibility (by eliminating it) and 

#### Problems

- Keycloak uses `Externalizer` for some of it's keys. We had to write a `Key2StringMapper` for those.
- Key length can be long, so the id/key in the database has to be `VARCHAR(1024)`
- While we haven't encountered it yet, there may be collisions if 2 nodes are trying to read/write the same key at the same time.
- More local memory consumption
- Lots of database reads/writes
- Simultaneous startup with multiple Keycloaks may fail if they try to create the `ispn_*` tables at exactly the same time
