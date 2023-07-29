package com.github.xgp.keycloak.backup;

import org.keycloak.provider.Provider;

public interface DatabaseBackupProvider extends Provider {

  void backup();

}
