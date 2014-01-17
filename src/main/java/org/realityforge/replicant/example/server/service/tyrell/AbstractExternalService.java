package org.realityforge.replicant.example.server.service.tyrell;

import javax.interceptor.Interceptors;
import org.realityforge.replicant.example.server.service.tyrell.replicate.Replicate;
import org.realityforge.replicant.example.server.service.tyrell.replicate.ReplicationInterceptor;

/**
 * Abstract base class for services exposed to external agents.
 */
@Replicate
@Interceptors( { ReplicationInterceptor.class } )
public abstract class AbstractExternalService
{
}
