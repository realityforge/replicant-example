package org.realityforge.replicant.example.server.service;

import java.util.Calendar;
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
import org.realityforge.gwt.datatypes.client.date.RDate;
import org.realityforge.replicant.example.server.data_type.RosterSubscriptionDTO;
import org.realityforge.replicant.example.server.entity.Roster;
import org.realityforge.replicant.example.server.entity.Shift;
import org.realityforge.replicant.example.server.entity.dao.PersonRepository;
import org.realityforge.replicant.example.server.entity.dao.RosterRepository;
import org.realityforge.replicant.example.server.entity.dao.RosterTypeRepository;
import org.realityforge.replicant.example.server.entity.dao.ShiftRepository;
import org.realityforge.replicant.example.server.net.AbstractTyrellSessionManager;
import org.realityforge.replicant.example.server.net.TyrellSession;
import org.realityforge.replicant.example.server.net.TyrellSessionContext;
import org.realityforge.replicant.server.EntityMessageEndpoint;
import org.realityforge.replicant.server.EntityMessageSet;
import org.realityforge.replicant.server.ee.rest.ReplicantPollSource;
import org.realityforge.ssf.SessionManager;

@Singleton
@Local({ TyrellSessionContext.class, ReplicantPollSource.class, EntityMessageEndpoint.class, SubscriptionService.class, SessionManager.class })
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
  @Inject
  private PersonRepository _personRepository;

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
  public void downloadAll( @Nonnull final String clientID )
    throws BadSessionException
  {
    for ( final Roster roster : _rosterRepository.findAll() )
    {
      subscribeToShiftList( clientID, roster, new RosterSubscriptionDTO( new Date(), 7 ) );
    }
    for ( final Shift shift : _shiftRepository.findAll() )
    {
      subscribeToShift( clientID, shift );
    }
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
    final Date startOn = filter.getStartOn();
    final int numberOfDays = filter.getNumberOfDays();
    final Date endDate = addDays( startOn, numberOfDays );
    return _shiftRepository.findAllByAreaOfInterest( object, startOn, endDate );
  }

  private Date addDays( final Date time, final int dayCount )
  {
    final Calendar cal = Calendar.getInstance();
    cal.setTime( time );
    cal.add( Calendar.DAY_OF_YEAR, dayCount );
    return cal.getTime();
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
  public void collectPeople( @Nonnull final EntityMessageSet messages )
  {
    getEncoder().encodeObjects( messages, _personRepository.findAll() );
  }

  @Override
  public boolean isShiftListInteresting( @Nonnull final TyrellSession session,
                                         @Nonnull final Integer rosterID,
                                         @Nonnull final RosterSubscriptionDTO filter,
                                         @Nullable final Date shiftStartAt )
  {
    final Date startOn = filter.getStartOn();
    final int numberOfDays = filter.getNumberOfDays();
    final Date endDate = addDays( startOn, numberOfDays );
    return null == shiftStartAt ||
           ( ( startOn.before( shiftStartAt ) || startOn.equals( shiftStartAt ) ) &&
             endDate.after( shiftStartAt ) );
  }

  @Override
  public void collectForFilterChangeShiftList( @Nonnull final EntityMessageSet messages,
                                               @Nonnull final Roster entity,
                                               @Nonnull final RosterSubscriptionDTO original,
                                               @Nonnull final RosterSubscriptionDTO filter )
  {
    //We assume that the roster has been replicated.
    // So what we need to do is send any additional shifts down. The client already knows enough to
    // delete those no longer of interest.
    final RDate originalStart = RDate.fromDate( original.getStartOn() );
    final RDate originalEnd = originalStart.addDays( original.getNumberOfDays() );
    RDate start = RDate.fromDate( filter.getStartOn() );
    RDate end = start.addDays( filter.getNumberOfDays() );

    if ( originalStart.equals( start ) && originalEnd.equals( end ) )
    {
      return;
    }

    if ( start.before( originalEnd ) && start.after( originalStart ) )
    {
      start = originalEnd;
    }
    if ( end.after( originalStart ) && end.before( originalEnd ) )
    {
      end = originalStart;
    }
    getEncoder().encodeObjects( messages,
                                _shiftRepository.findAllByAreaOfInterest( entity,
                                                                          RDate.toDate( start ),
                                                                          RDate.toDate( end ) ) );
  }
}
