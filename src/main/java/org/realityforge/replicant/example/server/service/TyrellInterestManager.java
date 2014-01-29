package org.realityforge.replicant.example.server.service;

public class TyrellInterestManager
  extends org.realityforge.replicant.example.server.entity.TyrellInterestManager
{
  private boolean _interestedInAllRosters;

  public final boolean isInterestedInAllRosters()
  {
    return _interestedInAllRosters;
  }

  public final void setInterestedInAllRosters( final boolean interestedInAllRosters )
  {
    _interestedInAllRosters = interestedInAllRosters;
  }
}
