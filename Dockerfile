FROM docker.io/library/alpine:latest as build
ADD . /src
WORKDIR /src
RUN apk update
RUN apk add --no-cache git openjdk8
RUN ./gradlew distRelease

FROM docker.io/library/alpine:latest as runtime
COPY --from=build /src/distribution/output/gdx-texture-packer.jar /usr/share/java/gdx-texture-packer-gui/gdx-texture-packer-gui.jar
RUN apk update
RUN apk add --no-cache openjdk8-jre
ENTRYPOINT [ "/usr/bin/java", "-jar", "/usr/share/java/gdx-texture-packer-gui/gdx-texture-packer-gui.jar" ]
CMD [ "--help" ]
