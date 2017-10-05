package org.realityforge.tyrell.client.net;

import javax.annotation.Nullable;
import org.realityforge.gwt.datatypes.client.date.RDate;
import org.realityforge.replicant.client.net.gwt.BaseFrontendContext;
import org.realityforge.tyrell.client.entity.Roster;
import org.realityforge.tyrell.client.entity.Shift;

public interface FrontendContext
  extends BaseFrontendContext
{
  void selectShift( @Nullable Shift shift );

  void selectRoster( @Nullable Roster roster, @Nullable RDate startOn );
  void loadPeople();
  void unloadPeople();
}
