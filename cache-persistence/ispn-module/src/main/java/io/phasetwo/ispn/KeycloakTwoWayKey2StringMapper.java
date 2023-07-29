package io.phasetwo.ispn;

import org.infinispan.persistence.keymappers.DefaultTwoWayKey2StringMapper;
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.MarshallUtil;
import org.infinispan.commons.marshall.SerializeWith;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import org.jboss.logging.Logger;

public class KeycloakTwoWayKey2StringMapper extends DefaultTwoWayKey2StringMapper {
  private static final Logger log = Logger.getLogger(KeycloakTwoWayKey2StringMapper.class);

  private static final char NON_STRING_PREFIX = '\uFEFF';
  private static final char EXTERNALIZER_IDENTIFIER = 'b';

  @Override
  public String getStringMapping(Object key) {
    try {
      if (hasExternalizer(key.getClass())) {
        byte[] b = toBytes(key, getExternalizer(key.getClass()));
        String v = String.valueOf(NON_STRING_PREFIX) + String.valueOf(EXTERNALIZER_IDENTIFIER) + key.getClass().getName() + String.valueOf(NON_STRING_PREFIX) + Base64.getEncoder().encodeToString(b);
        log.infof("Got value %s for key %s", v, key);
        return v;
      } else {
        return super.getStringMapping(key);
      }
    } catch (Exception e) {
      log.warn("Error getStringMapping", e);
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public Object getKeyMapping(String key) {
    try {
      if (key.length() > 0 && key.charAt(0) == NON_STRING_PREFIX) {
        char type = key.charAt(1);
        if (type == EXTERNALIZER_IDENTIFIER) {
          String value = key.substring(2);
          String className = value.substring(0, value.indexOf(NON_STRING_PREFIX));
          value = value.substring(value.indexOf(NON_STRING_PREFIX)+1);
          log.infof("Got type %c className %s value %s for key %s", type, className, value, key);
          byte[] b = Base64.getDecoder().decode(value);
          return fromBytes(b, getExternalizer(Class.forName(className)));
        }
      }
      return super.getKeyMapping(key);
    } catch (Exception e) {
      log.warn("Error getKeyMapping", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isSupportedType(Class<?> keyType) {
    return hasExternalizer(keyType) || super.isSupportedType(keyType);
  }

  boolean hasExternalizer(Class<?> keyType) {
    return getExternalizer(keyType) != null;
  }
  
  static Externalizer getExternalizer(Class keyType) {
    SerializeWith ann = (SerializeWith)keyType.getAnnotation(SerializeWith.class);
    if (ann != null) {
      try {
        return ann.value().newInstance();
      } catch (Exception e) {
        log.warn("Error getExternalizer", e);
      }
    }
    return null;
  }

  //  static <T> T fromBytes(byte[] data, Externalizer<T> ext) throws IOException, ClassNotFoundException {
  static Object fromBytes(byte[] data, Externalizer ext) throws IOException, ClassNotFoundException {
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
    Object o = ext.readObject(ois);
    ois.close();
    return o;
  }

  //  static <T> byte[] toBytes(T o, Externalizer<T> ext) throws IOException {
  static byte[] toBytes(Object o, Externalizer ext) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    ext.writeObject(oos, o);
    oos.close();
    return baos.toByteArray(); 
  }

}
