package org.realityforge.replicant.example.server.service;

import java.util.LinkedList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.inject.Inject;
import org.realityforge.replicant.example.server.entity.AbstractTyrellSessionManager;
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
@Local( { EntityMessageEndpoint.class, SubscriptionService.class, SessionManager.class } )
public class SubscriptionServiceEJB
  extends AbstractTyrellSessionManager
  implements SubscriptionService
{
  @Inject
  private RosterTypeRepository _rosterTypeRepository;

  @Inject
  private RosterRepository _rosterRepository;

  @SuppressWarnings( "SynchronizationOnLocalVariableOrMethodParameter" )
  @Override
  @Nullable
  public String poll( @Nonnull final String clientID, final int lastSequenceAcked )
  {
    final TyrellSession session = ensureSession( clientID );
    final PacketQueue queue = session.getQueue();
      queue.ack( lastSequenceAcked );
    final Packet packet = queue.nextPacketToProcess();
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
  {
    final TyrellSession session = ensureSession( clientID );
    final LinkedList<EntityMessage> messages = new LinkedList<>();
    for ( final Roster roster : _rosterRepository.findAll() )
    {
      if ( !session.isRosterInteresting( roster.getID() ) )
      {
        session.registerInterestInRoster( roster.getID() );
        getEncoder().encodeRoster( messages, roster );
      }
    }
    session.getQueue().addPacket( messages );
  }

  @Override
  public void subscribeToAll( @Nonnull final String clientID )
  {
    subscribeToRosterList( clientID );
  }

  @Override
  protected void collectMetaData( @Nonnull final LinkedList<EntityMessage> messages )
  {
    getEncoder().encodeObjects( messages, _rosterTypeRepository.findAll() );
  }

  @Override
  protected void collectRosterList( @Nonnull final LinkedList<EntityMessage> messages )
  {
    for ( final Roster roster : _rosterRepository.findAll() )
    {
      getEncoder().encodeRoster( messages, roster );
    }
  }
}
