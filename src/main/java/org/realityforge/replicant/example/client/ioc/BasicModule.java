package org.realityforge.replicant.example.client.ioc;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.name.Names;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import org.realityforge.replicant.client.EntityChangeBroker;
import org.realityforge.replicant.client.EntityChangeBrokerImpl;
import org.realityforge.replicant.client.EntityRepository;
import org.realityforge.replicant.client.EntityRepositoryImpl;
import org.realityforge.replicant.example.client.GlobalAsyncCallback;
import org.realityforge.replicant.example.client.services.DataLoaderService;
import org.realityforge.replicant.example.client.services.TyrellDataLoaderService;

public class BasicModule
  extends AbstractGinModule
{
  @Override
  protected void configure()
  {
    bindNamedService( "GLOBAL", AsyncCallback.class, GlobalAsyncCallback.class );
    bind( EntityRepository.class ).to( EntityRepositoryImpl.class ).asEagerSingleton();
    bind( EntityChangeBroker.class ).to( EntityChangeBrokerImpl.class ).asEagerSingleton();
    bind( DataLoaderService.class ).to( TyrellDataLoaderService.class ).asEagerSingleton();
    bind( EventBus.class ).to( SimpleEventBus.class ).asEagerSingleton();
  }

  private <T> void bindNamedService( final String name,
                                     final Class<T> service,
                                     final Class<? extends T> implementation )
  {
    bind( service ).annotatedWith( Names.named( name ) ).to( implementation ).asEagerSingleton();
  }
}
