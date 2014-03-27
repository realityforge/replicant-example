package org.realityforge.replicant.example.client.ui;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.realityforge.replicant.client.EntityChangeBroker;
import org.realityforge.replicant.client.EntityChangeEvent;
import org.realityforge.replicant.client.EntityChangeListener;
import org.realityforge.replicant.example.client.entity.Position;
import org.realityforge.replicant.example.client.entity.Roster;
import org.realityforge.replicant.example.client.entity.Shift;

public class SimpleUI
  extends Composite
  implements EntityChangeListener
{
  private static final Logger LOG = Logger.getLogger( SimpleUI.class.getName() );
  private static final Level LOG_LEVEL = Level.FINE;

  private final Tree _tree;
  private final Map<Object, TreeItem> _viewMap = new HashMap<Object, TreeItem>();

  @Inject
  public SimpleUI( final ApplicationController applicationController, final EntityChangeBroker broker )
  {
    broker.addChangeListener( this );

    final VerticalPanel panel = new VerticalPanel();
    panel.setWidth( "100%" );
    panel.add( applicationController );

    _tree = new Tree();
    _tree.setHeight( "200px" );
    _tree.setWidth( "100%" );
    _tree.setScrollOnSelectEnabled( true );

    panel.add( _tree );
    initWidget( panel );
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
    return panel;
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
  }

  private Widget createShiftWidget( final Shift shift )
  {
    final HorizontalPanel panel = new HorizontalPanel();
    final DateTimeFormat dtf = DateTimeFormat.getFormat( PredefinedFormat.DATE_TIME_FULL );
    panel.add( new Label( shift.getName() + " - Started At " + dtf.format( shift.getStartAt() ) ) );
    return panel;
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
    return panel;
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
