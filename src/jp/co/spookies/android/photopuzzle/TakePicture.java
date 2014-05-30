package jp.co.spookies.android.photopuzzle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import jp.co.spookies.android.utils.SimpleCameraCallback;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

public class TakePicture extends Activity {
	SimpleCameraCallback camera = null;
	PictureCallback jpegCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			try {
				File image = File.createTempFile("photopuzzle", ".jpg",
						getFilesDir());
				OutputStream output = new FileOutputStream(image);
				output.write(data);
				Intent intent = new Intent(TakePicture.this, PhotoPuzzle.class);
				intent.putExtra(getString(R.string.intent_param_name), image
						.toURI().toString());
				startActivity(intent);
			} catch (IOException e) {
				finish();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		SurfaceView view = new SurfaceView(this);
		setContentView(view);
		view.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		camera = new SimpleCameraCallback(this);
		view.getHolder().addCallback(camera);
		view.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					camera.takePicture(null, null, jpegCallback);
				}
				return false;
			}
		});
	}
}
