package org.realityforge.replicant.example.server.service.tyrell;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.inject.Inject;
import org.realityforge.replicant.example.server.entity.TyrellGraphEncoder;
import org.realityforge.replicant.example.server.entity.tyrell.Building;
import org.realityforge.replicant.example.server.service.tyrell.replicate.EntityRouter;
import org.realityforge.replicant.example.server.service.tyrell.replicate.Packet;
import org.realityforge.replicant.server.EntityMessage;
import org.realityforge.replicant.server.EntityMessageAccumulator;
import org.realityforge.replicant.server.EntityMessageEndpoint;
import org.realityforge.replicant.server.json.JsonEncoder;
import org.realityforge.ssf.InMemorySessionManager;
import org.realityforge.ssf.SessionManager;

@Singleton
@Local({ EntityMessageEndpoint.class, SubscriptionService.class, SessionManager.class })
public class SubscriptionServiceEJB
  extends InMemorySessionManager<TyrellSessionInfo>
  implements SubscriptionService, EntityMessageEndpoint
{
  private static final Logger LOG = Logger.getLogger( SubscriptionServiceEJB.class.getName() );

  @Inject
  private TyrellGraphEncoder _encoder;

  @Override
  @Nullable
  public String poll( @Nonnull final String clientID, final int lastSequenceAcked )
    throws BadSessionException
  {
    final TyrellSessionInfo session = ensureSession( clientID );
    final Packet packet = session.poll( lastSequenceAcked );
    if ( null != packet )
    {
      return JsonEncoder.encodeChangeSetFromEntityMessages( packet.getSequence(), packet.getChanges() );
    }
    else
    {
      return null;
    }
  }

  @Override
  @Nonnull
  public String subscribeToBuilding( @Nonnull final String clientID, @Nonnull final Building building )
    throws BadSessionException
  {
    final TyrellSessionInfo session = ensureSession( clientID );
    session.registerInterest( building.getID() );
    final LinkedList<EntityMessage> messages = new LinkedList<>();
    _encoder.encodeBuilding( messages, building );
    return JsonEncoder.encodeChangeSetFromEntityMessages( 0, messages );
  }

  @Override
  public void unsubscribeFromBuilding( @Nonnull final String clientID, @Nonnull final Building building )
    throws BadSessionException
  {
    ensureSession( clientID ).deregisterInterest( building.getID() );
  }

  @Override
  public void saveEntityMessages( @Nonnull final Collection<EntityMessage> messages )
  {
    if ( LOG.isLoggable( Level.FINER ) )
    {
      LOG.fine( "EntityMessage messages generated during service execution: " + messages );
    }

    //TODO: Rewrite this so that we add clients to indexes rather than searching through everyone for each change!
    final Map<String, TyrellSessionInfo> sessions = getSessions();
    EntityMessageAccumulator<TyrellSessionInfo> accumulator = new EntityMessageAccumulator<>();
    synchronized ( sessions )
    {
      for ( final EntityMessage message : messages )
      {
        final Map<String, Serializable> routingKeys = message.getRoutingKeys();

        final Integer buildingID = (Integer) routingKeys.get( EntityRouter.BUILDING_KEY );
        if ( null != buildingID )
        {
          for ( final TyrellSessionInfo sessionInfo : sessions.values() )
          {
            if ( sessionInfo.isBuildingInteresting( buildingID ) )
            {
              accumulator.addEntityMessage( sessionInfo, message );
            }
          }
        }
      }
    }

    accumulator.complete();
  }

  @Nonnull
  @Override
  protected TyrellSessionInfo newSessionInfo( @Nonnull final String username )
  {
    return new TyrellSessionInfo( UUID.randomUUID().toString(), username );
  }

  @Nonnull
  private TyrellSessionInfo ensureSession( @Nonnull final String clientID )
    throws BadSessionException
  {
    final TyrellSessionInfo session = getSession( clientID );
    if ( null == session )
    {
      throw new BadSessionException();
    }
    return session;
  }
}
