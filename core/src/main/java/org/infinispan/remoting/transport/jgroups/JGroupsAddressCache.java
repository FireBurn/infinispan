package org.infinispan.remoting.transport.jgroups;

import org.infinispan.commons.equivalence.AnyEquivalence;
import org.infinispan.commons.util.concurrent.jdk8backported.EquivalentConcurrentHashMapV8;
import org.infinispan.topology.PersistentUUID;
import org.jgroups.Address;
import org.jgroups.util.ExtendedUUID;
import org.jgroups.util.UUID;

/**
 * Cache JGroupsAddress instances
 *
 * @author Dan Berindei
 * @since 7.0
 */
public class JGroupsAddressCache {
   private static final EquivalentConcurrentHashMapV8<org.jgroups.Address, JGroupsAddress> addressCache =
         new EquivalentConcurrentHashMapV8<>(AnyEquivalence.getInstance(), AnyEquivalence.getInstance());
   private static final EquivalentConcurrentHashMapV8<org.jgroups.Address, PersistentUUID> persistentUUIDCache =
         new EquivalentConcurrentHashMapV8<>(AnyEquivalence.getInstance(), AnyEquivalence.getInstance());

   // HACK: Avoid the org.jgroups.Address reference in the signature so that local caches can work without the jgroups jar.
   // Otherwise, instantiating the JGroupsAddress externalizer will try to load the org.jgroups.Address class.
   static org.infinispan.remoting.transport.Address fromJGroupsAddress(Object address) {
      final Address jgAddress = (Address) address;
      // New entries are rarely added added after startup, but computeIfAbsent synchronizes every time
      JGroupsAddress ispnAddress = addressCache.get(jgAddress);
      if (ispnAddress != null) {
         return ispnAddress;
      }
      return addressCache.computeIfAbsent(jgAddress, uuid -> {
         if (jgAddress instanceof ExtendedUUID) {
            return new JGroupsTopologyAwareAddress((ExtendedUUID) jgAddress);
         } else {
            return new JGroupsAddress(jgAddress);
         }
      });
   }

   public static void putAddressPersistentUUID(Object address, PersistentUUID localUUID) {
      final Address jgAddress = ((JGroupsAddress) address).address;
      persistentUUIDCache.put(jgAddress, localUUID);
   }

   public static PersistentUUID getPersistentUUID(Object address) {
      final Address jgAddress = ((JGroupsAddress) address).address;
      return persistentUUIDCache.get(jgAddress);
   }

   static void pruneAddressCache() {
      // Prune the JGroups addresses & LocalUUIDs no longer in the UUID cache from the our address cache
      addressCache.forEachKey(Integer.MAX_VALUE, address -> {
         if (UUID.get(address) == null) {
            addressCache.remove(address);
            persistentUUIDCache.remove(address);
         }
      });
   }
}
