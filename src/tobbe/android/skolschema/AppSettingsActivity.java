package tobbe.android.skolschema;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;

public class AppSettingsActivity extends Activity {
	
	private CheckBox checkForUpdates;
	private CheckBox updateScheduleOnMobileNetwork;
	private CheckBox fullscreenMode;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_settings);
        
        // Find instances
        checkForUpdates = (CheckBox) findViewById(R.id.checkForUpdates);
        updateScheduleOnMobileNetwork = (CheckBox) findViewById(R.id.updateScheduleOnMobileNetwork);
        fullscreenMode = (CheckBox) findViewById(R.id.fullscreenMode);
        
        // Load settings
        checkForUpdates.setChecked(Settings.getBoolean(Settings.CHECK_FOR_UPDATES));
        updateScheduleOnMobileNetwork.setChecked(Settings.getBoolean(Settings.UPDATE_SCHEDULES_ON_MOBILE_NETWORK));
        fullscreenMode.setChecked(Settings.getBoolean(Settings.FULLSCREEN_MODE));
	}
	
	public void finish() {
		// Save settings
		Settings.setBoolean(Settings.CHECK_FOR_UPDATES, checkForUpdates.isChecked());
		Settings.setBoolean(Settings.UPDATE_SCHEDULES_ON_MOBILE_NETWORK, updateScheduleOnMobileNetwork.isChecked());
		Settings.setBoolean(Settings.FULLSCREEN_MODE, fullscreenMode.isChecked());
		Settings.saveSettings();
		
		super.finish();
	}

}
