package org.realityforge.replicant.example.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.web.bindery.event.shared.EventBus;
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
  private final Button _createBuilding;
  private Building _selectedBuilding;
  private Room _selectedRoom;

  @Inject
  public SimpleUI( final EntityChangeBroker broker,
                   final DataLoaderService dataLoaderService,
                   final GwtRpcBuildingService buildingService,
                   final EventBus eventBus )
  {
    super();

    final TextBox input = new TextBox();
    input.setValue( "Greetings!" );

    _connect = new Button( "Connect", new ClickHandler()
    {
      @Override
      public void onClick( final ClickEvent event )
      {
        _connect.setEnabled( false );
        _createBuilding.setEnabled( true );
        _disconnect.setEnabled( true );
        dataLoaderService.connect();
      }
    } );
    _disconnect = new Button( "Disconnect", new ClickHandler()
    {
      @Override
      public void onClick( ClickEvent event )
      {
        dataLoaderService.disconnect();
        _connect.setEnabled( true );
        _createBuilding.setEnabled( false );
        _disconnect.setEnabled( false );
      }
    } );
    _disconnect.setEnabled( false );

    _createBuilding = new Button( "Create and Subscribe to building", new ClickHandler()
    {
      @Override
      public void onClick( ClickEvent event )
      {
        buildingService.createBuilding( input.getValue(), new TyrellGwtRpcAsyncCallback<Integer>()
        {
          @Override
          public void onSuccess( final Integer result )
          {
            dataLoaderService.subscribeToBuilding( result );
          }
        } );

      }
    } );
    _createBuilding.setEnabled( false );

    final VerticalPanel panel = new VerticalPanel();
    {
      final FlowPanel controls = new FlowPanel();
      controls.add( _connect );
      controls.add( _disconnect );
      panel.add( controls );
    }

    {
      final FlowPanel controls = new FlowPanel();
      controls.add( input );
      controls.add( _createBuilding );
      panel.add( controls );
    }
    _tree = new Tree();

    _create = new Button( "Create", new ClickHandler()
    {
      @Override
      public void onClick( ClickEvent event )
      {
        if ( null == _selectedBuilding )
        {
          buildingService.createBuilding( input.getValue(), new TyrellGwtRpcAsyncCallback<Integer>()
          {
            @Override
            public void onSuccess( final Integer result )
            {
              dataLoaderService.subscribeToBuilding( result );
            }
          } );
        }
        else
        {
          buildingService.createRoom( _selectedBuilding.getID(), 1, 1, input.getName(), true );
        }
      }
    } );
    _create.setEnabled( false );

    _update = new Button( "Update", new ClickHandler()
    {
      @Override
      public void onClick( ClickEvent event )
      {
        if ( null != _selectedBuilding )
        {
          buildingService.setBuildingName( _selectedBuilding.getID(), input.getValue() );
        }
        else
        {
          buildingService.setRoomName( _selectedRoom.getID(), input.getName() );
        }
      }
    } );
    _update.setEnabled( false );

    final HorizontalPanel control = new HorizontalPanel();
    control.add( _create );
    control.add( _update );

    panel.add( control );
    panel.add( _tree );

    _tree.addSelectionHandler( new SelectionHandler<TreeItem>()
    {
      @Override
      public void onSelection( final SelectionEvent<TreeItem> event )
      {
        final Object userObject = event.getSelectedItem().getUserObject();
        _selectedBuilding = null;
        _selectedRoom = null;
        if ( userObject instanceof Building )
        {
          _selectedBuilding = (Building) userObject;
          _create.setText( "Create Room" );
          _create.setEnabled( true );
          _update.setEnabled( true );
        }
        else if ( userObject instanceof Room )
        {
          _selectedRoom = (Room) userObject;
          _create.setEnabled( false );
          _update.setEnabled( true );
        }
        else
        {
          _update.setEnabled( false );
        }
      }
    } );
    broker.addChangeListener( this );
    eventBus.addHandler( BuildingDataLoadedEvent.TYPE, new BuildingDataLoadedEventHandler()
    {
      @Override
      public void onBuildingDataLoaded( final BuildingDataLoadedEvent event )
      {
        final TreeItem treeItem = _tree.addTextItem( event.getBuilding().getName() );
        final Building building = event.getBuilding();
        treeItem.setUserObject( building );

      }
    } );
    initWidget( panel );
  }

  @Override
  public void entityRemoved( final EntityChangeEvent event )
  {
    LOG.info( "entityRemoved(" + event + ")" );
  }

  @Override
  public void attributeChanged( final EntityChangeEvent event )
  {
    LOG.info( "attributeChanged(" + event + ")" );
  }

  @Override
  public void relatedAdded( final EntityChangeEvent event )
  {
    LOG.info( "relatedAdded(" + event + ")" );
  }

  @Override
  public void relatedRemoved( final EntityChangeEvent event )
  {
    LOG.info( "relatedRemoved(" + event + ")" );
  }
}
