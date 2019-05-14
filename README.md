## Discourse-From-Twitter

Streams a Twitter search and posts tweets as forum topics on a 
Discourse forum.

## Configuration

Customise [src/main/resources/application.conf.sample](src/main/resources/application.conf.sample) and save as
`src/main/resources/application.conf` 

## Running (Development)

NOTE: Requires [SBT](https://www.scala-sbt.org/)

    sbt run

## Publishing Docker Image

    sbt docker:publishLocal

## Running Published Image as Docker Container

    docker run -d discourse-from-twitter:0.1
    
