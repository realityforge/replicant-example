package org.realityforge.replicant.example.server.servlet;

import java.io.IOException;
import javax.annotation.Priority;
import javax.ejb.EJB;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.realityforge.ssf.AbstractSessionFilter;
import org.realityforge.ssf.HttpUtil;
import org.realityforge.ssf.SessionManager;

@WebFilter( urlPatterns = "/*" )
@Priority( 100 )
public class SessionFilter
  extends AbstractSessionFilter
{
  @EJB
  private SessionManager _sessionManager;

  @Override
  protected boolean handleInvalid( final HttpServletRequest request,
                                final HttpServletResponse response,
                                final FilterChain chain )
    throws IOException, ServletException
  {
    response.sendRedirect( HttpUtil.getContextURL( request ).append( "/api/auth/login" ).toString() );
    return true;
  }

  @Override
  public SessionManager getSessionManager()
  {
    return _sessionManager;
  }

  @Override
  @SuppressWarnings( { "all" } )
  protected boolean isSessionCheckRequired( final HttpServletRequest request )
    throws IOException, ServletException
  {
    final String method = request.getMethod();
    final String requestURI = HttpUtil.getContextLocalPath( request );
    if ( "GET".equals( method ) )
    {
      if ( requestURI.startsWith( "/images/" ) ||
           requestURI.startsWith( "/stylesheets/" ) ||
           requestURI.startsWith( "/api/auth/" ) ||
           requestURI.equals( "/favicon.ico" ) )
      {
        return false;
      }
      else
      {
        return true;
      }
    }
    else if ( "POST".equals( method ) )
    {
      if ( requestURI.startsWith( "/api/auth/" ) )
      {
        return false;
      }
      else
      {
        return true;
      }
    }
    else
    {
      return false;
    }
  }
}
