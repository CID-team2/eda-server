FROM centos:7.9.2009

RUN yum update -y && \
    yum install -y git
# JDK download URL can be found on http://jdk.java.net/16/
RUN curl https://download.java.net/java/GA/jdk16.0.2/d4a915d82b4c4fbb9bde534da945d746/7/GPL/openjdk-16.0.2_linux-x64_bin.tar.gz -o openjdk-16.tar.gz && \
    tar -xvf openjdk-16.tar.gz && \
    mv jdk-16.0.2 /opt/

ENV JAVA_HOME /opt/jdk-16.0.2
ENV PATH $PATH:$JAVA_HOME/bin

# disable cache by giving the argument (with time)
ARG CACHEBUST

RUN git clone -b develop https://github.com/CID-team2/eda-server.git /opt/eda-server && \
    cd /opt/eda-server && \
    chmod +x gradlew && \
    ./gradlew bootJar

WORKDIR /opt/eda-server
# NOTE: This is a temporary workaround
COPY src/main/resources/application.properties ./src/main/resources/

CMD ./gradlew bootRun
