package org.realityforge.replicant.client.transport;

public final class SessionContext
{
  private SessionContext()
  {
  }

  private static ClientSession c_session;

  public static ClientSession getSession()
  {
    return c_session;
  }

  public static void setSession( final ClientSession session )
  {
    c_session = session;
  }
}
