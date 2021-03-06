package com.tjoris.timekeeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.tjoris.timekeeper.data.IPlaylistStore;
import com.tjoris.timekeeper.data.Playlist;
import com.tjoris.timekeeper.data.PlaylistStoreFactory;
import com.tjoris.timekeeper.data.Song;

public class PlaylistActivity extends Activity
{
	private static final String kKEY_NAME = "name";
	private static final String kKEY_TEMPO = "tempo";

	private SoundGenerator fSoundGenerator;
	private final IPlaylistStore fStore;
	private Playlist fPlaylist = null;

	public PlaylistActivity()
	{
		fStore = PlaylistStoreFactory.createStore(this);
	}

	@SuppressLint("ClickableViewAccessibility")
    @Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		getPlaylist().setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
			{
				trigger(position);
			}
		});
		((Button) findViewById(R.id.button_start)).setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(final View v, final MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					doStart();
				}
				return false;
			}
		});
		((Button) findViewById(R.id.button_stop)).setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(final View v, final MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					doStop();
				}
				return false;
			}
		});
		((Button) findViewById(R.id.button_next)).setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(final View v, final MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					doNext();
				}
				return false;
			}
		});
		
		final int frequency = SettingsActivity.getIntPreference(this, SettingsActivity.kFREQUENCY, 880);
		final int duration = SettingsActivity.getIntPreference(this, SettingsActivity.kDURATION, 20);
		fSoundGenerator = new SoundGenerator(this, frequency, duration);
		
		loadIntent();
	}
	
	private void loadIntent()
	{
		final Bundle bundle = getIntent().getExtras();
		if (bundle != null)
		{
			final long playlist = bundle.getLong("playlist");
			if (fPlaylist == null || fPlaylist.getID() != playlist)
			{
				loadPlaylist(fStore.readPlaylist(playlist));
			}
		}
		else
		{
			loadPlaylist(null);
		}
	}
	
	@Override
	protected void onResume()
	{
	    if (fPlaylist != null)
	    {
			loadPlaylist(fStore.readPlaylist(fPlaylist.getID()));
	    }
	    super.onResume();
	}

	@Override
	protected void onDestroy()
	{
		fSoundGenerator.close();
		fStore.close();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.playlist_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.playlist_action_edit:
		{
			doStop();
			final Intent intent = new Intent(this, PlaylistEditActivity.class);
			intent.putExtra("playlist", fPlaylist.getID());
			startActivity(intent);
			return true;
		}
		default:
		{
			return super.onOptionsItemSelected(item);
		}
		}
	}

	private void loadPlaylist(final Playlist playlist)
	{
		fPlaylist = playlist;
		final List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		if (fPlaylist != null)
		{
			getActionBar().setTitle(fPlaylist.getName());
			for (final Song song : fPlaylist.getSongs())
			{
				final Map<String, String> map = new HashMap<String, String>();
				map.put(kKEY_NAME, song.getName());
				map.put(kKEY_TEMPO, Integer.toString(song.getTempo()));
				data.add(map);
			}
		}
		else
		{
			getActionBar().setTitle("<no playlist>");
		}
		final ListView playlistView = getPlaylist();
		playlistView.setAdapter(new SimpleAdapter(this, data, R.layout.playlist_entry, new String[] { kKEY_NAME, kKEY_TEMPO }, new int[] { R.id.playlist_entry_name, R.id.playlist_entry_tempo }));
		
		if (fPlaylist != null && !fPlaylist.getSongs().isEmpty())
		{
			final int tempo = fPlaylist.getSongs().get(0).getTempo();
			fSoundGenerator.configure(tempo);
			playlistView.setItemChecked(0, true);
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
		if (pos < fPlaylist.getSongs().size() - 1)
		{
			final int newPos = pos + 1;
			trigger(newPos);
			final int first = playlist.getFirstVisiblePosition();
			final int last = playlist.getLastVisiblePosition();
			final int middle = (last - first) / 2 + first;
			if (newPos <= first)
			{
				playlist.smoothScrollToPosition(newPos);
			}
			else if (newPos > middle)
			{
				playlist.smoothScrollToPosition(last + newPos - middle);
			}
		}
	}

	private void trigger(final int selection)
	{
		final int tempo = fPlaylist.getSongs().get(selection).getTempo();
		fSoundGenerator.start(tempo);
		getPlaylist().setItemChecked(selection, true);
	}

	private ListView getPlaylist()
	{
		return (ListView) findViewById(R.id.playlist);
	}
}
