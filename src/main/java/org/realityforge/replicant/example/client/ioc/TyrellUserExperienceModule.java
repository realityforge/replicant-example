package org.realityforge.replicant.example.client.ioc;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.name.Names;
import com.google.web.bindery.event.shared.EventBus;
import org.realityforge.gwt.lognice.LoggingEventBus;
import org.realityforge.replicant.client.transport.CacheService;
import org.realityforge.replicant.client.gwt.LocalCacheService;
import org.realityforge.replicant.example.client.GlobalAsyncCallback;

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
}