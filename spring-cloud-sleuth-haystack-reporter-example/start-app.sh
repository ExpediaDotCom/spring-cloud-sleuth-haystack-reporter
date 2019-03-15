#!/usr/bin/env bash
set -exo pipefail
set -o errexit

[[ -z "${JAVA_XMS}" ]] && JAVA_XMS=128m
[[ -z "${JAVA_XMX}" ]] && JAVA_XMX=512m
[[ -z "${APP_NAME}" ]] && APP_NAME=opentracing-spring-haystack-example
[[ -z "${APP_HOME}" ]] && APP_HOME=.
[[ -z "${APP_MODE}" ]] && APP_MODE=backend
[[ -z "${BACKEND_URL}" ]] && BACKEND_URL=http://localhost:9091

JAVA_OPTS="${JAVA_OPTS} \
-XX:+UseG1GC \
-Xloggc:/var/log/gc.log \
-XX:+PrintGCDetails \
-XX:+PrintGCDateStamps \
-XX:+UseGCLogFileRotation \
-XX:NumberOfGCLogFiles=5 \
-XX:GCLogFileSize=2M \
-Xmx${JAVA_XMX} \
-Xms${JAVA_XMS} \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.port=1098 \
-Dspring.profiles.active=${SPRING_PROFILE}
-Dbackend.url=${BACKEND_URL}"

echo "Starting java ${JAVA_OPTS} -jar ${APP_HOME}/${APP_NAME}.jar ${APP_MODE}"
exec java ${JAVA_OPTS} -jar "${APP_HOME}/${APP_NAME}.jar" ${APP_MODE}
