package org.realityforge.replicant.example.server.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.replicant.server.transport.Packet;
import org.realityforge.replicant.server.transport.PacketQueue;
import org.realityforge.ssf.SimpleSessionInfo;

public class TyrellSessionInfo
  extends SimpleSessionInfo
{
  private final TyrellInterestManager _interestManager = new TyrellInterestManager();

  public TyrellSessionInfo( @Nonnull final String sessionID,
                            @Nonnull final String username )
  {
    super( sessionID, username );
  }

  public TyrellInterestManager getInterestManager()
  {
    return _interestManager;
  }

  @Nullable
  public synchronized Packet poll( final int ack )
  {
    updateAccessTime();
    final PacketQueue queue = getInterestManager().getQueue();
    queue.ack( ack );
    return queue.nextPacketToProcess();
  }
}
