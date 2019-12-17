[![Codacy Badge](https://api.codacy.com/project/badge/Grade/275a68c40e08400388e5e4b5bc7ffcaf)](https://www.codacy.com/manual/chrisbeach/discourse-from-twitter?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=chrisbeach/discourse-from-twitter&amp;utm_campaign=Badge_Grade)

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
    
    
### Notes

* `src/universal/conf/application.ini` contains arguments including the config file location