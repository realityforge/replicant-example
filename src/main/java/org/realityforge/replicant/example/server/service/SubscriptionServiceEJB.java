package org.realityforge.replicant.example.server.service;

import java.util.LinkedList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.inject.Inject;
import org.realityforge.replicant.example.server.entity.Roster;
import org.realityforge.replicant.example.server.entity.TyrellSession;
import org.realityforge.replicant.example.server.entity.dao.RosterRepository;
import org.realityforge.replicant.example.server.entity.dao.RosterTypeRepository;
import org.realityforge.replicant.server.EntityMessage;
import org.realityforge.replicant.server.EntityMessageEndpoint;
import org.realityforge.replicant.server.json.JsonEncoder;
import org.realityforge.replicant.server.transport.Packet;
import org.realityforge.replicant.server.transport.PacketQueue;
import org.realityforge.ssf.SessionManager;

@Singleton
@Local({ EntityMessageEndpoint.class, SubscriptionService.class, SessionManager.class })
public class SubscriptionServiceEJB
  extends AbstractTyrellSessionManager
  implements SubscriptionService
{
  @Inject
  private RosterTypeRepository _rosterTypeRepository;

  @Inject
  private RosterRepository _rosterRepository;

  @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
  @Override
  @Nullable
  public String poll( @Nonnull final String clientID, final int lastSequenceAcked )
    throws BadSessionException
  {
    final TyrellSession session = ensureSession( clientID );
    final PacketQueue queue = session.getQueue();
    final Packet packet;
    synchronized ( session )
    {
      queue.ack( lastSequenceAcked );
      packet = queue.nextPacketToProcess();
    }
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
    final TyrellSession session = ensureSession( clientID );
    if ( !session.isInterestedInRosterList() )
    {
      downloadAll( session );
    }
  }

  private void downloadAll( @Nonnull final TyrellSession session )
  {
    final LinkedList<EntityMessage> messages = new LinkedList<>();
    for ( final Roster roster : _rosterRepository.findAll() )
    {
      session.registerInterestInRoster( roster.getID() );
      getEncoder().encodeRoster( messages, roster );
    }
    session.getQueue().addPacket( messages );
  }

  @Override
  public void subscribeToAll( @Nonnull final String clientID )
    throws BadSessionException
  {
    final TyrellSession session = ensureSession( clientID );
    if ( !session.isInterestedInRosterList() )
    {
      session.setInterestedInRosterList( true );
      downloadAll( session );
    }
  }

  @Override
  public void subscribeToMetaData( @Nonnull final String clientID )
    throws BadSessionException
  {
    final TyrellSession session = ensureSession( clientID );
    if ( !session.isInterestedInMetaData() )
    {
      session.setInterestedInMetaData( true );
      final LinkedList<EntityMessage> messages = new LinkedList<>();
      getEncoder().encodeObjects( messages, _rosterTypeRepository.findAll() );
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
    final TyrellSession session = ensureSession( clientID );
    if ( !session.isRosterInteresting( roster.getID() ) )
    {
      session.registerInterestInRoster( roster.getID() );
      final LinkedList<EntityMessage> messages = new LinkedList<>();
      getEncoder().encodeRoster( messages, roster );
      session.getQueue().addPacket( messages );
    }
  }

  @Override
  public void unsubscribeFromRoster( @Nonnull final String clientID, @Nonnull final Roster roster )
    throws BadSessionException
  {
    ensureSession( clientID ).deregisterInterestInRoster( roster.getID() );
  }

  @Override
  public void subscribeToRosterList( @Nonnull final String clientID )
    throws BadSessionException
  {
    final TyrellSession session = ensureSession( clientID );
    if ( !session.isInterestedInRosterList() )
    {
      session.setInterestedInRosterList( true );
      final LinkedList<EntityMessage> messages = new LinkedList<>();
      getEncoder().encodeObjects( messages, _rosterRepository.findAll() );
      session.getQueue().addPacket( messages );
    }
  }

  @Override
  public void unsubscribeFromRosterList( @Nonnull final String clientID )
    throws BadSessionException
  {
    ensureSession( clientID ).setInterestedInRosterList( false );
  }
}
