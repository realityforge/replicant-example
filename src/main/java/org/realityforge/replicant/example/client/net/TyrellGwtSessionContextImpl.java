package org.realityforge.replicant.example.client.net;

import java.util.Date;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.gwt.datatypes.client.date.RDate;
import org.realityforge.replicant.example.client.data_type.RosterSubscriptionDTO;

public class TyrellGwtSessionContextImpl
  implements TyrellGwtSessionContext
{
  @Override
  public boolean doesShiftListMatchEntity( @Nonnull final RosterSubscriptionDTO filter,
                                           @Nonnull final Object entity,
                                           @Nonnull final Integer rosterID,
                                           @Nullable final Date tyrellShiftStartAt )
  {
    final RDate startAt = filter.getStartOn();
    final Date startOn = RDate.toDate( startAt );
    final Date endDate = RDate.toDate( startAt.addDays( filter.getNumberOfDays() ) );
    return null == tyrellShiftStartAt ||
           ( ( startOn.before( tyrellShiftStartAt ) || startOn.equals( tyrellShiftStartAt ) ) &&
             endDate.after( tyrellShiftStartAt ) );
  }
}
