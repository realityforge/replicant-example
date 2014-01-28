package org.realityforge.replicant.example.server.service;

import java.util.HashSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.replicant.server.transport.Packet;
import org.realityforge.replicant.server.transport.PacketQueue;
import org.realityforge.ssf.SimpleSessionInfo;

public class TyrellSessionInfo
  extends SimpleSessionInfo
{
  private final HashSet<Integer> _rostersOfInterest = new HashSet<>();

  private final PacketQueue _queue = new PacketQueue();
  private boolean _interestedInAllRosters;
  private boolean _interestedInRosterList;
  private boolean _interestedInMetaData;

  public TyrellSessionInfo( @Nonnull final String sessionID,
                            @Nonnull final String username )
  {
    super( sessionID, username );
  }

  public void registerInterest( final int id )
  {
    _rostersOfInterest.add( id );
  }

  public void deregisterInterest( final int id )
  {
    _rostersOfInterest.remove( id );
  }

  public boolean isInterestedInRosterList()
  {
    return _interestedInRosterList;
  }

  public void setInterestedInRosterList( final boolean interestedInRosterList )
  {
    _interestedInRosterList = interestedInRosterList;
  }

  public boolean isRosterInteresting( final int id )
  {
    return _rostersOfInterest.contains( id );
  }

  public boolean isInterestedInMetaData()
  {
    return _interestedInMetaData;
  }

  public final PacketQueue getQueue()
  {
    return _queue;
  }

  @Nullable
  public synchronized Packet poll( final int ack )
  {
    updateAccessTime();
    _queue.ack( ack );
    return _queue.nextPacketToProcess();
  }

  public boolean isInterestedInAllRosters()
  {
    return _interestedInAllRosters;
  }

  public void setInterestedInAllRosters( final boolean interestedInAllRosters )
  {
    _interestedInAllRosters = interestedInAllRosters;
  }

  public void setInterestedInMetaData( final boolean interestedInMetaData )
  {
    _interestedInMetaData = interestedInMetaData;
  }
}
