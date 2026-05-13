package de.jr.smtweaks.qrCodeScanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import de.jr.smtweaks.R;

public class QrCodeActivity extends AppCompatActivity {

    public static final String EXTRA_RESULT = "result";

    private String lastResult = null;

    private PreviewView previewView;

    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {

        Boolean granted = result.get(Manifest.permission.CAMERA);

        if (granted != null && granted) {
            startCamera();
        } else {

            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show();

            setResult(RESULT_CANCELED);
            finish();
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.qr_code_scanner);
        previewView = findViewById(R.id.previewView);
        findViewById(R.id.qrCodeButton).setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            permissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> providerFuture = ProcessCameraProvider.getInstance(this);
        providerFuture.addListener(() -> {

            try {
                ProcessCameraProvider cameraProvider = providerFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), this::scanQrCode);
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);
            } catch (ExecutionException | InterruptedException e) {

                Log.e("QrCodeActivity", "Failed to start camera", e);
            }

        }, ContextCompat.getMainExecutor(this));
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void scanQrCode(ImageProxy imageProxy) {

        if (imageProxy == null || imageProxy.getImage() == null) {
            if (imageProxy != null) {
                imageProxy.close();
            }
            return;
        }

        try (imageProxy) {
            ByteBuffer byteBuffer = imageProxy.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);

            int width = imageProxy.getWidth();
            int height = imageProxy.getHeight();

            int[] pixels = new int[width * height];

            for (int i = 0; i < pixels.length && i < bytes.length; i++) {
                int luminance = bytes[i] & 0xFF;
                pixels[i] = 0xFF000000 | (luminance << 16) | (luminance << 8) | luminance;
            }

            LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            String result = new MultiFormatReader().decode(bitmap).getText();

            if (result == null || LinkParser.otpAuthParser(result) == null) {
                if (result != null && !result.equals(lastResult))
                    Toast.makeText(this, "Wrong code", Toast.LENGTH_SHORT).show();
                lastResult = result;
                return;
            }

            Intent intent = new Intent();
            intent.putExtra(EXTRA_RESULT, result);
            setResult(RESULT_OK, intent);
            finish();


        } catch (NotFoundException ignored) {
        }
    }
}