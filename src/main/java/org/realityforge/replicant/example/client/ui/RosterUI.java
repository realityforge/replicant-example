package org.realityforge.replicant.example.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.realityforge.gwt.datatypes.client.date.RDate;
import org.realityforge.replicant.client.EntityChangeEvent;
import org.realityforge.replicant.client.EntityChangeListener;
import org.realityforge.replicant.example.client.entity.Person;
import org.realityforge.replicant.example.client.entity.Position;
import org.realityforge.replicant.example.client.entity.Roster;
import org.realityforge.replicant.example.client.entity.Shift;

public class RosterUI
  extends Composite
  implements EntityChangeListener
{
  private static final Logger LOG = Logger.getLogger( RosterUI.class.getName() );
  private static final Level LOG_LEVEL = Level.FINE;

  interface Binder
    extends UiBinder<Widget, RosterUI>
  {
  }

  private static final Binder UI_BINDER = GWT.create( Binder.class );

  private final Map<Object, TreeItem> _viewMap = new HashMap<Object, TreeItem>();


  @UiField
  Label _rosterName;
  @UiField
  Tree _tree;
  @UiField
  FlexTable _rosterData;
  @UiField
  Label _shiftName;
  @UiField
  Panel _shiftPanel;
  @UiField
  TextBox _shiftNameEdit;
  @UiField
  TextBox _positionNameCreate;
  @UiField
  VerticalPanel _rosterPanel;
  @UiField
  TextBox _rosterNameEdit;
  @UiField
  TextBox _shiftNameCreate;
  @UiField
  VerticalPanel _positionPanel;
  @UiField
  TextBox _positionNameEdit;
  @UiField
  Button _downloadAll;
  @UiField
  ListBox _resourceList;
  @UiField
  Button _assignResource;
  @UiField
  Button _createShift;
  @UiField
  DateBox _shiftDateCreate;
  @UiField
  Label _filterStart;

  private Roster _roster;
  private Shift _shift;
  private Position _position;

  private final ApplicationController _controller;

  public RosterUI( final ApplicationController controller )
  {
    _controller = controller;
    initWidget( UI_BINDER.createAndBindUi( this ) );
    final com.google.gwt.i18n.client.DateTimeFormat format =
      com.google.gwt.i18n.client.DateTimeFormat.getFormat(
        com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat.YEAR_MONTH_DAY );
    _shiftDateCreate.setFormat( new DefaultFormat( format ) );
  }

  @UiHandler( "_tree" )
  void onSelection( final SelectionEvent<TreeItem> event )
  {
    final Object model = event.getSelectedItem().getUserObject();
    if ( model instanceof Roster )
    {
      final Roster roster = (Roster) model;
      _rosterNameEdit.setText( roster.getName() );
      _shiftNameCreate.setText( null );
      _controller.unloadResources();

      updateCreateShiftButtonStatus();
      updateFilterLabel();
      _rosterPanel.setVisible( true );
      _shiftPanel.setVisible( false );
      _positionPanel.setVisible( false );
    }
    else if ( model instanceof Shift )
    {
      final Shift shift = (Shift) model;
      _controller.unloadResources();
      _controller.selectShift( shift );
      _position = null;
      _shiftNameEdit.setText( shift.getName() );
      _positionNameCreate.setText( null );

      _rosterPanel.setVisible( false );
      _shiftPanel.setVisible( true );
      _positionPanel.setVisible( false );
    }
    else if ( model instanceof Position )
    {
      final Position position = (Position) model;
      _position = position;

      _positionNameEdit.setText( position.getName() );
      _controller.loadResources();

      _rosterPanel.setVisible( false );
      _shiftPanel.setVisible( false );
      _positionPanel.setVisible( true );
    }
    else
    {
      _controller.unloadResources();
      _controller.selectShift( null );
      _position = null;
      _rosterPanel.setVisible( false );
      _shiftPanel.setVisible( false );
      _positionPanel.setVisible( false );
    }
  }

  private void updateFilterLabel()
  {
    _filterStart.setText( _controller.getCurrentDate().toString() );
  }

  @UiHandler("_moveToPast")
  void moveToPast( final ClickEvent event )
  {
    _controller.updateShiftListSubscription( _controller.getCurrentDate().addDays( -1 ) );
  }

  @UiHandler("_moveToFuture")
  void moveToFuture( final ClickEvent event )
  {
    _controller.updateShiftListSubscription( _controller.getCurrentDate().addDays( 1 ) );
  }

  @UiHandler("_assignResource")
  void setAssignResource( final ClickEvent event )
  {
    final int selectedIndex = _resourceList.getSelectedIndex();
    if ( -1 != selectedIndex )
    {
      _controller.assignResource( _position, Integer.parseInt( _resourceList.getValue( selectedIndex ) ) );
    }
  }

  @UiHandler("_updateRosterName")
  void setRosterName( final ClickEvent event )
  {
    _controller.setRosterName( _roster, _rosterNameEdit.getValue() );
  }

  @UiHandler("_shiftNameCreate")
  void onShiftNameChange( final KeyUpEvent event )
  {
    updateCreateShiftButtonStatus();
  }

  @UiHandler("_shiftDateCreate")
  void onShiftDateChange( final ValueChangeEvent<Date> event )
  {
    updateCreateShiftButtonStatus();
  }

  private void updateCreateShiftButtonStatus()
  {
    final String name = _shiftNameCreate.getValue();
    final Date date = _shiftDateCreate.getValue();
    final boolean enabled = null != name && !"".equals( name ) && null != date;
    _createShift.setEnabled( enabled );
  }

  @UiHandler("_createShift")
  void createShift( final ClickEvent event )
  {
    _controller.createShift( _roster, _shiftNameCreate.getValue(), RDate.fromDate( _shiftDateCreate.getValue() ) );
    _shiftNameCreate.setText( null );
    _shiftDateCreate.setValue( null );
  }

  @UiHandler("_updateShiftName")
  void setShiftName( final ClickEvent event )
  {
    _controller.setShiftName( _shift, _shiftNameEdit.getValue() );
  }

  @UiHandler("_createPosition")
  void createPosition( final ClickEvent event )
  {
    _controller.createPosition( _shift, _positionNameCreate.getValue() );
    _positionNameCreate.setText( null );
  }

  @UiHandler("_updatePositionName")
  void setPositionName( final ClickEvent event )
  {
    _controller.setPositionName( _position, _positionNameEdit.getValue() );
  }

  @UiHandler("_downloadAll")
  void onDownloadAll( final ClickEvent event )
  {
    _controller.downloadAll();
  }

  @UiHandler("_disconnect")
  void onDisconnect( final ClickEvent event )
  {
    _controller.disconnect();
  }

  @UiHandler("_delete")
  void onDeleteRoster( final ClickEvent event )
  {
    _controller.doDeleteRoster( _roster );
    setRoster( null );
  }

  public void setRoster( final Roster roster )
  {
    if ( null != _roster )
    {
      _tree.clear();
      _viewMap.clear();
      _controller.getBroker().purgeChangeListener( this );
      _rosterName.setText( "" );
      _controller.selectShift( null );
    }
    _roster = roster;
    if ( null != _roster )
    {
      setRosterLabel( _roster );
      final TreeItem rosterNode = addRoster( _roster );
      for ( final Shift shift : _roster.getShifts() )
      {
        addShift( rosterNode, shift );
      }
      _controller.getBroker().addChangeListener( this );
    }
  }

  public void setShift( final Shift shift )
  {
    LOG.warning( "setShift(" + shift + ")" );
    _shift = shift;
    _shiftName.setText( null != _shift ? _shift.getName() : "" );
    rebuildRosterData();
  }

  private void rebuildRosterData()
  {
    _rosterData.removeAllRows();
    if ( null != _shift )
    {
      final List<Position> positions = _shift.getPositions();
      final int size = positions.size();
      for ( int i = 0; i < size; i++ )
      {
        final Position position = positions.get( i );
        _rosterData.setText( i, 0, position.getName() );
      }
    }
  }

  private TreeItem addRoster( final Roster roster )
  {
    final TreeItem treeItem = _tree.addItem( createRosterWidget( roster ) );
    treeItem.setUserObject( roster );
    treeItem.setState( true );
    _viewMap.put( roster, treeItem );
    return treeItem;
  }

  private Widget createRosterWidget( final Roster roster )
  {
    final HorizontalPanel panel = new HorizontalPanel();
    panel.add( new Label( roster.getName() ) );
    return panel;
  }

  @Override
  public void entityAdded( final EntityChangeEvent event )
  {
    LOG.log( LOG_LEVEL, "entityAdded(" + event + ")" );
    final Object entity = event.getObject();
    if ( entity instanceof Roster )
    {
      addRoster( (Roster) entity );
    }
    else if ( entity instanceof Person )
    {
      final Person person = (Person) entity;
      boolean found = false;
      final int itemCount = _resourceList.getItemCount();
      for ( int i = 0; i < itemCount; i++ )
      {
        if ( person.getID().toString().equals( _resourceList.getValue( i ) ) )
        {
          found = true;
          break;
        }
      }
      if ( !found )
      {
        _resourceList.addItem( person.getName(), person.getID().toString() );
      }
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
    if ( entity instanceof Person )
    {
      final Person person = (Person) entity;
      final int itemCount = _resourceList.getItemCount();
      for ( int i = 0; i < itemCount; i++ )
      {
        if ( person.getID().toString().equals( _resourceList.getValue( i ) ) )
        {
          _resourceList.removeItem( i );
          break;
        }
      }
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
      setRosterLabel( roster );
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
      rebuildRosterData();
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
    _controller.removePosition( position );
  }

  private void setRosterLabel( final Roster roster )
  {
    _rosterName.setText( "Roster: " + roster.getName() );
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
      if ( shift == _shift )
      {
        rebuildRosterData();
      }
    }
  }

  @Override
  public void relatedRemoved( final EntityChangeEvent event )
  {
    LOG.log( LOG_LEVEL, "relatedRemoved(" + event + ")" );
    final Object value = event.getValue();
    final Object object = event.getObject();
    if (
      ( object instanceof Roster && value instanceof Shift ) ||
      ( object instanceof Shift && value instanceof Position )
      )
    {
      final TreeItem treeItem = _viewMap.remove( value );
      if ( null != treeItem )
      {
        treeItem.remove();
      }
      if ( value instanceof Position )
      {
        rebuildRosterData();
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
    final DateTimeFormat dtf = DateTimeFormat.getFormat( PredefinedFormat.DATE_TIME_SHORT );
    panel.add( new Label( dtf.format( shift.getStartAt() ) + " - " + shift.getName() ) );
    final Button delete = new Button( "X" );
    delete.addClickHandler( new ClickHandler()
    {
      @Override
      public void onClick( final ClickEvent event )
      {
        _controller.doDeleteShift( shift );
      }
    } );
    panel.add( delete );
    return panel;
  }
}
