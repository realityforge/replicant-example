package org.realityforge.replicant.example.server.service;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.transaction.TransactionSynchronizationRegistry;
import org.realityforge.replicant.example.server.entity.AbstractTyrellSessionManager;
import org.realityforge.replicant.example.server.entity.Roster;
import org.realityforge.replicant.example.server.entity.TyrellSession;
import org.realityforge.replicant.example.server.entity.dao.RosterRepository;
import org.realityforge.replicant.example.server.entity.dao.RosterTypeRepository;
import org.realityforge.replicant.server.EntityMessage;
import org.realityforge.replicant.server.EntityMessageEndpoint;
import org.realityforge.replicant.server.ee.ReplicantContextHolder;
import org.realityforge.replicant.server.json.JsonEncoder;
import org.realityforge.replicant.server.transport.Packet;
import org.realityforge.replicant.server.transport.PacketQueue;
import org.realityforge.replicant.shared.transport.ReplicantContext;
import org.realityforge.ssf.SessionManager;

@Singleton
@Local( { EntityMessageEndpoint.class, SubscriptionService.class, SessionManager.class } )
public class SubscriptionServiceEJB
  extends AbstractTyrellSessionManager
  implements SubscriptionService
{
  private static final Logger LOG = Logger.getLogger( SubscriptionServiceEJB.class.getName() );

  private static final int MAX_IDLE_TIME = 1000 * 60 * 5;

  @Inject
  private RosterTypeRepository _rosterTypeRepository;

  @Inject
  private RosterRepository _rosterRepository;

  @Resource
  private TransactionSynchronizationRegistry _registry;

  @Override
  protected String getMetaDataCacheKey()
  {
    // Return a constant as we know that it will never be changed except with a new release
    return "MyConstant";
  }

  /**
   * Remove idle session changes every 30 seconds.
   */
  @Schedule( second = "30", minute = "*", hour = "*", persistent = false )
  public void removeIdleSessions()
  {
    final int removedSessions = removeIdleSessions( MAX_IDLE_TIME );
    if ( 0 != removedSessions && LOG.isLoggable( Level.INFO ) )
    {
      LOG.info( "Removed " + removedSessions + " idle sessions" );
    }
  }

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
      return JsonEncoder.
        encodeChangeSetFromEntityMessages( packet.getSequence(),
                                           packet.getRequestID(),
                                           packet.getETag(),
                                           packet.getChanges() );
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
    if ( 0 != messages.size() )
    {
      final String requestID = (String) _registry.getResource( ReplicantContext.REQUEST_ID_KEY );
      ReplicantContextHolder.put( ReplicantContext.REQUEST_COMPLETE_KEY, "0" );
      session.getQueue().addPacket( requestID, null, messages );
    }
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
