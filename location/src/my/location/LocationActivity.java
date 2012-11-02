package my.location;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

public class LocationActivity extends Activity implements OnClickListener {
	/** Called when the activity is first created. */

	private CheckBox chkServiceStarted;
	private static final String TAG = "LOCATION DEMO";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		chkServiceStarted = (CheckBox) findViewById(R.id.startservice);
		chkServiceStarted
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							Log.i(TAG, "onClick: starting service");
							startService(new Intent(LocationActivity.this,
									LocationService.class));

						} else {
							Log.i(TAG, "onClick: stopping service");
							stopService(new Intent(LocationActivity.this,
									LocationService.class));
						}
					}
				});
	}

	public void onClick(View v) {
		switch (v.getId()) {
			default:
				Log.i(TAG, "No such button");
				break;				
		}		
	}
}