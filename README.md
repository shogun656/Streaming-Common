# Streaming-Common
A Spark Streaming Starter Application
## Setup
The local environment has a container to run Spark. All containers are indirectly based on Ubuntu 14.04 LTS

1. Install Docker for Mac Beta.
1. Build your environment with `docker-compose build`.
1. To run the Spark app fully locally, run `docker-compose up`.
1. To execute tests, run `docker-compose run spark /testSpark.sh`.
