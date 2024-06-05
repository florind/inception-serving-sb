# Zero to image recognition in 60 seconds with Tensorflow and Spring Boot

[![Build Status](https://github.com/florind/inception-serving-sb/actions/workflows/gradle.yml/badge.svg)](https://github.com/florind/inception-serving-sb/actions/workflows/gradle.yml)
[![Coverage Status](https://coveralls.io/repos/github/florind/inception-serving-sb/badge.svg?branch=master)](https://coveralls.io/github/florind/inception-serving-sb?branch=master)

TL;DR: for the impatient ones wanting to have a web service for image recognition without any Tensorflow prerequisites: run ```./gradlew fetchInceptionFrozenModel bootrun```, navigate to http://localhost:8080 and upload an image. The backend will categorize the image and output the result along with the probability.

Screenshot (non clickable)<br/>
<div align="center" style="text-align:center"><img src="cat_classified.jpg" width="560"/></div>

## Why
Tensorflow is hard enough to wrap one's head around. It has several parts that deal with preparing data, defining and training a model and finally, outputting a model that can then be used to categorize (infer) other data. There's math involved, new vocabulary to learn and on top, a toolchain which revolves around Python.
This project only addresses serving a Tensorflow pre-trained image categorization model, otherwise called the Inception model.

## Prerequisites
- JDK 17-21

## Run
```./gradlew fetchInceptionFrozenModel bootrun```

#### Or build & run executable jar
```
./gradlew bootjar
build/libs/inception-serving-sb.jar
```
#### Why it doesn't this work on my Apple Silicon machine?
Tensorflow jars are not distributed for the Apple Silicon hardware. More info [here](https://github.com/tensorflow/java#individual-dependencies)

Then navigate to http://localhost:8080 and upload an image. The backend will categorize the image and output the result along with the probability.

## How
Head to [the blog post](https://blog.newsplore.com/2017/07/31/zero-to-image-recognition-in-60-seconds-with-tensorflow-and-spring-boot) for the ful monty.

## Kotlin
Thanks to [@mcjojos](https://github.com/mcjojos), a port to Kotlin is now [available](https://github.com/florind/inception-serving-sb/tree/kotlin) in a separate branch. Woot!
