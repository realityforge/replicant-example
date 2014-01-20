package org.realityforge.replicant.example.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.realityforge.replicant.client.EntityChangeBroker;
import org.realityforge.replicant.client.EntityChangeEvent;
import org.realityforge.replicant.client.EntityChangeListener;
import org.realityforge.replicant.example.client.entity.tyrell.Building;
import org.realityforge.replicant.example.client.entity.tyrell.Room;
import org.realityforge.replicant.example.client.event.tyrell.BuildingDataLoadedEvent;
import org.realityforge.replicant.example.client.event.tyrell.BuildingDataLoadedEventHandler;
import org.realityforge.replicant.example.client.service.TyrellGwtRpcAsyncCallback;
import org.realityforge.replicant.example.client.service.tyrell.GwtRpcBuildingService;
import org.realityforge.replicant.example.client.services.DataLoaderService;

public class SimpleUI
  extends Composite
  implements EntityChangeListener
{
  private static final Logger LOG = Logger.getLogger( SimpleUI.class.getName() );

  private final Tree _tree;
  private final Button _create;
  private final Button _update;
  private final Button _connect;
  private final Button _disconnect;
  private final Map<Object, TreeItem> _viewMap = new HashMap<>();
  private final Label _selected;
  private final TextBox _input;
  private final DataLoaderService _dataLoaderService;
  private final GwtRpcBuildingService _buildingService;
  private final EntityChangeBroker _broker;
  private Building _selectedBuilding;
  private Room _selectedRoom;

  @Inject
  public SimpleUI( final EntityChangeBroker broker,
                   final DataLoaderService dataLoaderService,
                   final GwtRpcBuildingService buildingService,
                   final EventBus eventBus )
  {
    super();

    _broker = broker;
    _dataLoaderService = dataLoaderService;
    _buildingService = buildingService;
    _input = new TextBox();
    _input.setValue( "MyBuilding" );
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

    final VerticalPanel panel = new VerticalPanel();
    final FlowPanel controls = new FlowPanel();
    controls.add( _connect );
    controls.add( _disconnect );
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

    _tree.addSelectionHandler( new SelectionHandler<TreeItem>()
    {
      @Override
      public void onSelection( final SelectionEvent<TreeItem> event )
      {
        onSelect( event.getSelectedItem().getUserObject() );
      }
    } );
    _broker.addChangeListener( this );
    eventBus.addHandler( BuildingDataLoadedEvent.TYPE, new BuildingDataLoadedEventHandler()
    {
      @Override
      public void onBuildingDataLoaded( final BuildingDataLoadedEvent event )
      {
        createBuilding( event.getBuilding() );
      }
    } );
    initWidget( panel );
  }

  private void createBuilding( final Building building )
  {
    final TreeItem treeItem = _tree.addItem( createBuildingWidget( building ) );
    treeItem.setUserObject( building );
    treeItem.setState( true );
    _viewMap.put( building, treeItem );
  }

  private Widget createBuildingWidget( final Building building )
  {
    final HorizontalPanel panel = new HorizontalPanel();
    panel.add( new Label( building.getName() ) );
    final Button delete = new Button( "X" );
    delete.addClickHandler( new ClickHandler()
    {
      @Override
      public void onClick( final ClickEvent event )
      {
        doDeleteBuilding( building );
      }
    } );
    panel.add( delete );
    return panel;
  }

  private void doDeleteBuilding( final Building building )
  {
    _buildingService.removeBuilding( building.getID() );
    if( _selectedBuilding == building )
    {
      onSelect( null );
    }
  }

  private void onSelect( final Object userObject )
  {
    _selectedBuilding = null;
    _selectedRoom = null;
    if ( userObject instanceof Building )
    {
      _selectedBuilding = (Building) userObject;
      _selected.setText( "Selected Building: " + _selectedBuilding.getName() );
      _create.setText( "Create Room" );
      _create.setEnabled( true );
      _update.setEnabled( true );
    }
    else if ( userObject instanceof Room )
    {
      _selectedRoom = (Room) userObject;
      _selected.setText( "Selected Room: " + _selectedRoom.getName() );
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
    if ( null != _selectedBuilding )
    {
      _buildingService.setBuildingName( _selectedBuilding.getID(), _input.getValue() );
    }
    else
    {
      _buildingService.setRoomName( _selectedRoom.getID(), _input.getValue() );
    }
    _input.setValue( "" );
  }

  private void onCreate()
  {
    if ( null == _selectedBuilding )
    {
      _buildingService.createBuilding( _input.getValue(), new TyrellGwtRpcAsyncCallback<Integer>()
      {
        @Override
        public void onSuccess( final Integer result )
        {
          _dataLoaderService.subscribeToBuilding( result );
        }
      } );
    }
    else
    {
      _buildingService.createRoom( _selectedBuilding.getID(), 1, 1, _input.getValue(), true );
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
    _tree.clear();
    _disconnect.setEnabled( false );
  }

  private void doConnect()
  {
    _connect.setEnabled( false );
    _create.setEnabled( true );
    _input.setEnabled( true );
    _disconnect.setEnabled( true );
    _tree.clear();
    _dataLoaderService.connect();
  }

  @Override
  public void entityAdded( final EntityChangeEvent event )
  {
    LOG.info( "entityAdded(" + event + ")" );
    final Object entity = event.getObject();
    if ( entity instanceof Building )
    {
      createBuilding( (Building) entity );
    }
  }

  @Override
  public void entityRemoved( final EntityChangeEvent event )
  {
    LOG.info( "entityRemoved(" + event + ")" );
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
    LOG.info( "attributeChanged(" + event + ")" );
    final Object entity = event.getObject();
    if ( entity instanceof Building )
    {
      final Building building = (Building) entity;
      final String name = building.getName();
      final TreeItem treeItem = _viewMap.get( building );
      if ( null != treeItem )
      {
        treeItem.setWidget( createBuildingWidget( building ) );
      }
    }
    else if ( entity instanceof Room )
    {
      final Room room = (Room) entity;
      final TreeItem treeItem = _viewMap.get( room );
      if ( null != treeItem )
      {
        treeItem.setWidget( createRoomWidget( room ) );
      }
    }
  }

  @Override
  public void relatedAdded( final EntityChangeEvent event )
  {
    LOG.info( "relatedAdded(" + event + ")" );
    final Object value = event.getValue();
    final Object object = event.getObject();
    if ( object instanceof Building && value instanceof Room )
    {
      final Room room = (Room) value;
      final Building building = (Building) object;
      final TreeItem parent = _viewMap.get( building );
      if ( null != parent )
      {
        addRoom( parent, room );
      }
    }
  }

  private void addRoom( final TreeItem parent, final Room room )
  {
    final TreeItem treeItem = parent.addItem( createRoomWidget( room ) );
    treeItem.setUserObject( room );
    _viewMap.put( room, treeItem );
  }

  private Widget createRoomWidget( final Room room )
  {
    final HorizontalPanel panel = new HorizontalPanel();
    panel.add( new Label( room.getName() ) );
    final Button delete = new Button( "X" );
    delete.addClickHandler( new ClickHandler()
    {
      @Override
      public void onClick( final ClickEvent event )
      {
        doDeleteRoom( room );
      }
    } );
    panel.add( delete );
    return panel;
  }

  private void doDeleteRoom( final Room room )
  {
    _buildingService.removeRoom( room.getID() );
    if( _selectedRoom == room )
    {
      onSelect( null );
    }
  }

  @Override
  public void relatedRemoved( final EntityChangeEvent event )
  {
    LOG.info( "relatedRemoved(" + event + ")" );
    final Object value = event.getValue();
    final Object object = event.getObject();
    if ( object instanceof Building && value instanceof Room )
    {
      final Room room = (Room) value;
      final TreeItem treeItem = _viewMap.remove( room );
      if ( null != treeItem )
      {
        treeItem.remove();
      }
    }
  }
}
