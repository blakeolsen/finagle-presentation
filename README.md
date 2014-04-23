Finagle Presentation
=======

A presentation of Twitter's [Finagle](http://twitter.github.io/finagle/) framework. This was written for Montreal's Scala User Group held in April 2014.

Some of the content was borrowed from [this presentation](http://monkey.org/~marius/talks/twittersystems/) from Marius Eriksen.

Using it
=======

```
npm install
bower install
grunt
```

Then open the presentation [here](http://localhost:8000)

Demo
====

The presentation includes a demo in 6 steps. Each step has Scala code associated with it that is runnable. See ```src/main/scala``` for the source and use ```sbt``` ```runMain``` to execute each step (e.g: ```sbt runMain http.step1.App -urls http://www.google.ca```)

There's also a Vagrant VM that hosts some optional but useful services to run the demo (namely Redis and Zipkin). Simply run ```vagrant up``` in the root folder of the project to start the VM. Zipkin's UI will be available [here](http://localhost:4567).
