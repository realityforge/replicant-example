package org.realityforge.replicant.example.server.service.tyrell.replicate;

import javax.ejb.EJB;
import javax.interceptor.Interceptor;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.realityforge.replicant.example.server.entity.TyrellPersistenceUnit;
import org.realityforge.replicant.server.EntityMessageEndpoint;
import org.realityforge.replicant.server.ee.AbstractReplicationInterceptor;

@Interceptor
@Replicate
public class ReplicationInterceptor
  extends AbstractReplicationInterceptor
{
  @EJB
  private EntityMessageEndpoint _endpoint;

  @PersistenceContext( unitName = TyrellPersistenceUnit.NAME )
  private EntityManager _entityManager;

  @Override
  protected EntityMessageEndpoint getEndpoint()
  {
    return _endpoint;
  }

  @Override
  protected EntityManager getEntityManager()
  {
    return _entityManager;
  }
}
