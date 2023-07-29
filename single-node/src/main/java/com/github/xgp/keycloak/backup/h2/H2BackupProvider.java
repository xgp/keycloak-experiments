package com.github.xgp.keycloak.backup.h2;

import com.github.xgp.keycloak.backup.DatabaseBackupProvider;
import javax.persistence.EntityManager;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * http://www.h2database.com/html/tutorial.html#upgrade_backup_restore
 * https://stackoverflow.com/questions/2036117/how-to-back-up-the-embedded-h2-database-engine-while-it-is-running/65843171#65843171
 */
@JBossLog
public class H2BackupProvider implements DatabaseBackupProvider {

  private final KeycloakSession session;
  private final EntityManager em;
  private final String path;
  
  public H2BackupProvider(KeycloakSession session, EntityManager em, String path) {
    this.session = session;
    this.em = em;
    this.path = path;
  }

  private static final String VERSION_QUERY = "SELECT H2VERSION()";
  private static final String BACKUP_QUERY_TEMPLATE = "BACKUP TO '%s'";
  
  @Override
  public void backup() {
    //detect if it's h2
    try {
      Object o = em.createNativeQuery(VERSION_QUERY).getSingleResult();
      log.infof("%s = %s", VERSION_QUERY, o.toString());
    } catch (Exception e) {
      log.warn("Not h2 database type");
      return;
    }

    long unixTime = System.currentTimeMillis() / 1000L;
    String file = String.format("%s/backup-%d.zip", path, unixTime);
    try {
      int i = em.createNativeQuery(String.format(BACKUP_QUERY_TEMPLATE, file)).executeUpdate();
      log.infof("Ran h2 backup to %s -> %d", file, i);
    } catch (Exception e) {
      log.warn("Error running h2 backup", e);
    }                                     
  }

  @Override
  public void close() {}

  //trim backup files to a certain number
  // https://www.logicbig.com/how-to/java-io/delete-old-files.html
}
