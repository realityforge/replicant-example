package org.realityforge.tyrell.client.ui;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.realityforge.gwt.datatypes.client.date.RDate;
import org.realityforge.replicant.client.EntityChangeBroker;
import org.realityforge.replicant.client.EntityChangeEvent;
import org.realityforge.replicant.client.EntityChangeListener;
import org.realityforge.replicant.client.EntityRepository;
import org.realityforge.tyrell.client.entity.Position;
import org.realityforge.tyrell.client.entity.Roster;
import org.realityforge.tyrell.client.entity.RosterType;
import org.realityforge.tyrell.client.entity.Shift;
import org.realityforge.tyrell.client.entity.dao.PersonRepository;
import org.realityforge.tyrell.client.entity.dao.RosterRepository;
import org.realityforge.tyrell.client.entity.dao.RosterTypeRepository;
import org.realityforge.tyrell.client.event.MetaDataSubscribeCompletedEvent;
import org.realityforge.tyrell.client.net.FrontendContext;
import org.realityforge.tyrell.client.service.RosterService;

@SuppressWarnings( "ALL" )
public class ApplicationController
  implements IsWidget, EntityChangeListener
{
  private static final Logger LOG = Logger.getLogger( ApplicationController.class.getName() );
  private static final Level LOG_LEVEL = Level.FINE;

  private final RosterTypeRepository _rosterTypeRepository;
  private final RosterRepository _rosterRepository;
  private final PersonRepository _personRepository;
  private final EntityChangeBroker _broker;
  private final FrontendContext _frontendContext;
  private final RosterService _rosterService;
  private final LoginUI _loginUI;
  private final RosterListUI _rosterListUI;
  private final RosterUI _rosterUI;
  private final SimplePanel _mainPanel;
  private Roster _currentRoster;
  private Shift _currentShift;
  private RDate _currentDate;

  @Inject
  public ApplicationController( @Nonnull final RosterTypeRepository rosterTypeRepository,
                                @Nonnull final RosterRepository rosterRepository,
                                @Nonnull final PersonRepository personRepository,
                                @Nonnull final RosterService rosterService,
                                @Nonnull final FrontendContext frontendContext,
                                @Nonnull final EntityRepository repository,
                                @Nonnull final EntityChangeBroker broker,
                                @Nonnull final EventBus eventBus )
  {
    _rosterTypeRepository = rosterTypeRepository;
    _rosterRepository = rosterRepository;
    _personRepository = personRepository;
    _rosterService = rosterService;
    _frontendContext = frontendContext;
    _broker = broker;
    _loginUI = new LoginUI( this );
    _rosterListUI = new RosterListUI( this );
    _rosterUI = new RosterUI( this );
    _mainPanel = new SimplePanel();
    gotoLoginActivity();
    broker.addChangeListener( this );
    eventBus.addHandler( MetaDataSubscribeCompletedEvent.TYPE, e -> goToRosterListActivity() );
  }

  protected RosterType getRosterType()
  {
    return _rosterTypeRepository.getByID( 1 );
  }

  private void gotoLoginActivity()
  {
    _mainPanel.setWidget( _loginUI );
  }

  public void connect()
  {
    _frontendContext.connect();
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

  void createAndSelectRoster( final RosterType rosterType, final String rosterName )
  {
    _rosterService.createRoster( rosterType, rosterName, this::selectRoster );
  }

  void selectRoster( @Nullable final Roster roster )
  {
    if ( _currentRoster != roster )
    {
      _currentRoster = roster;
      _frontendContext.selectRoster( roster, getCurrentDate() );
      if ( null != _currentRoster )
      {
        _rosterUI.setRoster( _currentRoster );
        goToRosterActivity();
      }
      else
      {
        _rosterUI.setRoster( null );
        goToRosterListActivity();
      }
    }
  }

  @Nonnull
  RDate getCurrentDate()
  {
    if ( null == _currentDate )
    {
      _currentDate = RDate.fromDate( new Date() );
    }
    return _currentDate;
  }

  void updateShiftListSubscription( @Nonnull final RDate newStartDate )
  {
    if ( !newStartDate.equals( _currentDate ) )
    {
      _frontendContext.selectRoster( _currentRoster, newStartDate );
      _currentDate = newStartDate;
    }
  }

  void selectShift( @Nullable final Shift shift )
  {
    if ( _currentShift != shift )
    {
      _currentShift = shift;
      _frontendContext.selectShift( shift );
      _rosterUI.setShift( _currentShift );
    }
  }

  private void goToRosterActivity()
  {
    _mainPanel.setWidget( _rosterUI );
  }

  private void goToRosterListActivity()
  {
    _mainPanel.setWidget( _rosterListUI );
  }

  private void resetRosterList()
  {
    _rosterListUI.setRosters( _rosterRepository.findAll() );
  }

  @Override
  public void entityAdded( @Nonnull final EntityChangeEvent event )
  {
    LOG.log( LOG_LEVEL, "entityAdded(" + event + ")" );
    final Object entity = event.getObject();
    if ( entity instanceof Roster )
    {
      resetRosterList();
    }
  }

  @Override
  public void entityRemoved( @Nonnull final EntityChangeEvent event )
  {
    LOG.log( LOG_LEVEL, "entityRemoved(" + event + ")" );
    final Object entity = event.getObject();
    if ( _currentRoster == entity )
    {
      selectRoster( null );
    }
    else if ( _currentShift == entity )
    {
      selectShift( null );
    }
  }

  @Override
  public void attributeChanged( @Nonnull final EntityChangeEvent event )
  {
    LOG.log( LOG_LEVEL, "attributeChanged(" + event + ")" );
    final Object entity = event.getObject();
    if ( entity instanceof Roster )
    {
      resetRosterList();
    }
  }

  @Override
  public void relatedAdded( @Nonnull final EntityChangeEvent event )
  {
    LOG.log( LOG_LEVEL, "relatedAdded(" + event + ")" );
  }

  @Override
  public void relatedRemoved( @Nonnull final EntityChangeEvent event )
  {
    LOG.log( LOG_LEVEL, "relatedRemoved(" + event + ")" );
  }

  void doDeleteRoster( @Nonnull final Roster roster )
  {
    _rosterService.removeRoster( roster, r -> goToRosterListActivity() );
  }

  public void setRosterName( final Roster roster, final String name )
  {
    _rosterService.setRosterName( roster, name );
  }

  public void setShiftName( final Shift shift, final String name )
  {
    _rosterService.setShiftName( shift, name );
  }

  public void createShift( final Roster roster, final String name, final RDate shiftOn )
  {
    _rosterService.createShift( roster, name, shiftOn );
  }

  public void createPosition( final Shift shift, final String name )
  {
    _rosterService.createPosition( shift, name );
  }

  public void setPositionName( final Position position, final String name )
  {
    _rosterService.setPositionName( position, name );
  }

  void doDeleteShift( final Shift shift )
  {
    _rosterService.removeShift( shift );
  }

  void disconnect()
  {
    _frontendContext.disconnect();
    _loginUI.resetState();
    gotoLoginActivity();
  }

  public void removePosition( final Position position )
  {
    _rosterService.removePosition( position );
  }

  void loadResources()
  {
    _frontendContext.loadPeople();
  }

  void unloadResources()
  {
    _frontendContext.unloadPeople();
  }

  void assignResource( final Position position, final int personID )
  {
    _rosterService.assignPerson( position, _personRepository.getByID( personID ) );
  }
}
