package org.realityforge.replicant.example.server.service.tyrelll.replicate;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.ejb.EJB;
import javax.interceptor.Interceptor;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.realityforge.replicant.example.server.entity.TyrellPersistenceUnit;
import org.realityforge.replicant.server.EntityMessage;
import org.realityforge.replicant.server.ee.AbstractReplicationInterceptor;

@Interceptor
@Replicate
public class ReplicationInterceptor
  extends AbstractReplicationInterceptor
{
  @EJB
  private ChangeQueue _changeQueue;

  @PersistenceContext( unitName = TyrellPersistenceUnit.NAME )
  private EntityManager _entityManager;

  @Override
  protected void saveEntityMessages( @Nonnull final Collection<EntityMessage> messages )
  {
    _changeQueue.saveEntityMessages( messages );
  }

  @Override
  protected EntityManager getEntityManager()
  {
    return _entityManager;
  }
}
