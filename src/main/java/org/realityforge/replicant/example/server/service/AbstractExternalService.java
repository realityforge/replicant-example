package org.realityforge.replicant.example.server.service;

import javax.interceptor.Interceptors;
import org.realityforge.replicant.example.server.net.TyrellReplicationInterceptor;
import org.realityforge.replicant.server.ee.Replicate;

/**
 * Abstract base class for services exposed to external agents.
 */
@Replicate
@Interceptors(TyrellReplicationInterceptor.class)
public abstract class AbstractExternalService
{
}
