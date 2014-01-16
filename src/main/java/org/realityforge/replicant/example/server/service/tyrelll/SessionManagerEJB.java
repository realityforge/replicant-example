package org.realityforge.replicant.example.server.service.tyrelll;

import javax.ejb.Local;
import javax.ejb.Singleton;
import org.realityforge.ssf.SessionManager;
import org.realityforge.ssf.SimpleSessionManager;

@Local( SessionManager.class )
@Singleton
public class SessionManagerEJB
  extends SimpleSessionManager
{
}
