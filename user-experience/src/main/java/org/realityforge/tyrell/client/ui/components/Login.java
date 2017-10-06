package org.realityforge.tyrell.client.ui.components;

import javax.annotation.Nullable;
import org.realityforge.arez.annotations.ArezComponent;
import org.realityforge.tyrell.client.model.AppData;
import react4j.annotations.EventHandler;
import react4j.annotations.ReactComponent;
import react4j.arez.ReactArezComponent;
import react4j.core.BaseProps;
import react4j.core.BaseState;
import react4j.core.ReactElement;
import react4j.dom.events.MouseEventHandler;
import react4j.dom.proptypes.html.BtnProps;
import static org.realityforge.tyrell.client.ui.components.Login_.*;
import static react4j.dom.DOM.*;

@ReactComponent
@ArezComponent( allowEmpty = true )
public class Login
  extends ReactArezComponent<BaseProps, BaseState>
{
  @EventHandler( MouseEventHandler.class )
  void doLogin()
  {
    AppData.controller.connect();
  }

  @Nullable
  @Override
  protected ReactElement<?, ?> doRender()
  {
    return div(
      h1( "Welcome to the Replicant Example Application" ),
      button( new BtnProps().onClick( _doLogin( this ) ), "Connect" )
    );
  }
}
