package org.realityforge.tyrell.client.model;

import org.realityforge.arez.annotations.Action;
import org.realityforge.arez.annotations.ArezComponent;
import org.realityforge.arez.annotations.Observable;

@ArezComponent( singleton = true )
public class ViewService
{
  private boolean _connected;

  @Observable
  public boolean isConnected()
  {
    return _connected;
  }

  void setConnected( final boolean connected )
  {
    _connected = connected;
  }

  @Action
  public void connect()
  {
    setConnected( true );
  }

  @Action
  public void disconnect()
  {
    setConnected( false );
  }
}
