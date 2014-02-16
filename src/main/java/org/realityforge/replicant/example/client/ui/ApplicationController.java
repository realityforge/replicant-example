package org.realityforge.replicant.example.client.ui;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.realityforge.gwt.datatypes.client.date.RDate;
import org.realityforge.replicant.client.EntityChangeBroker;
import org.realityforge.replicant.client.EntityChangeEvent;
import org.realityforge.replicant.client.EntityChangeListener;
import org.realityforge.replicant.client.EntityRepository;
import org.realityforge.replicant.example.client.data_type.RosterSubscriptionDTO;
import org.realityforge.replicant.example.client.data_type.RosterSubscriptionDTOFactory;
import org.realityforge.replicant.example.client.entity.Position;
import org.realityforge.replicant.example.client.entity.Roster;
import org.realityforge.replicant.example.client.entity.Shift;
import org.realityforge.replicant.example.client.entity.TyrellSubscriptionManager;
import org.realityforge.replicant.example.client.event.SessionEstablishedEvent;
import org.realityforge.replicant.example.client.event.SessionEstablishedEventHandler;
import org.realityforge.replicant.example.client.service.GwtRpcRosterService;
import org.realityforge.replicant.example.client.service.TyrellGwtRpcAsyncCallback;
import org.realityforge.replicant.example.client.services.DataLoaderService;

public class ApplicationController
  implements IsWidget, EntityChangeListener
{
  private static final Logger LOG = Logger.getLogger( SimpleUI.class.getName() );
  private static final Level LOG_LEVEL = Level.FINE;

  private final EntityRepository _repository;
  private final EntityChangeBroker _broker;
  private final DataLoaderService _dataLoaderService;
  private final GwtRpcRosterService _rosterService;
  private final LoginUI _loginUI;
  private final RosterListUI _rosterListUI;
  private final RosterUI _rosterUI;
  private final SimplePanel _mainPanel;
  private final EventBus _eventBus;
  private Roster _currentRoster;
  private Shift _currentShift;

  @Inject
  public ApplicationController( final GwtRpcRosterService rosterService,
                                final DataLoaderService dataLoaderService,
                                final EntityRepository repository,
                                final EntityChangeBroker broker,
                                final EventBus eventBus )
  {
    _rosterService = rosterService;
    _dataLoaderService = dataLoaderService;
    _repository = repository;
    _broker = broker;
    _eventBus = eventBus;
    _loginUI = new LoginUI( this );
    _rosterListUI = new RosterListUI( this );
    _rosterUI = new RosterUI( this );
    _mainPanel = new SimplePanel();
    gotoLoginActivity();
    broker.addChangeListener( this );
    _eventBus.addHandler( SessionEstablishedEvent.TYPE, new SessionEstablishedEventHandler()
    {
      @Override
      public void onSessionEstablished( final SessionEstablishedEvent event )
      {
        goToRosterListActivity();
        _dataLoaderService.getSession().getSubscriptionManager().subscribeToRosterList();
      }
    } );
  }

  private void gotoLoginActivity()
  {
    _mainPanel.setWidget( _loginUI );
  }

  public void connect()
  {
    _dataLoaderService.connect();
  }

  public EntityChangeBroker getBroker()
  {
    return _broker;
  }

  @Override
  public Widget asWidget()
  {
    return _mainPanel;
  }

  public void createAndSelectRoster( final int rosterType, final String rosterName )
  {
    _rosterService.createRoster( rosterType, rosterName, new TyrellGwtRpcAsyncCallback<Integer>()
    {
      @Override
      public void onSuccess( final Integer result )
      {
        selectRoster( _repository.findByID( Roster.class, result ) );
      }
    } );
  }

  public void selectRoster( @Nullable final Roster roster )
  {
    if ( _currentRoster == roster )
    {
      return;
    }
    final TyrellSubscriptionManager subscriptionManager = _dataLoaderService.getSession().getSubscriptionManager();
    if ( null != _currentRoster )
    {
      subscriptionManager.unsubscribeFromShiftList( _currentRoster.getID() );
    }
    _currentRoster = roster;
    if ( null != _currentRoster )
    {
      final RosterSubscriptionDTO filter = RosterSubscriptionDTOFactory.create( RDate.fromDate( new Date() ), 7 );
      subscriptionManager.subscribeToShiftList( roster.getID(), filter );
      _rosterUI.setRoster( _currentRoster );
      goToRosterAtivity();
    }
    else
    {
      _rosterUI.setRoster( null );
      goToRosterListActivity();
    }
  }

  public void selectShift( @Nullable final Shift shift )
  {
    if( _currentShift == shift )
    {
      return;
    }
    final TyrellSubscriptionManager subscriptionManager = _dataLoaderService.getSession().getSubscriptionManager();
    if ( null != _currentShift )
    {
      subscriptionManager.unsubscribeFromShift( _currentShift.getID() );
    }
    _currentShift = shift;
    if ( null != _currentShift )
    {
      subscriptionManager.subscribeToShift( _currentShift.getID() );
      _rosterUI.setShift( _currentShift );
    }
    else
    {
      _rosterUI.setShift( null );
    }
  }

  private void goToRosterAtivity()
  {
    _mainPanel.setWidget( _rosterUI );
  }

  private void goToRosterListActivity()
  {
    _mainPanel.setWidget( _rosterListUI );
  }

  private void resetRosterList()
  {
    _rosterListUI.setRosters( _repository.findAll( Roster.class ) );
  }

  @Override
  public void entityAdded( final EntityChangeEvent event )
  {
    LOG.log( LOG_LEVEL, "entityAdded(" + event + ")" );
    final Object entity = event.getObject();
    if ( entity instanceof Roster )
    {
      resetRosterList();
    }
  }

  @Override
  public void entityRemoved( final EntityChangeEvent event )
  {
    LOG.log( LOG_LEVEL, "entityRemoved(" + event + ")" );
    final Object entity = event.getObject();
    if ( entity instanceof Roster )
    {
      resetRosterList();
    }
  }

  @Override
  public void attributeChanged( final EntityChangeEvent event )
  {
    LOG.log( LOG_LEVEL, "attributeChanged(" + event + ")" );
    final Object entity = event.getObject();
    if ( entity instanceof Roster )
    {
      resetRosterList();
    }
  }

  @Override
  public void relatedAdded( final EntityChangeEvent event )
  {
    LOG.log( LOG_LEVEL, "relatedAdded(" + event + ")" );
  }

  @Override
  public void relatedRemoved( final EntityChangeEvent event )
  {
    LOG.log( LOG_LEVEL, "relatedRemoved(" + event + ")" );
  }

  public void doDeleteRoster( final Roster roster )
  {
    _rosterService.removeRoster( roster.getID(), new TyrellGwtRpcAsyncCallback<Void>()
    {
      @Override
      public void onSuccess( final Void result )
      {
        goToRosterListActivity();
      }
    } );
  }

  public void doDeleteShift( final Shift shift )
  {
    _rosterService.removeShift( shift.getID() );
  }

  public void disconnect()
  {
    _dataLoaderService.disconnect();
    _loginUI.resetState();
    gotoLoginActivity();
  }

  public void removePosition( final Position position )
  {
    _rosterService.removePosition( position.getID() );
  }
}
