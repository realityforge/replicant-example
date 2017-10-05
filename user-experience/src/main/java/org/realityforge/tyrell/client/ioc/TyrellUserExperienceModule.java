package org.realityforge.tyrell.client.ioc;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Provides;
import javax.annotation.Nonnull;
import javax.inject.Singleton;
import org.realityforge.gwt.gin.AbstractGinModule;
import org.realityforge.gwt.lognice.LoggingEventBus;
import org.realityforge.replicant.client.EntityRepository;
import org.realityforge.replicant.client.runtime.DataLoaderEntry;
import org.realityforge.tyrell.client.GlobalAsyncCallback;
import org.realityforge.tyrell.client.net.FrontendContext;
import org.realityforge.tyrell.client.net.FrontendContextImpl;
import org.realityforge.tyrell.client.net.TyrellGwtDataLoaderListener;
import org.realityforge.tyrell.client.net.TyrellGwtDataLoaderService;

public class TyrellUserExperienceModule
  extends AbstractGinModule
{
  @Override
  protected void configure()
  {
    bindNamedService( "GLOBAL", AsyncCallback.class, GlobalAsyncCallback.class );
    bind( EventBus.class ).to( LoggingEventBus.class ).asEagerSingleton();
    bindService( FrontendContext.class, FrontendContextImpl.class );
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

  @Provides
  @Singleton
  public final com.google.web.bindery.event.shared.EventBus getSharedEventBus( @Nonnull final com.google.gwt.event.shared.EventBus eventBus )
  {
    return eventBus;
  }
}
