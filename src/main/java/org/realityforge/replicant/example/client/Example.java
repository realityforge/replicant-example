package org.realityforge.replicant.example.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;
import org.realityforge.replicant.example.client.ioc.TyrellGinjector;

public final class Example
  implements EntryPoint
{
  public void onModuleLoad()
  {
    final TyrellGinjector injector = GWT.create( TyrellGinjector.class );
    RootPanel.get().add( injector.getSimpleUI().asWidget() );
  }
}
