package com.github.xgp.keycloak.backup.h2;

import com.google.auto.service.AutoService;
import com.github.xgp.keycloak.backup.DatabaseBackupProvider;
import com.github.xgp.keycloak.backup.DatabaseBackupProviderFactory;
import javax.persistence.EntityManager;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config.Scope;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.timer.TimerProvider;

@JBossLog
@AutoService(DatabaseBackupProviderFactory.class)
public class H2BackupProviderFactory implements DatabaseBackupProviderFactory {

  public static final String PROVIDER_ID = "h2-database-backup";

  private String path;
  
  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public DatabaseBackupProvider create(KeycloakSession session) {
    EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
    return new H2BackupProvider(session, em, path);
  }

  @Override
  public void init(Scope config) {
    this.path = config.get("h2-backup-path", "/tmp");
    log.infof("Using path %s for database backups", this.path);
  }

  //private static final int INTERVAL = 1 * 60 * 60 * 1000; //1 HOUR
  private static final int INTERVAL = 60 * 1000; //1 MINUTE
  
  @Override
  public void postInit(KeycloakSessionFactory factory) {
    KeycloakModelUtils.runJobInTransaction(factory, s1 -> {
        TimerProvider timer = s1.getProvider(TimerProvider.class);
        log.infof("Registering database backup task with TimerProvider");
        timer.schedule(() -> {
            KeycloakModelUtils.runJobInTransaction(s1.getKeycloakSessionFactory(), s2 -> {
                log.infof("Running database backup");
                DatabaseBackupProvider backup = s2.getProvider(DatabaseBackupProvider.class);
                backup.backup();
              });
          }, INTERVAL, PROVIDER_ID); //every hour?
      });
  }

  @Override
  public void close() {}
}
