package org.realityforge.tyrell.server.test.util;

import com.google.inject.Injector;
import javax.annotation.Nonnull;

public class TyrellFactory
  extends AbstractTyrellFactory
{
  public TyrellFactory( @Nonnull final Injector injector )
  {
    super( injector );
  }
}
