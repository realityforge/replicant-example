package org.realityforge.replicant.server.ee;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class ReplicantContextHolder
{
  private static final ThreadLocal<Map<String, Serializable>> c_context = new ThreadLocal<>();

  private ReplicantContextHolder()
  {
  }

  public static void put( final String key, final Serializable payload )
  {
    if ( null == c_context.get() )
    {
      c_context.set( new HashMap<String, Serializable>() );
    }
    c_context.get().put( key, payload );
  }

  public static Serializable get( final String key )
  {
    return c_context.get().get( key );
  }

  public static void clean()
  {
    c_context.remove();
  }
}