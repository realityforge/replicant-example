package org.realityforge.replicant.example.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class LoginUI
  extends Composite
{
  interface Binder
    extends UiBinder<Widget, LoginUI>
  {
  }

  private static final Binder UI_BINDER = GWT.create( Binder.class );

  @UiField
  Button _connect;

  private final ApplicationController _controller;

  public LoginUI( final ApplicationController controller )
  {
    _controller = controller;
    initWidget( UI_BINDER.createAndBindUi( this ) );
  }

  void resetState()
  {
    _connect.setEnabled( true );
  }

  @UiHandler( "_connect" )
  void handleClick( final ClickEvent event )
  {
    _connect.setEnabled( false );
    _controller.connect();
  }
}
