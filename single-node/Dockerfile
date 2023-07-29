FROM debian:stable-slim as builder
ARG version=20.0.3

USER root

RUN apt update && \
apt install -y default-jre curl supervisor debian-keyring debian-archive-keyring apt-transport-https && \
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg && \
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | tee /etc/apt/sources.list.d/caddy-stable.list && \
apt update && \
apt install -y caddy && \
chmod ugo+rwx /opt && \
useradd keycloak

USER keycloak

RUN cd /opt && \
curl -L https://github.com/keycloak/keycloak/releases/download/$version/keycloak-$version.tar.gz --output keycloak-$version.tar.gz && \
tar zxf keycloak-$version.tar.gz && \
ln -s keycloak-$version keycloak 

COPY ./conf/cache-ispn-persistent.xml /opt/keycloak/conf/cache-ispn-persistent.xml
COPY ./conf/Caddyfile /etc/caddy/Caddyfile
COPY ./conf/supervisord.conf /etc/supervisor/conf.d/supervisord.conf
#COPY ./conf/single-keycloak-entrypoint.sh /opt/keycloak/bin/single-keycloak-entrypoint.sh

WORKDIR /opt/keycloak
# this cert shouldn't be used, as it's just to stop the startup from complaining
#RUN keytool -genkeypair -storepass password -storetype PKCS12 -keyalg RSA -keysize 2048 -dname "CN=server" -alias server -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -keystore conf/server.keystore

# configure keycloak
RUN /opt/keycloak/bin/kc.sh --verbose build --db=dev-file --cache=local --cache-config-file=cache-ispn-persistent.xml --health-enabled=true

USER root
CMD ["/usr/bin/supervisord"]
