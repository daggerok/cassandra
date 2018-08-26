FROM openjdk:8u171-jre-alpine3.8
LABEL MAINTAINE='Maksim Kostromin https://github.com/daggerok'
LABEL MAINTAINE='Maksim Kostromin https://github.com/daggerok'
ARG JAVA_OPTS_ARGS='\
 -Djavax.net.debug=ssl \
 -Djava.net.preferIPv4Stack=true \
 -XX:+UnlockExperimentalVMOptions \
 -XX:+UseCGroupMemoryLimitForHeap \
 -XshowSettings:vm '
ARG CASSANDRA_PORT_ARG='9042'
ARG CASSANDRA_KEYSPACE_ARG='demo'
ARG CASSANDRA_CLEAN_DATA_FILES_AT_STARTUP_ARG='true'
ARG HTTP_PORT_ARG='8080'
ENV JAVA_OPTS="${JAVA_OPTS} ${JAVA_OPTS_ARGS}" \
    CASSANDRA_CLEAN_DATA_FILES_AT_STARTUP="${CASSANDRA_CLEAN_DATA_FILES_AT_STARTUP_ARG}" \
    CASSANDRA_KEYSPACE="${CASSANDRA_KEYSPACE_ARG}" \
    CASSANDRA_PORT="${CASSANDRA_PORT_ARG}" \
    HTTP_PORT="${HTTP_PORT_ARG}" \
    PORT="${HTTP_PORT_ARG}"
USER root
RUN apk --no-cache --update add busybox-suid bash curl unzip sudo openssh-client shadow wget \
 && adduser -h /home/cassandra -s /bin/bash -D -u 1025 cassandra wheel \
 && echo 'cassandra ALL=(ALL) NOPASSWD: ALL' >> /etc/sudoers \
 && sed -i 's/.*requiretty$/Defaults !requiretty/' /etc/sudoers \
 && wget --no-cookies \
         --no-check-certificate \
         --header 'Cookie: oraclelicense=accept-securebackup-cookie' \
                  'http://download.oracle.com/otn-pub/java/jce/8/jce_policy-8.zip' \
         -O /tmp/jce_policy-8.zip \
 && unzip -o /tmp/jce_policy-8.zip -d /tmp \
 && mv -f ${JAVA_HOME}/lib/security ${JAVA_HOME}/lib/backup-security || echo 'nothing to backup' \
 && mv -f /tmp/UnlimitedJCEPolicyJDK8 ${JAVA_HOME}/lib/security \
 && apk del busybox-suid openssh-client shadow unzip wget \
 && rm -rf /var/cache/apk/* /tmp/* \
 && chown -R cassandra:wheel /home/cassandra
USER cassandra
WORKDIR /home/cassandra
VOLUME /home/cassandra
ENTRYPOINT /bin/bash ./app.jar
CMD /bin/bash
EXPOSE ${PORT}
HEALTHCHECK --timeout=1s \
            --retries=66 \
            CMD curl -f http://127.0.0.1:${PORT}/cassandra/health || exit 1
COPY --chown=cassandra ./build/libs/*.jar ./app.jar
