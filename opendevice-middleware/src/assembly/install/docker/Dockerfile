FROM openjdk:alpine
MAINTAINER Ricardo JL Rufino <ricardo.jl.rufino@gmail.com>
RUN apk add --no-cache wget bash curl \
    && wget -O - https://goo.gl/mgQGNH | bash

#RUN apk add --no-cache wget bash curl

# Expose default port
EXPOSE 8181 1883
# Set the default command
ENTRYPOINT ["/opt/opendevice/bin/opendevice", "start-fg"]

# Options
# docker build -t opendevice-server .
# docker run -d -p 8181:8181 -p 1883:1883 --name opendevice_instance -t opendevice-server
# docker run --entrypoint="/opt/opendevice/bin/opendevice" --name opendevice_instance -dit -p 8181:8181 opendevice-server:custom start-fg