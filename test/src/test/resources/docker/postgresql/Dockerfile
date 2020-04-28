FROM debezium/postgres:12
COPY init.sql /docker-entrypoint-initdb.d/
ENV POSTGRES_HOST_AUTH_METHOD "trust"
USER postgres