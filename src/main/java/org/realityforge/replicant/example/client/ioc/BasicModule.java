package org.realityforge.replicant.example.client.ioc;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.name.Names;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import org.realityforge.replicant.client.EntityChangeBroker;
import org.realityforge.replicant.client.EntityRepository;
import org.realityforge.replicant.client.EntityRepositoryImpl;
import org.realityforge.replicant.example.client.GlobalAsyncCallback;
import org.realityforge.replicant.example.client.services.TyrellEntityChangeBroker;
import org.realityforge.replicant.example.server.service.tyrelll.replicate.EntityRouter;
import org.realityforge.replicant.server.EntityMessageGenerator;
import tyrell.server.entity.TyrellEntityMessageGenerator;
import tyrell.server.entity.tyrell.TyrellRouter;

public class BasicModule
  extends AbstractGinModule
{
  @Override
  protected void configure()
  {
    bindNamedService( "GLOBAL", AsyncCallback.class, GlobalAsyncCallback.class );
    bind( EntityRepository.class ).to( EntityRepositoryImpl.class ).asEagerSingleton();
    bind( EntityChangeBroker.class ).to( TyrellEntityChangeBroker.class ).asEagerSingleton();
    bind( EntityMessageGenerator.class ).to( TyrellEntityMessageGenerator.class ).asEagerSingleton();
    bind( TyrellRouter.class ).to( EntityRouter.class ).asEagerSingleton();
    bind( EventBus.class ).to( SimpleEventBus.class ).asEagerSingleton();
  }

  private <T> void bindNamedService( final String name,
                                     final Class<T> service,
                                     final Class<? extends T> implementation )
  {
    bind( service ).annotatedWith( Names.named( name ) ).to( implementation ).asEagerSingleton();
  }
}
