package com.tjoris.timekeeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainActivity extends Activity
{
	private static final String[][] kPLAYLIST = new String[][] {
		{ "Weak", "95" },
		{ "It's So Hard", "128" },
		{ "So Lonely", "160" },
		{ "Message In a Bottle", "152" },
		{ "You Oughta Know", "105" },
		{ "Hard To Handle", "104" },
		{ "Personal Jesus", "117" },
		{ "Run to you", "126" },
		{ "Don't Stop Me Now (1/2)", "101" },
		{ "Don't Stop Me Now (2/2)", "158" },
		{ "Welcome To The Jungle (1/2)", "100" },
		{ "Welcome To The Jungle (2/2)", "120" },
		{ "Jerusalem", "132" },
		{ "Girl", "136" },
	};
	private static final String kKEY_NAME = "name";
	private static final String kKEY_TEMPO = "tempo";

	private final SoundGenerator fSoundGenerator = new SoundGenerator();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		getPlaylist().setOnItemClickListener(new OnItemClickListener()
		{
			@Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
            {
				trigger(position);
            }
		});
		fillList();
		register(R.id.button_start, new View.OnClickListener()
		{
			@Override
            public void onClick(final View v)
            {
				doStart();
            }
		});
		register(R.id.button_stop, new View.OnClickListener()
		{
			@Override
            public void onClick(final View v)
            {
				doStop();
            }
		});
		register(R.id.button_next, new View.OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				doNext();
			}
		});
		trigger(0);
	}
	
	@Override
	protected void onDestroy()
	{
		fSoundGenerator.close();
	    super.onDestroy();
	}

	private void fillList()
	{
		final List<Map<String, String>> data = new ArrayList<Map<String,String>>();
		for (final String[] entry : kPLAYLIST)
		{
			final Map<String, String> map = new HashMap<String, String>();
			map.put(kKEY_NAME, entry[0]);
			map.put(kKEY_TEMPO, entry[1]);
			data.add(map);
		}
		final ListView playlist = getPlaylist();
		playlist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		playlist.setAdapter(new SimpleAdapter(this, data, R.layout.list_entry, new String[] {"name", "tempo"}, new int[] {R.id.entry_name, R.id.entry_tempo}));
	}

	private void register(final int id, final View.OnClickListener listener)
	{
		final Button button = (Button) findViewById(id);
		if (button != null)
		{
			button.setOnClickListener(listener);
		}
	}

	private void doStart()
	{
		fSoundGenerator.start();
	}

	private void doStop()
	{
		fSoundGenerator.stop();
	}
	
	private void doNext()
	{
		final ListView playlist = getPlaylist();
		final int pos = playlist.getCheckedItemPosition();
		if (pos < kPLAYLIST.length - 1)
		{
			trigger(pos + 1);
			//playlist.scrollTo(0, playlist.getPositionForView(view));
		}
	}
	
	private void trigger(final int selection)
	{
		final String tempo = kPLAYLIST[selection][1];
		final int bpm = Integer.parseInt(tempo);
		fSoundGenerator.start(bpm);
		getPlaylist().setItemChecked(selection, true);
	}
	
	private ListView getPlaylist()
	{
		return (ListView) findViewById(R.id.playlist);
	}
}
