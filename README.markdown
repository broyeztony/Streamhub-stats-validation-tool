## Streamhub stats validation tool

The goal of this project is to provide validation tool for your implementation of the Streamhub REST api.

It is a complementary tool to the documentation we provide at https://streamhub5.wordpress.com/docs-3/. 

Factually, it is a Scala/Spray server that you can target in place of the production collector server (stats.streamhub.io) in your api implementation project, and submit requests to.

The tool will evaluate your query and will reply in JSON format telling if fields are missing or holds incorrect values.  


Follow these steps to get started:

1. Git-clone this repository.

        $ git clone git@bitbucket.org:broyeztony/streamhub-stats-collector-validator.git
        

2. Change directory into your clone:

        $ cd streamhub-stats-collector-validator
        

3. Launch SBT. If you don't have SBT, you can install it from http://www.scala-sbt.org/:

        $ sbt
        

4. Start the application:

        > re-start
        

6. Submit your queries to 
[http://localhost:8080/api/player](http://localhost:8080/api/player) or
[http://localhost:8080/api/playerevent](http://localhost:8080/api/playerevent)

> http://localhost:8080/api/player?startTime=1&publicId=unboxing6s&partnerId=jwplatform&analyticsId=streamhub-5812d&playerId=player-demo&isLive=false&refUrl=http%3A%2F%2Flocalhost%2F&locationUrl=http%3A%2F%2Flocalhost%2Fjw%2Findex.html%3Fhtml5&agent=iPhone; CPU iPhone OS 7_0 like Mac OS X&randomSessionKey=9D085974-0FDB-CE34-8AA0-F50B4757FA59&sessionId=F7769FD4-5327-198B-6344-F50B4757FDEB
        
        > http://localhost:8080/api/playerevent?startTime=0&publicId=unboxing6s&partnerId=jwplatform&analyticsId=streamhub-5812d&playerId=player-demo&isLive=false&refUrl=http%3A%2F%2Flocalhost%2F&locationUrl=http%3A%2F%2Flocalhost%2Fjw%2Findex.html%3Fhtml5&agent=Mozilla/5.0%20(Macintosh;%20Intel%20Mac%20OS%20X%2010_10_5)%20AppleWebKit/537.36%20(KHTML,%20like%20Gecko)%20Chrome/49.0.2623.110%20Safari/537.36&randomSessionKey=9D085974-0FDB-CE34-8AA0-F50B4757FA59&sessionId=F7769FD4-5327-198B-6344-F50B4757FDEB&event=player_start


7. Stop the application:

        > re-stop
        

8. You can also check the scripts in the tools/ folder to see usages of the validation server.
