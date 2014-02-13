package org.realityforge.replicant.example.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.List;
import org.realityforge.replicant.example.client.entity.Roster;

public class RosterListUI
  extends Composite
{
  interface Binder
    extends UiBinder<Widget, RosterListUI>
  {
  }

  private static final Binder UI_BINDER = GWT.create( Binder.class );

  private final HashMap<String, Roster> _rosters = new HashMap<>();

  @UiField
  Button _select;
  @UiField
  Button _create;
  @UiField
  TextBox _rosterName;
  @UiField
  ListBox _rosterList;
  @UiField
  Button _disconnect;

  private final ApplicationController _controller;

  public RosterListUI( final ApplicationController controller )
  {
    _controller = controller;
    initWidget( UI_BINDER.createAndBindUi( this ) );
  }

  public void setRosters( final List<Roster> rosters )
  {
    final int selectedIndex = _rosterList.getSelectedIndex();
    final String selectedValue = ( -1 != selectedIndex ) ? _rosterList.getValue( selectedIndex ) : null;

    _rosterList.clear();
    _rosters.clear();

    for ( final Roster roster : rosters )
    {
      final String key = String.valueOf( roster.getID() );
      _rosters.put( key, roster );
      _rosterList.addItem( roster.getName(), key );
    }

    final int itemCount = _rosterList.getItemCount();
    _select.setEnabled( 0 != itemCount );
    _rosterList.setEnabled( 0 != itemCount );
    if ( null != selectedValue )
    {
      for ( int i = 0; i < itemCount; i++ )
      {
        if ( _rosterList.getValue( i ).equals( selectedValue ) )
        {
          _rosterList.setSelectedIndex( i );
          break;
        }
      }
    }
  }

  @Override
  protected void onAttach()
  {
    resetState();

    super.onAttach();
  }

  void resetState()
  {
    _rosterName.setEnabled( true );
    _rosterName.setValue( "" );
    _create.setEnabled( false );

    final int itemCount = _rosterList.getItemCount();
    _select.setEnabled( 0 != itemCount );
    _rosterList.setEnabled( 0 != itemCount );
  }

  @UiHandler( "_disconnect" )
  void handleRosterNameChange( final ClickEvent event )
  {
    _controller.disconnect();
  }

  @UiHandler( "_rosterName" )
  void handleRosterNameChange( final KeyUpEvent event )
  {
    _create.setEnabled( 0 != _rosterName.getValue().length() );
  }

  @UiHandler( "_create" )
  void createRoster( final ClickEvent event )
  {
    final int rosterType = 1;
    _controller.createAndSelectRoster( rosterType, _rosterName.getValue() );
    disableControls();
  }

  @UiHandler( "_select" )
  void selectRoster( final ClickEvent event )
  {
    final String selectedValue = _rosterList.getValue( _rosterList.getSelectedIndex() );
    final Roster roster = _rosters.get( selectedValue );

    _controller.selectRoster( roster );

    disableControls();
  }

  private void disableControls()
  {
    _rosterName.setEnabled( false );
    _select.setEnabled( false );
    _create.setEnabled( false );
    _rosterList.setEnabled( false );
  }
}
