package org.realityforge.replicant.example.client.ioc;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import org.realityforge.replicant.example.client.ui.SimpleUI;

@GinModules( { TyrellUserExperienceModule.class, TyrellModule.class } )
public interface TyrellGinjector
  extends Ginjector
{
  SimpleUI getSimpleUI();
}
