FROM nimmis/java:14.04-openjdk-8-jre-headless
USER root
ENV DEBIAN_FRONTEND noninteractive

# Add sbt sources and GPG keys.
RUN echo "deb http://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823

# Install sbt (and bc for some reason).
RUN apt-get update && apt-get -y install sbt bc
ENV SBT_HOME /usr/share/sbt/bin/sbt-launch.jar
ENV PATH /usr/share/sbt/bin:$PATH

# Install Spark.
RUN mkdir spark ; wget https://archive.apache.org/dist/spark/spark-2.2.1/spark-2.2.1-bin-hadoop2.7.tgz -q -O - | tar -C spark --strip-components=1 -xvz
ENV SPARK_HOME $HOME/spark
ENV PATH $SPARK_HOME/bin:$SPARK_HOME/sbin:$PATH
ENV PYTHONPATH $SPARK_HOME/python/:$PYTHONPATH
ENV PYTHONPATH $SPARK_HOME/python/lib/py4j-0.10.4-src.zip:$PYTHONPATH

# Copy startup scripts.
COPY ./compose/spark/startSpark.sh /startSpark.sh
COPY ./compose/spark/testSpark.sh /testSpark.sh

# Set Ubuntu's timezone.
RUN set_tz America/Detroit

# Set working dir to app root for running commands.
WORKDIR /app
