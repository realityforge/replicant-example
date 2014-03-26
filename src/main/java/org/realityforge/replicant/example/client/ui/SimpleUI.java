package org.realityforge.replicant.example.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.realityforge.gwt.datatypes.client.date.RDate;
import org.realityforge.replicant.client.EntityChangeBroker;
import org.realityforge.replicant.client.EntityChangeEvent;
import org.realityforge.replicant.client.EntityChangeListener;
import org.realityforge.replicant.example.client.data_type.RosterSubscriptionDTO;
import org.realityforge.replicant.example.client.data_type.RosterSubscriptionDTOFactory;
import org.realityforge.replicant.example.client.entity.Position;
import org.realityforge.replicant.example.client.entity.Roster;
import org.realityforge.replicant.example.client.entity.Shift;
import org.realityforge.replicant.example.client.service.GwtRosterService;
import org.realityforge.replicant.example.client.service.TyrellGwtRpcAsyncCallback;
import org.realityforge.replicant.example.client.services.DataLoaderService;

public class SimpleUI
  extends Composite
  implements EntityChangeListener
{
  private static final Logger LOG = Logger.getLogger( SimpleUI.class.getName() );
  private static final Level LOG_LEVEL = Level.FINE;

  private final Tree _tree;
  private final Button _create;
  private final Button _update;
  private final Button _connect;
  private final Button _disconnect;
  private final Map<Object, TreeItem> _viewMap = new HashMap<Object, TreeItem>();
  private final Label _selected;
  private final TextBox _input;
  private final DataLoaderService _dataLoaderService;
  private final GwtRosterService _rosterService;
  private final Button _downloadAll;
  private Roster _selectedRoster;
  private Shift _selectedShift;
  private Position _selectedPosition;

  @Inject
  public SimpleUI( final ApplicationController applicationController,
                   final EntityChangeBroker broker,
                   final DataLoaderService dataLoaderService,
                   final GwtRosterService rosterService )
  {
    super();

    _dataLoaderService = dataLoaderService;
    _rosterService = rosterService;
    _input = new TextBox();
    _input.setValue( "My Roster" );
    _input.setEnabled( false );

    _connect = new Button( "Connect", new ClickHandler()
    {
      @Override
      public void onClick( final ClickEvent event )
      {
        doConnect();
      }
    } );
    _disconnect = new Button( "Disconnect", new ClickHandler()
    {
      @Override
      public void onClick( ClickEvent event )
      {
        doDisconnect();
      }
    } );
    _disconnect.setEnabled( false );
    _downloadAll = new Button( "Download All", new ClickHandler()
    {
      @Override
      public void onClick( ClickEvent event )
      {
        doDownloadAll();
      }
    } );
    _downloadAll.setEnabled( false );

    final VerticalPanel panel = new VerticalPanel();
    final FlowPanel controls = new FlowPanel();
    controls.add( _connect );
    controls.add( _disconnect );
    controls.add( _downloadAll );
    panel.add( controls );

    _tree = new Tree();
    _tree.setHeight( "200px" );
    _tree.setWidth( "100%" );
    _tree.setScrollOnSelectEnabled( true );

    _create = new Button( "Create", new ClickHandler()
    {
      @Override
      public void onClick( final ClickEvent event )
      {
        onCreate();
      }
    } );
    _create.setEnabled( false );

    _update = new Button( "Update", new ClickHandler()
    {
      @Override
      public void onClick( final ClickEvent event )
      {
        onUpdate();
      }
    } );
    _update.setEnabled( false );

    final HorizontalPanel control = new HorizontalPanel();
    control.add( _input );
    control.add( _create );
    control.add( _update );
    _selected = new Label();
    _selected.setWidth( "600px" );
    control.add( _selected );

    panel.add( control );
    panel.add( _tree );

    panel.add( applicationController );

    _tree.addSelectionHandler( new SelectionHandler<TreeItem>()
    {
      @Override
      public void onSelection( final SelectionEvent<TreeItem> event )
      {
        onSelect( event.getSelectedItem().getUserObject() );
      }
    } );
    broker.addChangeListener( this );
    initWidget( panel );
  }

  private void doDownloadAll()
  {
    _dataLoaderService.downloadAll();
  }

  private void createRoster( final Roster roster )
  {
    final TreeItem treeItem = _tree.addItem( createRosterWidget( roster ) );
    treeItem.setUserObject( roster );
    treeItem.setState( true );
    _viewMap.put( roster, treeItem );
  }

  private Widget createRosterWidget( final Roster roster )
  {
    final HorizontalPanel panel = new HorizontalPanel();
    panel.add( new Label( roster.getName() ) );
    final Button delete = new Button( "X" );
    delete.addClickHandler( new ClickHandler()
    {
      @Override
      public void onClick( final ClickEvent event )
      {
        doDeleteRoster( roster );
      }
    } );
    panel.add( delete );
    return panel;
  }

  private void doDeleteRoster( final Roster roster )
  {
    _rosterService.removeRoster( roster.getID() );
    if ( _selectedRoster == roster )
    {
      onSelect( null );
    }
  }

  private void onSelect( final Object userObject )
  {
    _selectedRoster = null;
    _selectedShift = null;
    _selectedPosition = null;
    if ( userObject instanceof Roster )
    {
      _selectedRoster = (Roster) userObject;
      _selected.setText( "Selected Roster: " + _selectedRoster.getName() );
      _create.setText( "Create Shift" );
      _create.setEnabled( true );
      _update.setEnabled( true );
    }
    else if ( userObject instanceof Shift )
    {
      _selectedShift = (Shift) userObject;
      _selected.setText( "Selected Shift: " + _selectedShift.getName() );
      _create.setText( "Create Position" );
      _create.setEnabled( true );
      _update.setEnabled( true );
    }
    else if ( userObject instanceof Position )
    {
      _selectedPosition = (Position) userObject;
      _selected.setText( "Selected Position: " + _selectedPosition.getName() );
      _create.setEnabled( false );
      _update.setEnabled( true );
    }
    else
    {
      _selected.setText( "" );
      _create.setEnabled( true );
      _update.setEnabled( false );
    }
  }

  private void onUpdate()
  {
    if ( null != _selectedRoster )
    {
      _rosterService.setRosterName( _selectedRoster.getID(), _input.getValue() );
    }
    else if ( null != _selectedShift )
    {
      _rosterService.setShiftName( _selectedShift.getID(), _input.getValue() );
    }
    else
    {
      _rosterService.setPositionName( _selectedPosition.getID(), _input.getValue() );
    }
    _input.setValue( "" );
  }

  private void onCreate()
  {
    if ( null == _selectedRoster && null == _selectedShift )
    {
      final int rosterType = 1;
      _rosterService.createRoster( rosterType, _input.getValue(), new TyrellGwtRpcAsyncCallback<Integer>()
      {
        @Override
        public void onSuccess( final Integer result )
        {
      final RosterSubscriptionDTO filter = RosterSubscriptionDTOFactory.create( RDate.fromDate( new Date() ), 7 );
          _dataLoaderService.getSession().subscribeToShiftList( result, filter, null );
        }
      } );
    }
    else if ( null != _selectedRoster && null == _selectedPosition )
    {
      _rosterService.createShift( _selectedRoster.getID(), _input.getValue(), new TyrellGwtRpcAsyncCallback<Integer>()
      {
        @Override
        public void onSuccess( final Integer result )
        {
          _dataLoaderService.getSession().subscribeToShift( result, null );
        }
      } );
    }
    else
    {
      _rosterService.createPosition( _selectedShift.getID(), _input.getValue() );
    }
    _input.setValue( "" );
  }

  private void doDisconnect()
  {
    _dataLoaderService.disconnect();
    _connect.setEnabled( true );
    _create.setEnabled( false );
    _update.setEnabled( false );
    _input.setEnabled( false );
    _downloadAll.setEnabled( false );
    _tree.clear();
    _disconnect.setEnabled( false );
  }

  private void doConnect()
  {
    _connect.setEnabled( false );
    _create.setEnabled( true );
    _input.setEnabled( true );
    _disconnect.setEnabled( true );
    _downloadAll.setEnabled( true );
    _tree.clear();
    _dataLoaderService.connect();
  }

  @Override
  public void entityAdded( final EntityChangeEvent event )
  {
    LOG.log( LOG_LEVEL, "entityAdded(" + event + ")" );
    final Object entity = event.getObject();
    if ( entity instanceof Roster )
    {
      createRoster( (Roster) entity );
    }
  }

  @Override
  public void entityRemoved( final EntityChangeEvent event )
  {
    LOG.log( LOG_LEVEL, "entityRemoved(" + event + ")" );
    final Object entity = event.getObject();
    final TreeItem treeItem = _viewMap.remove( entity );
    if ( null != treeItem )
    {
      treeItem.remove();
    }
  }

  @Override
  public void attributeChanged( final EntityChangeEvent event )
  {
    LOG.log( LOG_LEVEL, "attributeChanged(" + event + ")" );
    final Object entity = event.getObject();
    if ( entity instanceof Roster )
    {
      final Roster roster = (Roster) entity;
      final TreeItem treeItem = _viewMap.get( roster );
      if ( null != treeItem )
      {
        treeItem.setWidget( createRosterWidget( roster ) );
      }
    }
    else if ( entity instanceof Shift )
    {
      final Shift shift = (Shift) entity;
      final TreeItem treeItem = _viewMap.get( shift );
      if ( null != treeItem )
      {
        treeItem.setWidget( createShiftWidget( shift ) );
      }
    }
    else if ( entity instanceof Position )
    {
      final Position position = (Position) entity;
      final TreeItem treeItem = _viewMap.get( position );
      if ( null != treeItem )
      {
        treeItem.setWidget( createPositionWidget( position ) );
      }
    }
  }

  @Override
  public void relatedAdded( final EntityChangeEvent event )
  {
    LOG.log( LOG_LEVEL, "relatedAdded(" + event + ")" );
    final Object value = event.getValue();
    final Object object = event.getObject();
    if ( object instanceof Roster && value instanceof Shift )
    {
      final Shift shift = (Shift) value;
      final Roster roster = (Roster) object;
      final TreeItem parent = _viewMap.get( roster );
      if ( null != parent )
      {
        addShift( parent, shift );
      }
    }
    else if ( object instanceof Shift && value instanceof Position )
    {
      final Shift shift = (Shift) object;
      final Position position = (Position) value;
      final TreeItem parent = _viewMap.get( shift );
      if ( null != parent )
      {
        addPosition( parent, position );
      }
    }
  }

  private void addShift( final TreeItem parent, final Shift shift )
  {
    final TreeItem treeItem = parent.addItem( createShiftWidget( shift ) );
    treeItem.setUserObject( shift );
    _viewMap.put( shift, treeItem );
    for ( final Position position : shift.getPositions() )
    {
      addPosition( treeItem, position );
    }
  }

  private Widget createShiftWidget( final Shift shift )
  {
    final HorizontalPanel panel = new HorizontalPanel();
    final DateTimeFormat dtf = DateTimeFormat.getFormat( PredefinedFormat.DATE_TIME_FULL );
    panel.add( new Label( shift.getName() + " - Started At " + dtf.format( shift.getStartAt() ) ) );
    final Button delete = new Button( "X" );
    delete.addClickHandler( new ClickHandler()
    {
      @Override
      public void onClick( final ClickEvent event )
      {
        doDeleteShift( shift );
      }
    } );
    panel.add( delete );
    return panel;
  }

  private void doDeleteShift( final Shift shift )
  {
    _rosterService.removeShift( shift.getID() );
    if ( _selectedShift == shift )
    {
      onSelect( null );
    }
  }

  private void addPosition( final TreeItem parent, final Position position )
  {
    final TreeItem treeItem = parent.addItem( createPositionWidget( position ) );
    treeItem.setUserObject( position );
    _viewMap.put( position, treeItem );
  }

  private Widget createPositionWidget( final Position position )
  {
    final HorizontalPanel panel = new HorizontalPanel();
    panel.add( new Label( position.getName() ) );
    final Button delete = new Button( "X" );
    delete.addClickHandler( new ClickHandler()
    {
      @Override
      public void onClick( final ClickEvent event )
      {
        doDeletePosition( position );
      }
    } );
    panel.add( delete );
    return panel;
  }

  private void doDeletePosition( final Position position )
  {
    _rosterService.removePosition( position.getID() );
    if ( _selectedPosition == position )
    {
      onSelect( null );
    }
  }

  @Override
  public void relatedRemoved( final EntityChangeEvent event )
  {
    LOG.log( LOG_LEVEL, "relatedRemoved(" + event + ")" );
    final Object value = event.getValue();
    final Object object = event.getObject();
    if ( object instanceof Roster && value instanceof Shift )
    {
      final Shift shift = (Shift) value;
      final TreeItem treeItem = _viewMap.remove( shift );
      if ( null != treeItem )
      {
        treeItem.remove();
      }
    }
    else if ( object instanceof Shift && value instanceof Position )
    {
      final Position position = (Position) value;
      final TreeItem treeItem = _viewMap.remove( position );
      if ( null != treeItem )
      {
        treeItem.remove();
      }
    }
  }
}
