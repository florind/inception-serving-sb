package com.newsplore.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tensorflow.*;

import java.util.Arrays;
import java.util.List;

//Inspired from https://github.com/tensorflow/tensorflow/blob/master/tensorflow/java/src/main/java/org/tensorflow/examples/LabelImage.java
@Service
@Slf4j
public class ClassifyImageService {

    private final Graph inceptionGraph;
    private final List<String> labels;
    private final String outputLayer;

    private final int H = 299;
    private int W = 299;
    private float mean = 0;
    private float scale = 255f;

    public ClassifyImageService(Graph inceptionGraph, List<String> labels, @Value("${tf.outputLayer}") String outputLayer) {
        this.inceptionGraph = inceptionGraph;
        this.labels = labels;
        this.outputLayer = outputLayer;
    }

    public LabelWithProbability classifyImage(byte[] imageBytes) {
        long start = System.currentTimeMillis();
        try (Tensor image = normalizedImageToTensor(imageBytes)) {
            float[] labelProbabilities = classifyImageProbabilities(image);
            int bestLabelIdx = maxIndex(labelProbabilities);
            LabelWithProbability labelWithProbability =
                new LabelWithProbability(labels.get(bestLabelIdx), labelProbabilities[bestLabelIdx] * 100f, System.currentTimeMillis() -  start);
            log.info(String.format("Image classification [%s %.2f%%] took %d ms", labelWithProbability.getLabel(), labelWithProbability.getProbability(), labelWithProbability.getElapsed()));
            return labelWithProbability;
        }
    }

    private float[] classifyImageProbabilities (Tensor image) {
        try (Session s = new Session(inceptionGraph);
             Tensor result = s.runner().feed("input", image).fetch(outputLayer).run().get(0)) {
            final long[] rshape = result.shape();
            if (result.numDimensions() != 2 || rshape[0] != 1) {
                throw new RuntimeException(
                    String.format(
                        "Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s",
                        Arrays.toString(rshape)));
            }
            int nlabels = (int) rshape[1];
            return result.copyTo(new float[1][nlabels])[0];
        }
    }


    private int maxIndex(float[] probabilities) {
        int best = 0;
        for (int i = 1; i < probabilities.length; ++i) {
            if (probabilities[i] > probabilities[best]) {
                best = i;
            }
        }
        return best;
    }

    private Tensor normalizedImageToTensor(byte[] imageBytes) {
        try (Graph g = new Graph()) {
            GraphBuilder b = new GraphBuilder(g);
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
            final Output input = b.constant("input", imageBytes);
            final Output output =
                b.div(
                    b.sub(
                        b.resizeBilinear(
                            b.expandDims(
                                b.cast(b.decodeJpeg(input, 3), DataType.FLOAT),
                                b.constant("make_batch", 0)),
                            b.constant("size", new int[] {H, W})),
                        b.constant("mean", mean)),
                    b.constant("scale", scale));
            try (Session s = new Session(g)) {
                return s.runner().fetch(output.op().name()).run().get(0);
            }
        }
    }

    static class GraphBuilder {
        GraphBuilder(Graph g) {
            this.g = g;
        }

        Output div(Output x, Output y) {
            return binaryOp("Div", x, y);
        }

        Output sub(Output x, Output y) {
            return binaryOp("Sub", x, y);
        }

        Output resizeBilinear(Output images, Output size) {
            return binaryOp("ResizeBilinear", images, size);
        }

        Output expandDims(Output input, Output dim) {
            return binaryOp("ExpandDims", input, dim);
        }

        Output cast(Output value, DataType dtype) {
            return g.opBuilder("Cast", "Cast").addInput(value).setAttr("DstT", dtype).build().output(0);
        }

        Output decodeJpeg(Output contents, long channels) {
            return g.opBuilder("DecodeJpeg", "DecodeJpeg")
                .addInput(contents)
                .setAttr("channels", channels)
                .build()
                .output(0);
        }

        Output constant(String name, Object value) {
            try (Tensor t = Tensor.create(value)) {
                return g.opBuilder("Const", name)
                    .setAttr("dtype", t.dataType())
                    .setAttr("value", t)
                    .build()
                    .output(0);
            }
        }

        private Output binaryOp(String type, Output in1, Output in2) {
            return g.opBuilder(type, type).addInput(in1).addInput(in2).build().output(0);
        }

        private Graph g;
    }

    @Data
    @NoArgsConstructor @AllArgsConstructor
    public static class LabelWithProbability {
        private String label;
        private float probability;
        private long elapsed;
    }
}
