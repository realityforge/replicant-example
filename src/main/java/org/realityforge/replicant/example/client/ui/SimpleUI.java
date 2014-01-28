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
import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.realityforge.replicant.client.EntityChangeBroker;
import org.realityforge.replicant.client.EntityChangeEvent;
import org.realityforge.replicant.client.EntityChangeListener;
import org.realityforge.replicant.example.client.entity.Roster;
import org.realityforge.replicant.example.client.entity.Shift;
import org.realityforge.replicant.example.client.service.GwtRpcRosterService;
import org.realityforge.replicant.example.client.service.TyrellGwtRpcAsyncCallback;
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
  private final GwtRpcRosterService _rosterService;
  private final EntityChangeBroker _broker;
  private final Button _subscribeToAll;
  private final Button _downloadAll;
  private Roster _selectedRoster;
  private Shift _selectedShift;

  @Inject
  public SimpleUI( final EntityChangeBroker broker,
                   final DataLoaderService dataLoaderService,
                   final GwtRpcRosterService rosterService )
  {
    super();

    _broker = broker;
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
    _subscribeToAll = new Button( "Subscribe To All", new ClickHandler()
    {
      @Override
      public void onClick( ClickEvent event )
      {
        doSubscribeToAll();
      }
    } );
    _subscribeToAll.setEnabled( false );
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
    controls.add( _subscribeToAll );
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

    _tree.addSelectionHandler( new SelectionHandler<TreeItem>()
    {
      @Override
      public void onSelection( final SelectionEvent<TreeItem> event )
      {
        onSelect( event.getSelectedItem().getUserObject() );
      }
    } );
    _broker.addChangeListener( this );
    initWidget( panel );
  }

  private void doDownloadAll()
  {
    _dataLoaderService.downloadAll();
  }

  private void doSubscribeToAll()
  {
    _dataLoaderService.subscribeToAll();
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
    if( _selectedRoster == roster )
    {
      onSelect( null );
    }
  }

  private void onSelect( final Object userObject )
  {
    _selectedRoster = null;
    _selectedShift = null;
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
    else
    {
      _rosterService.setShiftName( _selectedShift.getID(), _input.getValue() );
    }
    _input.setValue( "" );
  }

  private void onCreate()
  {
    if ( null == _selectedRoster )
    {
      final int rosterType = 1;
      _rosterService.createRoster( rosterType, _input.getValue(), new TyrellGwtRpcAsyncCallback<Integer>()
      {
        @Override
        public void onSuccess( final Integer result )
        {
          _dataLoaderService.getSubscriptionManager().subscribeToRoster( result );
        }
      } );
    }
    else
    {
      _rosterService.createShift( _selectedRoster.getID(), _input.getValue() );
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
    _subscribeToAll.setEnabled( true );
    _downloadAll.setEnabled( true );
    _tree.clear();
    _dataLoaderService.connect();
  }

  @Override
  public void entityAdded( final EntityChangeEvent event )
  {
    LOG.info( "entityAdded(" + event + ")" );
    final Object entity = event.getObject();
    if ( entity instanceof Roster )
    {
      createRoster( (Roster) entity );
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
  }

  @Override
  public void relatedAdded( final EntityChangeEvent event )
  {
    LOG.info( "relatedAdded(" + event + ")" );
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
  }

  private void addShift( final TreeItem parent, final Shift shift )
  {
    final TreeItem treeItem = parent.addItem( createShiftWidget( shift ) );
    treeItem.setUserObject( shift );
    _viewMap.put( shift, treeItem );
  }

  private Widget createShiftWidget( final Shift shift )
  {
    final HorizontalPanel panel = new HorizontalPanel();
    panel.add( new Label( shift.getName() ) );
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
    if( _selectedShift == shift )
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
    if ( object instanceof Roster && value instanceof Shift )
    {
      final Shift shift = (Shift) value;
      final TreeItem treeItem = _viewMap.remove( shift );
      if ( null != treeItem )
      {
        treeItem.remove();
      }
    }
  }
}
