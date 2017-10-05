package org.realityforge.tyrell.client.ioc;

import com.google.gwt.inject.client.GinModules;
import org.realityforge.tyrell.client.net.FrontendContext;
import org.realityforge.tyrell.client.ui.SimpleUI;

@GinModules( { TyrellUserExperienceModule.class, TyrellEntrypointModule.class } )
public interface TyrellGinjector
  extends AbstractTyrellGinjector<FrontendContext>
{
  SimpleUI getSimpleUI();
}
