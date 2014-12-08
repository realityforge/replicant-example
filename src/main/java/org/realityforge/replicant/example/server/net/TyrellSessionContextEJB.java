package org.realityforge.replicant.example.server.net;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.realityforge.gwt.datatypes.client.date.RDate;
import org.realityforge.replicant.example.server.data_type.RosterSubscriptionDTO;
import org.realityforge.replicant.example.server.entity.Roster;
import org.realityforge.replicant.example.server.entity.Shift;
import org.realityforge.replicant.example.server.entity.dao.PersonRepository;
import org.realityforge.replicant.example.server.entity.dao.RosterRepository;
import org.realityforge.replicant.example.server.entity.dao.RosterTypeRepository;
import org.realityforge.replicant.example.server.entity.dao.ShiftRepository;
import org.realityforge.replicant.server.EntityMessageSet;

@Stateless
public class TyrellSessionContextEJB
  implements TyrellSessionContext
{
  @Inject
  private TyrellGraphEncoder _encoder;
  @Inject
  private RosterTypeRepository _rosterTypeRepository;
  @Inject
  private RosterRepository _rosterRepository;
  @Inject
  private ShiftRepository _shiftRepository;
  @Inject
  private PersonRepository _personRepository;

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
    _encoder.encodeObjects( messages,
                                _shiftRepository.findAllByAreaOfInterest( entity,
                                                                          RDate.toDate( start ),
                                                                          RDate.toDate( end ) ) );
  }
}
