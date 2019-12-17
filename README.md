## Discourse-From-Twitter

Streams a Twitter search and posts tweets as forum topics on a 
Discourse forum.

## Configuration

Customise [src/main/resources/application.conf.sample](src/main/resources/application.conf.sample) 
and save as `src/main/resources/application.conf` 

## Publishing Docker Image

    sbt docker:publishLocal

## Running Using Docker

Substitute the path to your application.conf file

    docker run -v [path to]application.conf:/application.conf -d discourse-from-twitter:0.2
    
    
#### Notes

* `src/universal/conf/application.ini` contains arguments including the config file location