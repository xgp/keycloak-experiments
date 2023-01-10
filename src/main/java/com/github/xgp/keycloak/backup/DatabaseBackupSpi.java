package com.github.xgp.keycloak.backup;

import com.google.auto.service.AutoService;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

@AutoService(Spi.class)
public class DatabaseBackupSpi implements Spi {

  @Override
  public boolean isInternal() {
    return false;
  }

  @Override
  public String getName() {
    return "databaseBackupProvider";
  }

  @Override
  public Class<? extends Provider> getProviderClass() {
    return DatabaseBackupProvider.class;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Class<? extends ProviderFactory> getProviderFactoryClass() {
    return DatabaseBackupProviderFactory.class;
  }
}
