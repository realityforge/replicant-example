package org.realityforge.replicant.example.client.ioc;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import org.realityforge.replicant.example.client.ui.SimpleUI;
import tyrell.client.ioc.TyrellGwtRpcServicesModule;
import tyrell.client.ioc.TyrellImitServicesModule;

@GinModules( { BasicModule.class, TyrellImitServicesModule.class, TyrellGwtRpcServicesModule.class } )
public interface TyrellGinjector
  extends Ginjector
{
  SimpleUI getSimpleUI();
}
