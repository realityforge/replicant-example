package org.realityforge.replicant.example.server.service;

import javax.annotation.Nonnull;
import org.realityforge.replicant.example.server.entity.TyrellSession;
import org.realityforge.ssf.SimpleSessionInfo;

public class TyrellSessionInfo
  extends SimpleSessionInfo
{
  private final TyrellSession _session = new TyrellSession();

  public TyrellSessionInfo( @Nonnull final String sessionID )
  {
    super( sessionID );
  }

  public TyrellSession getSession()
  {
    return _session;
  }
}
