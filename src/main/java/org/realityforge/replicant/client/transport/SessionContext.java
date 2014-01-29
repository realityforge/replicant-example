package org.realityforge.replicant.client.transport;

public final class SessionContext
{
  private SessionContext()
  {
  }

  private static String c_sessionID;

  public static void setSessionID( final String sessionID )
  {
    c_sessionID = sessionID;
  }

  public static String getSessionID()
  {
    return c_sessionID;
  }
}
