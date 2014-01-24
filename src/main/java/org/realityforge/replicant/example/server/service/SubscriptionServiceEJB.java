package org.realityforge.replicant.example.server.service;

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
import org.realityforge.replicant.example.server.entity.Roster;
import org.realityforge.replicant.example.server.entity.TyrellGraphEncoder;
import org.realityforge.replicant.example.server.entity.TyrellRouterImpl;
import org.realityforge.replicant.example.server.entity.dao.RosterRepository;
import org.realityforge.replicant.server.EntityMessage;
import org.realityforge.replicant.server.EntityMessageEndpoint;
import org.realityforge.replicant.server.json.JsonEncoder;
import org.realityforge.replicant.server.transport.EntityMessageAccumulator;
import org.realityforge.replicant.server.transport.Packet;
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

  @Inject
  private RosterRepository _rosterRepository;

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
  public String downloadAll( @Nonnull final String clientID )
    throws BadSessionException
  {
    final TyrellSessionInfo session = ensureSession( clientID );
    return downloadAll( session );
  }

  @Nonnull
  private String downloadAll( @Nonnull final TyrellSessionInfo session )
  {
    final LinkedList<EntityMessage> messages = new LinkedList<>();
    for ( final Roster roster : _rosterRepository.findAll() )
    {
      session.registerInterest( roster.getID() );
      _encoder.encodeRoster( messages, roster );
    }
    return JsonEncoder.encodeChangeSetFromEntityMessages( 0, messages );
  }

  @Override
  @Nonnull
  public String subscribeToAll( @Nonnull final String clientID )
    throws BadSessionException
  {
    final TyrellSessionInfo session = ensureSession( clientID );
    session.registerInterestInAll();
    return downloadAll( session );
  }

  @Override
  @Nonnull
  public String subscribeToRoster( @Nonnull final String clientID, @Nonnull final Roster roster )
    throws BadSessionException
  {
    final TyrellSessionInfo session = ensureSession( clientID );
    session.registerInterest( roster.getID() );
    final LinkedList<EntityMessage> messages = new LinkedList<>();
    _encoder.encodeRoster( messages, roster );
    return JsonEncoder.encodeChangeSetFromEntityMessages( 0, messages );
  }

  @Override
  public void unsubscribeFromRoster( @Nonnull final String clientID, @Nonnull final Roster roster )
    throws BadSessionException
  {
    ensureSession( clientID ).deregisterInterest( roster.getID() );
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
    final EntityMessageAccumulator accumulator = new EntityMessageAccumulator();
    synchronized ( sessions )
    {
      for ( final EntityMessage message : messages )
      {
        final Map<String, Serializable> routingKeys = message.getRoutingKeys();

        final Integer buildingID = (Integer) routingKeys.get( TyrellRouterImpl.ROSTER_KEY );
        if ( null != buildingID )
        {
          for ( final TyrellSessionInfo sessionInfo : sessions.values() )
          {
            if ( sessionInfo.isBuildingInteresting( buildingID ) )
            {
              accumulator.addEntityMessage( sessionInfo.getQueue(), message );
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
