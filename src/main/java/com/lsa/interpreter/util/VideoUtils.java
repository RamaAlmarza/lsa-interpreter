package com.lsa.interpreter.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class VideoUtils {
    private static final Logger logger = LoggerFactory.getLogger(VideoUtils.class);

    private VideoUtils() {
        // Utility class, prevent instantiation
    }

    public static Image matToImage(Mat frame) {
        try {
            // Convert the frame to BGR format
            Mat bgr = new Mat();
            if (frame.channels() == 1) {
                Imgproc.cvtColor(frame, bgr, Imgproc.COLOR_GRAY2BGR);
            } else {
                frame.copyTo(bgr);
            }

            // Create JavaFX image
            WritableImage writableImage = new WritableImage(bgr.cols(), bgr.rows());
            PixelWriter pixelWriter = writableImage.getPixelWriter();

            // Create buffer for pixel data
            byte[] buffer = new byte[bgr.channels() * bgr.cols() * bgr.rows()];
            bgr.get(0, 0, buffer);

            // Write pixels
            pixelWriter.setPixels(0, 0, bgr.cols(), bgr.rows(),
                javafx.scene.image.PixelFormat.getByteRgbInstance(),
                ByteBuffer.wrap(buffer),
                bgr.cols() * bgr.channels());

            // Clean up
            bgr.release();

            return writableImage;
        } catch (Exception e) {
            logger.error("Error converting Mat to Image", e);
            return null;
        }
    }

    public static Mat resizeFrame(Mat frame, int width, int height) {
        try {
            Mat resized = new Mat();
            Imgproc.resize(frame, resized, new org.opencv.core.Size(width, height));
            return resized;
        } catch (Exception e) {
            logger.error("Error resizing frame", e);
            return frame;
        }
    }

    public static Mat rotateFrame(Mat frame, double angle) {
        try {
            org.opencv.core.Point center = new org.opencv.core.Point(frame.cols() / 2.0, frame.rows() / 2.0);
            Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0);
            Mat rotated = new Mat();
            Imgproc.warpAffine(frame, rotated, rotationMatrix, frame.size());
            return rotated;
        } catch (Exception e) {
            logger.error("Error rotating frame", e);
            return frame;
        }
    }

    public static Mat adjustBrightness(Mat frame, double alpha, double beta) {
        try {
            Mat adjusted = new Mat();
            frame.convertTo(adjusted, -1, alpha, beta);
            return adjusted;
        } catch (Exception e) {
            logger.error("Error adjusting frame brightness", e);
            return frame;
        }
    }

    public static Mat equalizeHistogram(Mat frame) {
        try {
            Mat equalized = new Mat();
            if (frame.channels() == 1) {
                Imgproc.equalizeHist(frame, equalized);
            } else {
                // Convert to YUV
                Mat yuv = new Mat();
                Imgproc.cvtColor(frame, yuv, Imgproc.COLOR_BGR2YUV);

                // Split channels
                java.util.List<Mat> channels = new java.util.ArrayList<>();
                org.opencv.core.Core.split(yuv, channels);

                // Equalize Y channel
                Imgproc.equalizeHist(channels.get(0), channels.get(0));

                // Merge channels
                org.opencv.core.Core.merge(channels, yuv);

                // Convert back to BGR
                Imgproc.cvtColor(yuv, equalized, Imgproc.COLOR_YUV2BGR);

                // Clean up
                yuv.release();
                channels.forEach(Mat::release);
            }
            return equalized;
        } catch (Exception e) {
            logger.error("Error equalizing frame histogram", e);
            return frame;
        }
    }
}
