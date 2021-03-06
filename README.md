# Saul Project 
[![Build Status](https://semaphoreci.com/api/v1/projects/3a8f460c-fd44-42e2-bedb-70611d83a15c/658568/badge.svg)](https://semaphoreci.com/christos-c/saul)

[![Travis Build Status](https://magnum.travis-ci.com/IllinoisCogComp/saul.svg?token=sh2TUxymJtwGcwzpH5oQ&branch=master)](https://magnum.travis-ci.com/IllinoisCogComp/saul)
 
The project contains three modules. See the readme files for each module:

- [Saul core](saul-core/README.md)
- [Saul examples](saul-examples/README.md)
- [Saul webapp] (saul-webapp/README.md)

The project's [official chat group is at Slack](https://cogcomp.slack.com/messages/saul/)

## Usage 

First, run `sbt`. 

- `projects` will show the names of the existing module names. 
    - `project saulCore` will take you inside the core package. 
    -  `project saulExamples` will take you inside the examples package.
    - `project saulWebapp` will take you inside the webapp package. Then type `run` to start the server. Type `localhost:9000` in browser address bar to see the webapp.
- Inside each project you can `compile` it, or `run` it. 
- To fix the formatting problems, run `format`
