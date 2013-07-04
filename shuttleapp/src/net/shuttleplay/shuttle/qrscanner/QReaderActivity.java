package net.shuttleplay.shuttle.qrscanner;

import java.io.IOException;
import java.util.Collection;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.client.result.URIParsedResult;
import net.shuttleplay.shuttle.qrscanner.camera.CameraManager;
import net.shuttleplay.shuttle.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
//import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
//import android.widget.TextView;


public class QReaderActivity extends Activity implements Callback {
	private static final String TAG = "TrendBox";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.capture);

//		statusView = (TextView) findViewById(R.id.status_view);
		handler = null;
		lastResult = null;
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}

	protected void onResume() {
		super.onResume();

		// CameraManager must be initialized here, not in onCreate(). This is
		// necessary because we don't
		// want to open the camera driver and measure the screen size if we're
		// going to show the help on
		// first launch. That led to bugs where the scanning rectangle was the
		// wrong size and partially
		// off screen.
		cameraManager = new CameraManager(getApplication());

		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);

		resetStatusView();

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

//		Intent intent = getIntent();
//		String action = intent == null ? null : intent.getAction();
//		String dataString = intent == null ? null : intent.getDataString();
		// if (intent != null && action != null) {
		// if (action.equals(Intents.Scan.ACTION)) {
		// // Scan the formats the intent requested, and return the result
		// // to the calling activity.
		// source = Source.NATIVE_APP_INTENT;
		// decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
		// if (intent.hasExtra(Intents.Scan.WIDTH)
		// && intent.hasExtra(Intents.Scan.HEIGHT)) {
		// int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
		// int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
		// if (width > 0 && height > 0) {
		// cameraManager.setManualFramingRect(width, height);
		// }
		// }
		// } else if (dataString != null
		// if (dataString != null
		// && dataString.contains(PRODUCT_SEARCH_URL_PREFIX)
		// && dataString.contains(PRODUCT_SEARCH_URL_SUFFIX)) {
		// // Scan only products and send the result to mobile Product
		// // Search.
		// source = Source.PRODUCT_SEARCH_LINK;
		// sourceUrl = dataString;
		// decodeFormats = DecodeFormatManager.PRODUCT_FORMATS;
		// } else if (dataString != null && dataString.startsWith(ZXING_URL)) {
		// // Scan formats requested in query string (all formats if none
		// // specified).
		// // If a return URL is specified, send the results there.
		// // Otherwise, handle it ourselves.
		// source = Source.ZXING_LINK;
		// sourceUrl = dataString;
		// Uri inputUri = Uri.parse(sourceUrl);
		// returnUrlTemplate = inputUri
		// .getQueryParameter(RETURN_URL_PARAM);
		// decodeFormats = DecodeFormatManager
		// .parseDecodeFormats(inputUri);
		// } else {
		// // Scan all formats and handle the results ourselves (launched
		// // from Home).
		// source = Source.NONE;
		// decodeFormats = null;
		// }
		// characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);
		// } else {
		// source = Source.NONE;
		decodeFormats = null;
		characterSet = null;
		// }

//		SharedPreferences prefs = PreferenceManager
//				.getDefaultSharedPreferences(this);
//		copyToClipboard = prefs.getBoolean(Go.KEY_COPY_TO_CLIPBOARD, true)
//				&& (intent == null || intent.getBooleanExtra(
//						Intents.Scan.SAVE_HISTORY, true));

		// beepManager.updatePrefs();
		//
		inactivityTimer.onResume();
	}

	@Override
	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	public void handleDecode(Result rawResult, Bitmap barcode) {
		inactivityTimer.onActivity();
		lastResult = rawResult;

		ParsedResult result = ResultParser.parseResult(lastResult);
		if (result.getType().equals(ParsedResultType.URI)) {
			URIParsedResult uriResult = (URIParsedResult) result;
			launchIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(uriResult
					.getURI())));
			finish();
			return;
		}

		Message message = handler.obtainMessage(R.id.restart_preview);
		handler.sendMessage(message);
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (handler == null) {
				handler = new QReaderActivityHandler(this, decodeFormats,
						characterSet, cameraManager);
			}
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}
	}

	void launchIntent(Intent intent) {
		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			Log.d(TAG, "Launching intent: " + intent + " with extras: "
					+ intent.getExtras());
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.app_name);
				builder.setMessage(R.string.msg_intent_failed);
				builder.setPositiveButton(android.R.string.ok, null);
				builder.show();
			}
		}
	}

	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(android.R.string.ok, new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}

	private void resetStatusView() {
//		statusView.setText(R.string.msg_default_status);
//		statusView.setVisibility(View.VISIBLE);
		viewfinderView.setVisibility(View.VISIBLE);
		lastResult = null;
	}

	private CameraManager cameraManager;
	private QReaderActivityHandler handler;
	private ViewfinderView viewfinderView;
//	private TextView statusView;
	private Result lastResult;
	private boolean hasSurface;
//	private boolean copyToClipboard;
//	private String sourceUrl;
//	private String returnUrlTemplate;
	private Collection<BarcodeFormat> decodeFormats;
	private String characterSet;
//	private String versionName;
	private InactivityTimer inactivityTimer;

	// private BeepManager beepManager;

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG,
					"*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}
}
