# cassandra [![Build Status](https://travis-ci.org/daggerok/cassandra.svg?branch=master)](https://travis-ci.org/daggerok/cassandra)
[Docker image](https://hub.docker.com/r/daggerok/cassandra/) based on [openjdk 8u171 (jre) alpine 3.8](https://hub.docker.com/_/openjdk/), [Spring WebFlux](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html) and [Achilles Embedded Cassandra Server](https://github.com/doanduyhai/Achilles) 

Status: in progress...

- linux alpine 3.8
- java version: openjdk 8u171 jre + jce policy
- spring-boot / webflux version: 2.0.4.RELEASE
- cassandra version: 3.11.2

**Available tags**:

- [`daggerok/kafka:cassandra     (latest)`](https://github.com/daggerok/cassandra/blob/master/Dockerfile)
- [`daggerok/kafka:cassandra-bin (latest)`](https://github.com/daggerok/cassandra/blob/bin/Dockerfile)

**Exposed ports**:

- 9042 - cassandra
- 8080 - http (actuator) endpoints

### Usage:

#### docker

```bash

docker run -p 8080:8080 -p 9042:9042 daggerok/cassandra

# shutdown cassandra:
curl http://localhost:8080/stop
# or:
http post :8080/cassandra/shutdown
# or press Ctrl+C
```

#### Dockerfile

```dockerfile

FROM daggerok/cassandra
ENV HTTP_PORT=8080 \
    CASSANDRA_PORT=9042 \
    CASSANDRA_KEYSPACE='demo' \
    CASSANDRA_CLEAN_DATA_FILES_AT_STARTUP_ARG=true

```

```bash

docker build --no-cache -t my-cassandra .
docker run --rm --name=run-my-cassandra -p 8080:8080 -p 9042:9042 my-cassandra

```

#### docker-compose.yml

```yaml

version: '2.1'
services:
  cassandra:
    image: daggerok/cassandra
    environment:
      HTTP_PORT: 8080
      CASSANDRA_PORT: 9042
      CASSANDRA_KEYSPACE: demo
      CASSANDRA_CLEAN_DATA_FILES_AT_STARTUP_ARG: true
    ports:
    - '8080:8080'
    - '9042:9042'
    volumes:
    - 'data:/home/cassandra'
    networks: [backing-services]
volumes:
  data: {}
networks:
  backing-services:
    driver: bridge

```

```bash

docker-compose up
# ...
docker-compose down -v

```

links:

- [Spring WebFlux](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html)
- [Achilles Embedded Cassandra Server](https://github.com/doanduyhai/Achilles)
