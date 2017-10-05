package org.realityforge.tyrell.server.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.realityforge.gwt.datatypes.client.date.RDate;
import org.realityforge.replicant.server.ChangeSet;
import org.realityforge.replicant.server.ChannelDescriptor;
import org.realityforge.replicant.server.EntityMessage;
import org.realityforge.replicant.server.EntityMessageSet;
import org.realityforge.replicant.server.transport.ReplicantSession;
import org.realityforge.tyrell.server.data_type.RosterSubscriptionDTO;
import org.realityforge.tyrell.server.entity.Roster;
import org.realityforge.tyrell.server.entity.Shift;
import org.realityforge.tyrell.server.entity.dao.ShiftRepository;
import org.realityforge.tyrell.server.net.TyrellGraphEncoder;

@ApplicationScoped
@Transactional( Transactional.TxType.REQUIRED )
@Typed( TyrellSessionContext.class )
public class TyrellSessionContextImpl
  extends AbstractTyrellSessionContextImpl
  implements TyrellSessionContext
{
  @Inject
  private TyrellGraphEncoder _encoder;
  @Inject
  private ShiftRepository _shiftRepository;

  @Override
  @Nonnull
  public List<Shift> getTyrellShiftRosterInShiftListGraph( @Nonnull final Roster entity,
                                                           @Nonnull final RosterSubscriptionDTO filter )
  {
    final Date startOn = filter.getStartOn();
    final int numberOfDays = filter.getNumberOfDays();
    final Date endDate = addDays( startOn, numberOfDays );
    return _shiftRepository.findAllByAreaOfInterest( entity, startOn, endDate );
  }

  private Date addDays( final Date time, final int dayCount )
  {
    final Calendar cal = Calendar.getInstance();
    cal.setTime( time );
    cal.add( Calendar.DAY_OF_YEAR, dayCount );
    return cal.getTime();
  }

  @Override
  protected boolean isShiftListInteresting( @Nonnull final EntityMessage message,
                                            @Nonnull final ReplicantSession session,
                                            @Nonnull final Integer rosterID,
                                            @Nonnull final RosterSubscriptionDTO filter,
                                            @Nullable final Date tyrellShiftStartAt )
  {
    final Date startOn = filter.getStartOn();
    final int numberOfDays = filter.getNumberOfDays();
    final Date endDate = addDays( startOn, numberOfDays );
    return null == tyrellShiftStartAt ||
           ( ( startOn.before( tyrellShiftStartAt ) || startOn.equals( tyrellShiftStartAt ) ) &&
             endDate.after( tyrellShiftStartAt ) );
  }

  @Override
  public void collectForFilterChangeShiftList( @Nonnull final ReplicantSession session,
                                               @Nonnull final ChangeSet changeSet,
                                               @Nonnull final ChannelDescriptor descriptor,
                                               @Nonnull final Roster entity,
                                               @Nonnull final RosterSubscriptionDTO originalFilter,
                                               @Nonnull final RosterSubscriptionDTO currentFilter )
  {
    //We assume that the roster has been replicated.
    // So what we need to do is send any additional shifts down. The client already knows enough to
    // delete those no longer of interest.
    final RDate originalStart = RDate.fromDate( originalFilter.getStartOn() );
    final RDate originalEnd = originalStart.addDays( originalFilter.getNumberOfDays() );
    RDate start = RDate.fromDate( currentFilter.getStartOn() );
    RDate end = start.addDays( currentFilter.getNumberOfDays() );

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
    final EntityMessageSet messages = new EntityMessageSet();
    _encoder.encodeObjects( messages,
                            _shiftRepository.findAllByAreaOfInterest( entity,
                                                                      RDate.toDate( start ),
                                                                      RDate.toDate( end ) ) );
    changeSet.merge( descriptor, messages );
  }
}
