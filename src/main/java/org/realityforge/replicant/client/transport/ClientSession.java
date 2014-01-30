package org.realityforge.replicant.client.transport;

import javax.annotation.Nonnull;

/**
 * Abstract representation of client session.
 * Simply tracks the session identifier and job sequencing.
 */
public abstract class ClientSession
{
  private final String _sessionID;

  public ClientSession( @Nonnull final String sessionID )
  {
    _sessionID = sessionID;
  }

  public String getSessionID()
  {
    return _sessionID;
  }
}
