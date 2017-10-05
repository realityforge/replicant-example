package org.realityforge.tyrell.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.realityforge.tyrell.client.ioc.AbstractTyrellGinjector;
import org.realityforge.tyrell.client.ioc.TyrellGinjector;

public class TyrellApp
  extends AbstractTyrellApp
{
  private static final Logger LOG = Logger.getLogger( TyrellApp.class.getName() );

  private TyrellGinjector _injector;

  @Nullable
  @Override
  protected AbstractTyrellGinjector getInjector()
  {
    return _injector;
  }

  @Override
  protected void postStart()
  {
  }

  @Override
  protected void preStart()
  {
    _injector = GWT.create( TyrellGinjector.class );
  }

  @Override
  protected void prepareServices()
  {
  }

  @Override
  protected void prepareUI()
  {
    RootPanel.get().add( _injector.getSimpleUI().asWidget() );
  }

  @Override
  protected void onUncaughtException( final Throwable e )
  {
    LOG.log( Level.WARNING, "Error: " + e, e );
  }
}
