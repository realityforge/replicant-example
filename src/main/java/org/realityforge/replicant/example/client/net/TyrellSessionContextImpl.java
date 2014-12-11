package org.realityforge.replicant.example.client.net;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.realityforge.gwt.datatypes.client.date.RDate;
import org.realityforge.replicant.example.client.data_type.RosterSubscriptionDTO;

public class TyrellSessionContextImpl
  implements TyrellSessionContext
{
  private final TyrellClientRouter _router;

  @Inject
  public TyrellSessionContextImpl( final TyrellClientRouter router )
  {
    _router = router;
  }

  @Override
  public boolean doesShiftListMatchEntity( @Nonnull final RosterSubscriptionDTO filter, @Nonnull final Object entity )
  {
    final RDate startAt = filter.getStartOn();
    final Date startOn = RDate.toDate( startAt );
    final Date endDate = RDate.toDate( startAt.addDays( filter.getNumberOfDays() ) );
    final Map<String, Serializable> route = _router.route( entity );
    final Date shiftStartAt = (Date) route.get( TyrellClientRouter.SHIFT_LIST_TYRELL_SHIFT_START_AT_KEY );
    return null == shiftStartAt ||
           ( ( startOn.before( shiftStartAt ) || startOn.equals( shiftStartAt ) ) &&
             endDate.after( shiftStartAt ) );
  }
}
