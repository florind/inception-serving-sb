package com.newsplore.inception.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.tensorflow.*

import java.util.Arrays

//Inspired from https://github.com/tensorflow/tensorflow/blob/master/tensorflow/java/src/main/java/org/tensorflow/examples/LabelImage.java
@Service
class ClassifyImageService(private val inceptionGraph: Graph,
                           private val labels: List<String>,
                           @param:Value("\${tf.outputLayer}") private val outputLayer: String,
                           @param:Value("\${tf.image.width}") private val W: Int,
                           @param:Value("\${tf.image.height}") private val H: Int,
                           @param:Value("\${tf.image.mean}") private val mean: Float,
                           @param:Value("\${tf.image.scale}") private val scale: Float) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun classifyImage(imageBytes: ByteArray): LabelWithProbability {
        val start = System.currentTimeMillis()
        normalizedImageToTensor(imageBytes).use { image ->
            val labelProbabilities = classifyImageProbabilities(image)
            val bestLabelIdx = maxIndex(labelProbabilities)
            val labelWithProbability = LabelWithProbability(labels[bestLabelIdx], labelProbabilities[bestLabelIdx] * 100f, System.currentTimeMillis() - start)
            log.debug(String.format("Image classification [%s %.2f%%] took %d ms", labelWithProbability.label, labelWithProbability.probability, labelWithProbability.elapsed))
            return labelWithProbability
        }
    }

    private fun classifyImageProbabilities(image: Tensor): FloatArray {
        Session(inceptionGraph).use { s ->
            s.runner().feed("input", image).fetch(outputLayer).run()[0].use { result ->
                val rshape = result.shape()
                if (result.numDimensions() != 2 || rshape[0] != 1L) {
                    throw RuntimeException(
                            String.format(
                                    "Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s",
                                    Arrays.toString(rshape)))
                }
                val nlabels = rshape[1].toInt()
                return result.copyTo(Array(1) { FloatArray(nlabels) })[0]
            }
        }
    }


    private fun maxIndex(probabilities: FloatArray): Int {
        var best = 0
        (1 until probabilities.size)
                .asSequence()
                .filter { probabilities[it] > probabilities[best] }
                .forEach { best = it }
        return best
    }

    private fun normalizedImageToTensor(imageBytes: ByteArray): Tensor {
        Graph().use { g ->
            val b = GraphBuilder(g)
            //Tutorial python here: https://github.com/tensorflow/tensorflow/tree/master/tensorflow/examples/label_image
            // Some constants specific to the pre-trained model at:
            // https://storage.googleapis.com/download.tensorflow.org/models/inception_v3_2016_08_28_frozen.pb.tar.gz
            //
            // - The model was trained with images scaled to 299x299 pixels.
            // - The colors, represented as R, G, B in 1-byte each were converted to
            //   float using (value - Mean)/Scale.

            // Since the graph is being constructed once per execution here, we can use a constant for the
            // input image. If the graph were to be re-used for multiple input images, a placeholder would
            // have been more appropriate.
            val input = b.constant("input", imageBytes)
            val output = b.div(
                    b.sub(
                            b.resizeBilinear(
                                    b.expandDims(
                                            b.cast(b.decodeJpeg(input, 3), DataType.FLOAT),
                                            b.constant("make_batch", 0)),
                                    b.constant("size", intArrayOf(H, W))),
                            b.constant("mean", mean)),
                    b.constant("scale", scale))
            Session(g).use { s -> return s.runner().fetch(output.op().name()).run()[0] }
        }
    }

    private class GraphBuilder(private val g: Graph) {

        fun div(x: Output, y: Output): Output {
            return binaryOp("Div", x, y)
        }

        fun sub(x: Output, y: Output): Output {
            return binaryOp("Sub", x, y)
        }

        fun resizeBilinear(images: Output, size: Output): Output {
            return binaryOp("ResizeBilinear", images, size)
        }

        fun expandDims(input: Output, dim: Output): Output {
            return binaryOp("ExpandDims", input, dim)
        }

        fun cast(value: Output, dtype: DataType): Output {
            return g.opBuilder("Cast", "Cast").addInput(value).setAttr("DstT", dtype).build().output(0)
        }

        fun decodeJpeg(contents: Output, channels: Long): Output {
            return g.opBuilder("DecodeJpeg", "DecodeJpeg")
                    .addInput(contents)
                    .setAttr("channels", channels)
                    .build()
                    .output(0)
        }

        fun constant(name: String, value: Any): Output {
            Tensor.create(value).use { t ->
                return g.opBuilder("Const", name)
                        .setAttr("dtype", t.dataType())
                        .setAttr("value", t)
                        .build()
                        .output(0)
            }
        }

        private fun binaryOp(type: String, in1: Output, in2: Output): Output {
            return g.opBuilder(type, type).addInput(in1).addInput(in2).build().output(0)
        }
    }

}
