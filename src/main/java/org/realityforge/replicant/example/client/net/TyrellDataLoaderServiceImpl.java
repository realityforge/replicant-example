package org.realityforge.replicant.example.client.net;

import com.google.web.bindery.event.shared.EventBus;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.realityforge.gwt.datatypes.client.date.RDate;
import org.realityforge.replicant.client.ChannelDescriptor;
import org.realityforge.replicant.client.EntityChangeBroker;
import org.realityforge.replicant.client.EntityRepository;
import org.realityforge.replicant.client.EntitySubscriptionManager;
import org.realityforge.replicant.client.json.gwt.ReplicantConfig;
import org.realityforge.replicant.client.transport.CacheService;
import org.realityforge.replicant.example.client.data_type.JsoRosterSubscriptionDTO;
import org.realityforge.replicant.example.client.data_type.RosterSubscriptionDTO;
import org.realityforge.replicant.example.client.service.internal.GwtSubscriptionService;
import org.realityforge.replicant.example.shared.net.TyrellReplicationGraph;

public class TyrellDataLoaderServiceImpl
  extends AbstractTyrellDataLoaderService
{
  private final TyrellClientRouter _router;

  @Inject
  public TyrellDataLoaderServiceImpl( final TyrellChangeMapperImpl changeMapper,
                                      final EntityChangeBroker changeBroker,
                                      final EntityRepository repository,
                                      final CacheService cacheService,
                                      final EntitySubscriptionManager subscriptionManager,
                                      final GwtSubscriptionService subscriptionService,
                                      final ReplicantConfig replicantConfig,
                                      final EventBus eventBus,
                                      final TyrellClientRouter router )
  {
    super( changeMapper,
           changeBroker,
           repository,
           cacheService,
           subscriptionManager,
           eventBus,
           replicantConfig,
           subscriptionService );
    _router = router;
  }

  @Override
  protected boolean doesEntityMatchFilter( @Nonnull final ChannelDescriptor descriptor,
                                           @Nullable final Object rawFilter,
                                           @Nonnull final Class<?> entityType,
                                           @Nonnull final Object entityID )
  {
    if ( TyrellReplicationGraph.SHIFT_LIST == descriptor.getGraph() )
    {
      final RosterSubscriptionDTO filter = (JsoRosterSubscriptionDTO) rawFilter;
      assert null != filter;
      final RDate startAt = filter.getStartOn();
      final Date startOn = RDate.toDate( startAt );
      final Date endDate = RDate.toDate( startAt.addDays( filter.getNumberOfDays() ) );
      final Object entity = getRepository().getByID( entityType, entityID );
      final Map<String, Serializable> route = _router.route( entity );
      final Date shiftStartAt = (Date) route.get( TyrellClientRouter.SHIFT_LIST_TYRELL_SHIFT_START_AT_KEY );
      return null == shiftStartAt ||
             ( ( startOn.before( shiftStartAt ) || startOn.equals( shiftStartAt ) ) &&
               endDate.after( shiftStartAt ) );
    }
    else
    {
      throw new IllegalStateException();
    }
  }
}
