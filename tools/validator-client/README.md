validator-client
==================================

A validation tool for [streamhub-stats-collector-validator](https://bitbucket.org/fivecool/streamhub-stats-collector-validator).

Set up
======
```console
% gem i bundler
% bundle install --path vendor/bundle
```

Create files to input
=========================
This tool reads data from a file and makes requests.
* Fields should be separated by semi-colon, ";"

See ticks.txt and / or events.txt and edit it.

Run
===
```console
% bundle exec ruby validator-client.rb INPUT-FILE [OPTIONS]
```
