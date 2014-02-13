package org.realityforge.replicant.example.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.realityforge.replicant.client.EntityChangeEvent;
import org.realityforge.replicant.client.EntityChangeListener;
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

  private Roster _roster;

  private final ApplicationController _controller;

  public RosterUI( final ApplicationController controller )
  {
    _controller = controller;
    initWidget( UI_BINDER.createAndBindUi( this ) );
  }

  @UiHandler( "_disconnect" )
  void handleRosterNameChange( final ClickEvent event )
  {
    _controller.disconnect();
  }

  @UiHandler( "_delete" )
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
    }
    _roster = roster;
    if ( null != _roster )
    {
      _rosterName.setText( "Roster: " + _roster.getName() );
      final TreeItem rosterNode = addRoster( _roster );
      for ( final Shift shift : _roster.getShifts() )
      {
        addShift( rosterNode, shift );
      }
      _controller.getBroker().addChangeListener( _roster, this );
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
    final DateTimeFormat dtf = DateTimeFormat.getFormat( PredefinedFormat.DATE_TIME_FULL );
    panel.add( new Label( shift.getName() + " - Started At " + dtf.format( shift.getStartAt() ) ) );
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
