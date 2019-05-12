## Discourse-From-Twitter

Streams a Twitter search and posts tweets as forum topics on a 
Discourse forum.

## Configuration

Customise [src/main/resources/application.conf.sample](src/main/resources/application.conf.sample) and save as
`src/main/resources/application.conf` 

## Publishing Docker Image

    sbt docker:publishLocal

## Running Using Docker

    docker run -d discourse-from-twitter:0.1
    
