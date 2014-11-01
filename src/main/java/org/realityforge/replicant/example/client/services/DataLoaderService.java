package org.realityforge.replicant.example.client.services;

import org.realityforge.replicant.example.client.net.TyrellDataLoaderService;

public interface DataLoaderService
  extends TyrellDataLoaderService
{
  void downloadAll();
}
