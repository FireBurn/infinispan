package org.infinispan.jmx;

/**
 * Implementors of this should return an MBeanServer to which MBeans will be registered.
 *
 * @author Mircea.Markus@jboss.com
 * @see org.infinispan.jmx.PlatformMBeanServerLookup
 * @since 4.0
 * @deprecated Use {@link org.infinispan.commons.jmx.MBeanServerLookup} instead
 */
@Deprecated(forRemoval=true)
public interface MBeanServerLookup extends org.infinispan.commons.jmx.MBeanServerLookup {
}
