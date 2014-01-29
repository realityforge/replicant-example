package org.realityforge.replicant.server.transport;

public class BadSessionException
  extends Exception
{
  public BadSessionException()
  {
    this( null, null );
  }

  public BadSessionException( final String message )
  {
    this( message, null );
  }

  public BadSessionException( final String message, final Throwable cause )
  {
    super( message, cause );
  }
}
