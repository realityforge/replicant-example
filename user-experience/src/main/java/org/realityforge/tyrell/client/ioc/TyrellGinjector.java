package org.realityforge.tyrell.client.ioc;

import com.google.gwt.inject.client.GinModules;
import org.realityforge.tyrell.client.ui.SimpleUI;

@GinModules( { TyrellUserExperienceModule.class, TyrellModule.class } )
public interface TyrellGinjector
  extends AbstractTyrellGinjector
{
  SimpleUI getSimpleUI();
}
