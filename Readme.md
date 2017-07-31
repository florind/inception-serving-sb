# 0 to image recognition with Tensorflow in 60 seconds

TL;DR: for the impatient ones wanting to have a web service for image recognition without any Tensorflow prerequisites: run ```./gradlew fetchInceptionFrozenModel bootrun```, navigate to http://localhost:8080 and upload an image. The backend will categorize the image and output the result along with the probability.

## Why
Tensorflow is hard enough to wrap one's head around. It has several parts that deal with preparing data, defining and training a model and finally, outputting a model that can then be used to categorize (infer) other data. There's math involved, new vocabulary to learn and on top, a toolchain which revolves around Python.
This project only addresses serving a Tensorflow pre-trained image categorization model, otherwise called the Inception model.  

## Prerequisites
- JDK 8
- A trained Tensorflow frozen model. We'll use the inception_v3 model offered by Google here https://storage.googleapis.com/download.tensorflow.org/models/inception_v3_2016_08_28_frozen.pb.tar.gz


## Run
```./gradlew fetchInceptionFrozenModel bootrun```

Navigate to http://localhost:8080 and upload an image. The backend will categorize the image and output the result along with the probability.
