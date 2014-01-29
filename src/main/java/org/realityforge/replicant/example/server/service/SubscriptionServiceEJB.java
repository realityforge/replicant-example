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
import org.realityforge.replicant.example.server.entity.dao.RosterTypeRepository;
import org.realityforge.replicant.server.EntityMessage;
import org.realityforge.replicant.server.EntityMessageEndpoint;
import org.realityforge.replicant.server.json.JsonEncoder;
import org.realityforge.replicant.server.transport.EntityMessageAccumulator;
import org.realityforge.replicant.server.transport.Packet;
import org.realityforge.ssf.InMemorySessionManager;
import org.realityforge.ssf.SessionManager;

@Singleton
@Local( { EntityMessageEndpoint.class, SubscriptionService.class, SessionManager.class } )
public class SubscriptionServiceEJB
  extends InMemorySessionManager<TyrellSessionInfo>
  implements SubscriptionService, EntityMessageEndpoint
{
  private static final Logger LOG = Logger.getLogger( SubscriptionServiceEJB.class.getName() );

  @Inject
  private TyrellGraphEncoder _encoder;

  @Inject
  private RosterTypeRepository _rosterTypeRepository;

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
  public void downloadAll( @Nonnull final String clientID )
    throws BadSessionException
  {
    final TyrellSessionInfo session = ensureSession( clientID );
    if ( !session.isInterestedInAllRosters() )
    {
      downloadAll( session );
    }
  }

  private void downloadAll( @Nonnull final TyrellSessionInfo session )
  {
    final LinkedList<EntityMessage> messages = new LinkedList<>();
    for ( final Roster roster : _rosterRepository.findAll() )
    {
      session.registerInterest( roster.getID() );
      _encoder.encodeRoster( messages, roster );
    }
    session.getQueue().addPacket( messages );
  }

  @Override
  public void subscribeToAll( @Nonnull final String clientID )
    throws BadSessionException
  {
    final TyrellSessionInfo session = ensureSession( clientID );
    if ( !session.isInterestedInAllRosters() )
    {
      session.setInterestedInAllRosters( true );
      downloadAll( session );
    }
  }

  @Override
  public void subscribeToMetaData( @Nonnull final String clientID )
    throws BadSessionException
  {
    final TyrellSessionInfo session = ensureSession( clientID );
    if ( !session.isInterestedInMetaData() )
    {
      session.setInterestedInMetaData( true );
      final LinkedList<EntityMessage> messages = new LinkedList<>();
      _encoder.encodeObjects( messages, _rosterTypeRepository.findAll() );
      session.getQueue().addPacket( messages );
    }
  }

  @Override
  public void unsubscribeFromMetaData( @Nonnull final String clientID )
    throws BadSessionException
  {
    ensureSession( clientID ).setInterestedInMetaData( false );
  }

  @Override
  public void subscribeToRoster( @Nonnull final String clientID, @Nonnull final Roster roster )
    throws BadSessionException
  {
    final TyrellSessionInfo session = ensureSession( clientID );
    if ( !session.isRosterInteresting( roster.getID() ) )
    {
      session.registerInterest( roster.getID() );
      final LinkedList<EntityMessage> messages = new LinkedList<>();
      _encoder.encodeRoster( messages, roster );
      session.getQueue().addPacket( messages );
    }
  }

  @Override
  public void unsubscribeFromRoster( @Nonnull final String clientID, @Nonnull final Roster roster )
    throws BadSessionException
  {
    ensureSession( clientID ).deregisterInterest( roster.getID() );
  }

  @Override
  public void subscribeToRosterList( @Nonnull final String clientID )
    throws BadSessionException
  {
    final TyrellSessionInfo session = ensureSession( clientID );
    if ( !session.isInterestedInRosterList() )
    {
      session.setInterestedInRosterList( true );
      final LinkedList<EntityMessage> messages = new LinkedList<>();
      _encoder.encodeObjects( messages, _rosterRepository.findAll() );
      session.getQueue().addPacket( messages );
    }
  }

  @Override
  public void unsubscribeFromRosterList( @Nonnull final String clientID )
    throws BadSessionException
  {
    ensureSession( clientID ).setInterestedInRosterList( false );
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

        final Integer rosterID = (Integer) routingKeys.get( TyrellRouterImpl.ROSTER_KEY );
        if ( null != rosterID )
        {
          for ( final TyrellSessionInfo sessionInfo : sessions.values() )
          {
            if ( sessionInfo.isInterestedInAllRosters() || sessionInfo.isRosterInteresting( rosterID ) )
            {
              accumulator.addEntityMessage( sessionInfo.getQueue(), message );
            }
          }
        }
        if ( null != routingKeys.get( TyrellRouterImpl.META_DATA_KEY ) )
        {
          for ( final TyrellSessionInfo sessionInfo : sessions.values() )
          {
            if ( sessionInfo.isInterestedInMetaData() )
            {
              accumulator.addEntityMessage( sessionInfo.getQueue(), message );
            }
          }
        }
        if ( null != routingKeys.get( TyrellRouterImpl.ROSTER_LIST_KEY ) )
        {
          for ( final TyrellSessionInfo sessionInfo : sessions.values() )
          {
            if ( sessionInfo.isInterestedInRosterList() )
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
