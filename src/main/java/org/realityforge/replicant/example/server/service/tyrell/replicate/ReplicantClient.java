package org.realityforge.replicant.example.server.service.tyrell.replicate;

import java.util.List;
import org.realityforge.replicant.server.EntityMessage;

public interface ReplicantClient
{
  void addPacket( List<EntityMessage> changeSet );
}
