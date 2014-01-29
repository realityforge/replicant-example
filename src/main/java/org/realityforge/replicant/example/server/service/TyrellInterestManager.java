package org.realityforge.replicant.example.server.service;

import java.util.HashSet;
import org.realityforge.replicant.server.transport.PacketQueue;

public class TyrellInterestManager
{
  private final HashSet<Integer> _rostersOfInterest = new HashSet<>();
  private final PacketQueue _queue = new PacketQueue();
  private boolean _interestedInAllRosters;
  private boolean _interestedInRosterList;
  private boolean _interestedInMetaData;

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
