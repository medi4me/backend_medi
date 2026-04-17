package com.mediforme.mediforme.medicine.external.adapter;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import com.mediforme.mediforme.medicine.external.port.OcrPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * GCP Vision 기반 OCR 어댑터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleVisionOcrAdapter implements OcrPort {

    private final ImageAnnotatorClient imageAnnotatorClient;

    @Override
    public String extractText(byte[] image) {
        if (image == null || image.length == 0) return null;

        try {
            ByteString imgBytes = ByteString.copyFrom(image);
            Image img = Image.newBuilder().setContent(imgBytes).build();

            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build())
                .setImage(img)
                .build();

            BatchAnnotateImagesResponse batch =
                imageAnnotatorClient.batchAnnotateImages(List.of(request));
            AnnotateImageResponse response = batch.getResponsesList().get(0);

            if (response.hasError()) {
                log.warn("GCP Vision error: {}", response.getError().getMessage());
                return null;
            }
            if (response.getTextAnnotationsList().isEmpty()) return null;

            return response.getTextAnnotationsList().get(0).getDescription();

        } catch (Exception e) {
            log.warn("Vision OCR failed", e);
            return null;
        }
    }
}
