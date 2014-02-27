package org.realityforge.replicant.example.server.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import org.realityforge.replicant.example.server.data_type.RosterSubscriptionDTO;
import org.realityforge.replicant.example.server.entity.AbstractTyrellSessionManager;
import org.realityforge.replicant.example.server.entity.Roster;
import org.realityforge.replicant.example.server.entity.Shift;
import org.realityforge.replicant.example.server.entity.TyrellSession;
import org.realityforge.replicant.example.server.entity.TyrellSessionContext;
import org.realityforge.replicant.example.server.entity.dao.RosterRepository;
import org.realityforge.replicant.example.server.entity.dao.RosterTypeRepository;
import org.realityforge.replicant.example.server.entity.dao.ShiftRepository;
import org.realityforge.replicant.server.EntityMessageEndpoint;
import org.realityforge.replicant.server.EntityMessageSet;
import org.realityforge.replicant.server.ee.EntityMessageCacheUtil;
import org.realityforge.replicant.server.json.JsonEncoder;
import org.realityforge.replicant.server.transport.Packet;
import org.realityforge.ssf.SessionManager;

@Singleton
@Local({ TyrellSessionContext.class, EntityMessageEndpoint.class, SubscriptionService.class, SessionManager.class })
public class SubscriptionServiceEJB
  extends AbstractTyrellSessionManager
  implements SubscriptionService, TyrellSessionContext
{
  private static final Logger LOG = Logger.getLogger( SubscriptionServiceEJB.class.getName() );

  private static final int MAX_IDLE_TIME = 1000 * 60 * 5;

  @Inject
  private RosterTypeRepository _rosterTypeRepository;

  @Inject
  private RosterRepository _rosterRepository;

  @Inject
  private ShiftRepository _shiftRepository;

  /**
   * Remove idle session changes every 30 seconds.
   */
  @Schedule(second = "30", minute = "*", hour = "*", persistent = false)
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
    final Packet packet = poll( ensureSession( clientID ), lastSequenceAcked );
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
    final EntityMessageSet messages = EntityMessageCacheUtil.getSessionEntityMessageSet();
    final TyrellSession session = ensureSession( clientID );
    for ( final Roster roster : _rosterRepository.findAll() )
    {
      if ( !session.isShiftListInteresting( roster.getID() ) )
      {
        final RosterSubscriptionDTO filter = new RosterSubscriptionDTO( new Date(), 7 );
        session.registerInterestInShiftList( roster.getID(), filter );
        getEncoder().encodeShiftList( messages, roster, filter );
      }
    }
    for ( final Shift shift : _shiftRepository.findAll() )
    {
      if ( !session.isShiftInteresting( shift.getID() ) )
      {
        session.registerInterestInShift( shift.getID() );
        getEncoder().encodeShift( messages, shift );
      }
    }
  }

  @Override
  public String getMetaDataCacheKey()
  {
    // Return a constant as we know that it will never be changed except with a new release
    return "MyConstant";
  }

  @Override
  protected TyrellSessionContext getContext()
  {
    return this;
  }

  @Override
  @Nonnull
  public List<Shift> getShiftsInShiftListGraph( @Nonnull final Roster object,
                                                @Nonnull final RosterSubscriptionDTO filter )
  {
    return object.getShifts();
  }

  @Override
  public void collectMetaData( @Nonnull final EntityMessageSet messages )
  {
    getEncoder().encodeObjects( messages, _rosterTypeRepository.findAll() );
  }

  @Override
  public void collectRosterList( @Nonnull final EntityMessageSet messages )
  {
    getEncoder().encodeObjects( messages, _rosterRepository.findAll() );
  }

  @Override
  public boolean isShiftListInteresting( @Nonnull final TyrellSession session,
                                         @Nonnull final Integer rosterID,
                                         @Nonnull final RosterSubscriptionDTO filter,
                                         @Nonnull final Date shiftStartAt )
  {
    return true;
  }

  @Override
  public void updateSubscriptionToShiftList( @Nonnull final String clientID,
                                             @Nonnull final Roster roster,
                                             @Nonnull final RosterSubscriptionDTO rosterSubscriptionDTO )
    throws BadSessionException
  {
    final TyrellSession session = ensureSession( clientID );
    final RosterSubscriptionDTO existing = session.getInterestedInShiftList().get( roster.getID() );
    session.updateInterestInShiftList( roster.getID(), rosterSubscriptionDTO );
  }

  @Nonnull
  @Override
  protected TyrellSession newSessionInfo()
  {
    return super.newSessionInfo();
  }
}
