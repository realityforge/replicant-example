package org.realityforge.replicant.example.server.service.tyrell.replicate;

import javax.inject.Inject;
import org.realityforge.replicant.server.EntityMessageGenerator;
import org.realityforge.replicant.server.ee.ChangeRecorder;

public class TyrellChangeRecorder
  extends ChangeRecorder
{
  @Inject
  private EntityMessageGenerator _messageGenerator;

  @Override
  protected EntityMessageGenerator getEntityMessageGenerator()
  {
    return _messageGenerator;
  }
}
