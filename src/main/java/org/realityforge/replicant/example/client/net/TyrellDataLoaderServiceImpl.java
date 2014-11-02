package org.realityforge.replicant.example.client.net;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.web.bindery.event.shared.EventBus;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.realityforge.gwt.datatypes.client.date.RDate;
import org.realityforge.replicant.client.ChangeMapper;
import org.realityforge.replicant.client.ChannelDescriptor;
import org.realityforge.replicant.client.EntityChangeBroker;
import org.realityforge.replicant.client.EntityRepository;
import org.realityforge.replicant.client.EntitySubscriptionManager;
import org.realityforge.replicant.client.json.gwt.ReplicantConfig;
import org.realityforge.replicant.client.transport.CacheService;
import org.realityforge.replicant.example.client.data_type.JsoRosterSubscriptionDTO;
import org.realityforge.replicant.example.client.data_type.RosterSubscriptionDTO;
import org.realityforge.replicant.example.client.event.SessionEstablishedEvent;
import org.realityforge.replicant.example.client.event.SystemErrorEvent;
import org.realityforge.replicant.example.client.service.internal.GwtSubscriptionService;
import org.realityforge.replicant.example.shared.net.TyrellReplicationGraph;

public class TyrellDataLoaderServiceImpl
  extends AbstractTyrellDataLoaderService
  implements DataLoaderService
{
  private final EventBus _eventBus;
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
           replicantConfig,
           subscriptionService );
    _eventBus = eventBus;
    _router = router;
  }

  @Override
  public void connect()
  {
    final String url = GWT.getHostPageBaseURL() + "api/auth/token";
    final RequestBuilder rb = new RequestBuilder( RequestBuilder.GET, url );
    try
    {
      rb.sendRequest( "", new RequestCallback()
      {
        @Override
        public void onResponseReceived( final Request request, final Response response )
        {
          onSessionCreated( response.getText() );
        }

        @Override
        public void onError( final Request request, final Throwable exception )
        {
          handleSystemFailure( exception, "Failed to generate token" );
        }
      } );
    }
    catch ( final RequestException e )
    {
      handleSystemFailure( e, "Failed to generate token" );
    }
  }

  @Override
  protected void onSessionConnected()
  {
    getSession().subscribeToMetaData( new Runnable()
    {
      @Override
      public void run()
      {
        _eventBus.fireEvent( new SessionEstablishedEvent() );
      }
    } );
  }

  @Override
  public void downloadAll()
  {
    getRemoteSubscriptionService().downloadAll( getSessionID() );
  }

  protected final void handleSystemFailure( @Nonnull final Throwable caught, @Nonnull final String message )
  {
    LOG.log( Level.SEVERE, "System Failure: " + message, caught );
    final Throwable cause = ( caught instanceof InvocationException ) ? caught.getCause() : caught;
    _eventBus.fireEvent( new SystemErrorEvent( message, cause ) );
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
