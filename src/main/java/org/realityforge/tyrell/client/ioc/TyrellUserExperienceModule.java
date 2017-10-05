package org.realityforge.tyrell.client.ioc;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.google.web.bindery.event.shared.EventBus;
import javax.annotation.Nonnull;
import javax.inject.Singleton;
import org.realityforge.gwt.lognice.LoggingEventBus;
import org.realityforge.replicant.client.EntityRepository;
import org.realityforge.replicant.client.gwt.LocalCacheService;
import org.realityforge.replicant.client.runtime.DataLoaderEntry;
import org.realityforge.replicant.client.transport.CacheService;
import org.realityforge.tyrell.client.GlobalAsyncCallback;
import org.realityforge.tyrell.client.net.TyrellGwtDataLoaderListener;
import org.realityforge.tyrell.client.net.TyrellGwtDataLoaderService;

public class TyrellUserExperienceModule
  extends AbstractGinModule
{
  @Override
  protected void configure()
  {
    bindNamedService( "GLOBAL", AsyncCallback.class, GlobalAsyncCallback.class );
    bind( CacheService.class ).to( LocalCacheService.class ).asEagerSingleton();
    bind( EventBus.class ).to( LoggingEventBus.class ).asEagerSingleton();
  }

  private <T> void bindNamedService( final String name,
                                     final Class<T> service,
                                     final Class<? extends T> implementation )
  {
    bind( service ).annotatedWith( Names.named( name ) ).to( implementation ).asEagerSingleton();
  }

  @Provides
  @Singleton
  public final DataLoaderEntry[] getDataLoaderEntries( @Nonnull final com.google.gwt.event.shared.EventBus eventBus,
                                                       @Nonnull final EntityRepository repository,
                                                       @Nonnull final TyrellGwtDataLoaderService dataLoaderService )
  {
    final DataLoaderEntry[] dataLoaders =
      {
        new DataLoaderEntry( dataLoaderService, true )
      };
    dataLoaderService.addDataLoaderListener( new TyrellGwtDataLoaderListener( repository, eventBus ) );
    return dataLoaders;
  }
}
