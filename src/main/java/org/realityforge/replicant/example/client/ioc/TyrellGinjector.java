package org.realityforge.replicant.example.client.ioc;

import com.google.gwt.inject.client.GinModules;
import org.realityforge.replicant.example.client.ui.SimpleUI;

@GinModules( { TyrellUserExperienceModule.class, TyrellModule.class } )
public interface TyrellGinjector
  extends AbstractTyrellGinjector
{
  SimpleUI getSimpleUI();
}
