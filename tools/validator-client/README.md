streamhub-stats-collector-predator
==================================

A validation tool for [streamhub-stats-collector-validator](https://bitbucket.org/fivecool/streamhub-stats-collector-validator).

Set up
======
```console
% gem i bundler
% bundle install --path vendor/bundle
```

Run
===
```console
% bundle exec ruby predator.rb {ticks.tsv|events.tsv}
```
