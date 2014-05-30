package jp.co.spookies.android.photopuzzle;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TableLayout;
import android.widget.TableRow;

public class PhotoPuzzle extends Activity {
	private int emptyId;
	private int colSize;
	private int rowSize;
	private Bitmap original;
	private static final int SHUFFLE_COUNT = 100;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.puzzle);
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);

		// パズルサイズの設定
		int size = Integer.parseInt(pref.getString(
				getString(R.string.pref_size_key),
				getString(R.string.pref_size_default)));
		colSize = size;
		rowSize = size;

		// 縁の画像の設定
		setTheme(pref.getString(getString(R.string.pref_theme_key),
				getString(R.string.pref_theme_default)));

		try {
			original = readPicture(getIntent());
		} catch (Exception e) {
			finish();
		}
		initView(original);

		// 最初からクリアという状態ではないようにシャッフルする
		while (isCleared()) {
			shuffle(SHUFFLE_COUNT);
		}
	}

	private Bitmap readPicture(Intent intent) throws Exception {
		String key = getString(R.string.intent_param_name);
		if (!intent.hasExtra(key)) {
			throw new Exception();
		}
		File file = new File(new URI(intent.getStringExtra(key))); // インテントからURIを取り出し、ファイルオブジェクトを作成
		InputStream input = new FileInputStream(file);
		byte[] image = new byte[input.available()];
		input.read(image);
		return decodeBitmap(image);
	}

	private Bitmap decodeBitmap(byte[] image) {
		WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		int windowWidth = display.getWidth();
		int windowHeight = display.getHeight();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // サイズの取得のみ行うようにする
		BitmapFactory.decodeByteArray(image, 0, image.length, options);
		// 画面サイズに合わせて縮小
		if (options.outWidth > options.outHeight) {
			options.inSampleSize = (int) Math.ceil((float) options.outWidth
					/ windowWidth) - 1;
		} else {
			options.inSampleSize = (int) Math.ceil((float) options.outHeight
					/ windowHeight) - 1;
		}
		options.inJustDecodeBounds = false; // 実際にデコードさせる
		return BitmapFactory.decodeByteArray(image, 0, image.length, options);
	}

	private boolean isSameRow(int n1, int n2) {
		return n1 / colSize == n2 / colSize;
	}

	private boolean isSameCol(int n1, int n2) {
		return n1 % colSize == n2 % colSize;
	}

	private void movePanel(int n) {
		if (isSameRow(n, emptyId)) {
			if (n > emptyId) {
				// 下方向
				for (int i = emptyId; i < n; i += 1) {
					copyPanel(i, i + 1);
				}
			} else if (n < emptyId) {
				// 上方向
				for (int i = emptyId; i > n; i -= 1) {
					copyPanel(i, i - 1);
				}
			}
			setEmpty(n);
		} else if (isSameCol(n, emptyId)) {
			if (n > emptyId) {
				// 右方向
				for (int i = emptyId; i < n; i += colSize) {
					copyPanel(i, i + colSize);
				}
			} else if (n < emptyId) {
				// 左方向
				for (int i = emptyId; i > n; i -= colSize) {
					copyPanel(i, i - colSize);
				}
			}
			setEmpty(n);
		}
	}

	private boolean isCleared() {
		int size = rowSize * colSize;
		for (int i = 0; i < size; i++) {
			ImagePanel panel = (ImagePanel) findViewById(i);
			if (i != emptyId && i != panel.getImageId()) {
				return false;
			}
		}
		return true;
	}

	private void finishGame() {
		TableLayout table = (TableLayout) findViewById(R.id.table);
		table.removeAllViews();
		ImageView originalImageView = (ImageView) findViewById(R.id.originalImage);
		originalImageView.setImageBitmap(original);
		originalImageView.setVisibility(View.VISIBLE);
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(getText(R.string.dialog_title));
		dialog.setMessage(getText(R.string.dialog_message));
		dialog.setPositiveButton("OK", null);
		dialog.show();
	}

	private void copyPanel(int destId, int srcId) {
		ImagePanel dest = (ImagePanel) findViewById(destId);
		ImagePanel src = (ImagePanel) findViewById(srcId);
		dest.setImageDrawable(src.getDrawable());
		dest.setImageId(src.getImageId());
	}

	private void setEmpty(int id) {
		ImagePanel panel = (ImagePanel) findViewById(id);
		panel.setImageDrawable(null);
		panel.setImageId(null);
		emptyId = id;
	}

	private void shuffle(int count) {
		for (int i = 0; i < count; i++) {
			// 空白マスを上下左右どれかのマスと交換
			double p = Math.random();
			int n = emptyId;
			if (p < 0.25) {
				n += 1;
			} else if (p < 0.5) {
				n -= 1;
			} else if (p < 0.75) {
				n += colSize;
			} else {
				n -= colSize;
			}
			if (0 <= n && n < colSize * rowSize) {
				movePanel(n);
			}
		}
	}

	private void initView(Bitmap bitmap) {
		int width = bitmap.getWidth() / colSize;
		int height = bitmap.getHeight() / rowSize;
		TableLayout table = (TableLayout) findViewById(R.id.table);
		TableLayout.LayoutParams rowParam = new TableLayout.LayoutParams(
				LayoutParams.FILL_PARENT, 0, 1);
		TableRow.LayoutParams cellParam = new TableRow.LayoutParams(0,
				LayoutParams.FILL_PARENT, 1);
		FrameLayout.LayoutParams frameParam = new FrameLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		// 元画像をrowSize * colSize個に分割してTableLayout上に設置
		for (int i = 0; i < rowSize; i++) {
			TableRow row = new TableRow(this);
			for (int j = 0; j < colSize; j++) {
				int n = i * rowSize + j;
				int x = width * j;
				int y = height * i;
				ImagePanel panel = new ImagePanel(this, n, Bitmap.createBitmap(
						bitmap, x, y, width, height));
				ImageView margin = new ImageView(this);
				margin.setImageResource(R.drawable.margin);
				margin.setScaleType(ScaleType.FIT_XY);
				FrameLayout frame = new FrameLayout(this);
				frame.addView(panel, frameParam);
				frame.addView(margin, frameParam);
				row.addView(frame, cellParam);
			}
			table.addView(row, rowParam);
		}
		setEmpty((int) (Math.random() * (rowSize * colSize - 1)));
	}

	private void setTheme(String theme) {
		FrameLayout layout = (FrameLayout) findViewById(R.id.frame);
		if (theme.equals(getString(R.string.pref_theme_1))) {
			layout.setBackgroundResource(R.drawable.frame_01);
		} else if (theme.equals(getString(R.string.pref_theme_2))) {
			layout.setBackgroundResource(R.drawable.frame_02);
		} else if (theme.equals(getString(R.string.pref_theme_3))) {
			layout.setBackgroundResource(R.drawable.frame_03);
		}
	}

	class ImagePanel extends ImageView {
		private Integer imageId = null;

		public ImagePanel(Context context, int id, Bitmap bitmap) {
			super(context);
			setImageId(id);
			setId(id);
			setImageBitmap(bitmap);
			setScaleType(ScaleType.FIT_XY);
			setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					movePanel(getId());
					if (isCleared()) {
						finishGame();
					}
				}
			});
		}

		public void setImageId(Integer imageId) {
			this.imageId = imageId;
		}

		public Integer getImageId() {
			return imageId;
		}
	}
}