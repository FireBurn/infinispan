package org.infinispan.persistence.spi;

import java.util.concurrent.CompletionStage;

import org.infinispan.commons.api.Lifecycle;
import org.reactivestreams.Publisher;

import io.reactivex.rxjava3.core.Flowable;
import net.jcip.annotations.ThreadSafe;

/**
 * Allows persisting data to an external storage, as opposed to the {@link CacheLoader}.
 *
 * @author Mircea Markus
 * @since 6.0
 * @deprecated since 11.0 replaced by {@link NonBlockingStore}
 */
@ThreadSafe
@Deprecated(forRemoval=true)
public interface CacheWriter<K, V> extends Lifecycle {

   /**
    * Used to initialize a cache loader.  Typically invoked by the {@link org.infinispan.persistence.manager.PersistenceManager}
    * when setting up cache loaders.
    *
    * @throws PersistenceException in case of an error, e.g. communicating with the external storage
    */
   void init(InitializationContext ctx);

   /**
    * Persists the entry to the storage.
    *
    * @throws PersistenceException in case of an error, e.g. communicating with the external storage
    * @see MarshallableEntry
    */
   void write(MarshallableEntry<? extends K, ? extends V> entry);

   /**
    * @return true if the entry existed in the persistent store and it was deleted.
    * @throws PersistenceException in case of an error, e.g. communicating with the external storage
    */
   boolean delete(Object key);

   /**
    * Persist all provided entries to the store in chunks, with the size of each chunk determined by the store
    * implementation. If chunking is not supported by the underlying store, then entries are written to the store
    * individually via {@link #write(MarshallableEntry)}.
    *
    * @param publisher a {@link Publisher} of {@link MarshallableEntry} instances
    * @throws NullPointerException if the publisher is null.
    */
   default CompletionStage<Void> bulkUpdate(Publisher<MarshallableEntry<? extends K, ? extends V>> publisher) {
      return Flowable.fromPublisher(publisher)
            .doOnNext(this::write)
            .ignoreElements()
            .toCompletionStage(null);
   }

   /**
    * Remove all provided keys from the store in a single batch operation. If this is not supported by the
    * underlying store, then keys are removed from the store individually via {@link #delete(Object)}.
    *
    * @param keys an Iterable of entry Keys to be removed from the store.
    * @throws NullPointerException if keys is null.
    */
   default void deleteBatch(Iterable<Object> keys) {
      keys.forEach(this::delete);
   }

   /**
    * @return true if the writer can be connected to, otherwise false
    */
   default boolean isAvailable() {
      return true;
   }
}
