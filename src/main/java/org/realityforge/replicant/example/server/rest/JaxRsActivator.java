package org.realityforge.replicant.example.server.rest;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api")
public class JaxRsActivator
  extends Application
{
  @Override
  public Set<Class<?>> getClasses()
  {
    final Set<Class<?>> classes = new HashSet<Class<?>>();
    classes.addAll( super.getClasses() );
    classes.add( AuthenticationService.class );
    classes.add( ReplicantResource.class );
    return classes;
  }
}
