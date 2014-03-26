package org.realityforge.replicant.example.client.services;

import org.realityforge.replicant.example.client.net.TyrellClientSession;

public interface DataLoaderService
{
  boolean isConnected();

  void connect();

  void disconnect();

  TyrellClientSession getSession();

  void downloadAll();
}
