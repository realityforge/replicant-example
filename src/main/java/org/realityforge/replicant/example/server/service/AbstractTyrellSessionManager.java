package org.realityforge.replicant.example.server.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.realityforge.replicant.example.server.entity.TyrellGraphEncoder;
import org.realityforge.replicant.example.server.entity.TyrellRouterImpl;
import org.realityforge.replicant.example.server.entity.TyrellSession;
import org.realityforge.replicant.server.EntityMessage;
import org.realityforge.replicant.server.EntityMessageEndpoint;
import org.realityforge.replicant.server.transport.EntityMessageAccumulator;
import org.realityforge.ssf.InMemorySessionManager;

public abstract class AbstractTyrellSessionManager
  extends InMemorySessionManager<TyrellSession>
  implements EntityMessageEndpoint
{
  private static final Logger LOG = Logger.getLogger( AbstractTyrellSessionManager.class.getName() );

  @Inject
  private TyrellGraphEncoder _encoder;

  protected final TyrellGraphEncoder getEncoder()
  {
    return _encoder;
  }

  @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
  @Override
  public void saveEntityMessages( @Nonnull final Collection<EntityMessage> messages )
  {
    if ( LOG.isLoggable( Level.FINER ) )
    {
      LOG.fine( "EntityMessage messages generated during service execution: " + messages );
    }

    //TODO: Rewrite this so that we add clients to indexes rather than searching through everyone for each change!
    final Map<String, TyrellSession> sessions = getSessions();
    final EntityMessageAccumulator accumulator = new EntityMessageAccumulator();
    synchronized ( sessions )
    {
      for ( final EntityMessage message : messages )
      {
        final Map<String, Serializable> routingKeys = message.getRoutingKeys();

        final Integer rosterID = (Integer) routingKeys.get( TyrellRouterImpl.ROSTER_KEY );
        if ( null != rosterID )
        {
          for ( final TyrellSession session : sessions.values() )
          {
            if ( session.isRosterInteresting( rosterID ) )
            {
              accumulator.addEntityMessage( session.getQueue(), message );
            }
          }
        }
        if ( null != routingKeys.get( TyrellRouterImpl.META_DATA_KEY ) )
        {
          for ( final TyrellSession session : sessions.values() )
          {
            if ( session.isInterestedInMetaData() )
            {
              accumulator.addEntityMessage( session.getQueue(), message );
            }
          }
        }
        if ( null != routingKeys.get( TyrellRouterImpl.ROSTER_LIST_KEY ) )
        {
          for ( final TyrellSession session : sessions.values() )
          {
            if ( session.isInterestedInRosterList() )
            {
              accumulator.addEntityMessage( session.getQueue(), message );
            }
          }
        }
      }
    }

    accumulator.complete();
  }

  @Nonnull
  @Override
  protected TyrellSession newSessionInfo()
  {
    return new TyrellSession( UUID.randomUUID().toString() );
  }

  @Nonnull
  protected final TyrellSession ensureSession( @Nonnull final String clientID )
    throws BadSessionException
  {
    final TyrellSession session = getSession( clientID );
    if ( null == session )
    {
      throw new BadSessionException();
    }
    return session;
  }
}
