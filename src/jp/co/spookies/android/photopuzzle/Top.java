package jp.co.spookies.android.photopuzzle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class Top extends Activity {
	private static final int MENU_SETTING = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.top);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_SETTING, Menu.NONE, R.string.menu_preference)
				.setIcon(android.R.drawable.ic_menu_preferences);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SETTING:
			startActivity(new Intent(this, Preference.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void startGame(View view) {
		startActivity(new Intent(this, TakePicture.class));
	}
}
