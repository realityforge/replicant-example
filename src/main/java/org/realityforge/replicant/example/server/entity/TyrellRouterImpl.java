package org.realityforge.replicant.example.server.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

@Dependent
public class TyrellRouterImpl
  implements TyrellRouter
{
  public static final String ROSTER_KEY = "Tyrell.RosterID";

  @Nonnull
  @Override
  public Map<String, Serializable> routeRoster( @Nonnull final Roster entity )
  {
    final HashMap<String, Serializable> map = new HashMap<>();
    map.put( ROSTER_KEY, entity.getID() );
    return map;
  }

  @Nonnull
  @Override
  public Map<String, Serializable> routeShift( @Nonnull final Shift entity )
  {
    return routeRoster( entity.getRoster() );
  }
}
