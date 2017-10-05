package org.realityforge.replicant.example.client.net;

import javax.annotation.Nullable;
import org.realityforge.gwt.datatypes.client.date.RDate;
import org.realityforge.replicant.client.net.gwt.BaseFrontendContext;
import org.realityforge.replicant.example.client.entity.Roster;
import org.realityforge.replicant.example.client.entity.Shift;

public interface FrontendContext
  extends BaseFrontendContext
{
  void selectShift( @Nullable Shift shift );

  void selectRoster( @Nullable Roster roster, @Nullable RDate startOn );
  void loadPeople();
  void unloadPeople();
}
