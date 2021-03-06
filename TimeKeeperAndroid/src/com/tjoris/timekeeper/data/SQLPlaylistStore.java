package com.tjoris.timekeeper.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLPlaylistStore extends SQLiteOpenHelper implements IPlaylistStore
{
	private static final String kDATABASE_NAME = "timekeeper";
	private static final int kDATABASE_VERSION = 1;

	private static final String kPLAYLIST_TABLE_NAME = "playlist";
	private static final String kPLAYLIST_ID = "id";
	private static final String kPLAYLIST_NAME = "name";
	private static final String kPLAYLIST_WEIGHT = "weight";
	private static final String kPLAYLIST_TABLE_CREATE = "CREATE TABLE " + kPLAYLIST_TABLE_NAME + " (" + kPLAYLIST_ID + " INTEGER PRIMARY KEY, " + kPLAYLIST_NAME + " TEXT, " + kPLAYLIST_WEIGHT + " INTEGER);";

	private static final String kSONG_TABLE_NAME = "song";
	private static final String kSONG_ID = "id";
	private static final String kSONG_PLAYLIST = "playlist";
	private static final String kSONG_NAME = "name";
	private static final String kSONG_TEMPO = "tempo";
	private static final String kSONG_WEIGHT = "weight";
	private static final String kSONG_TABLE_CREATE = "CREATE TABLE " + kSONG_TABLE_NAME + " (" + kSONG_ID + " INTEGER PRIMARY KEY, " + kSONG_PLAYLIST + " INTEGER, " + kSONG_NAME + " TEXT, " + kSONG_TEMPO + " INTEGER, " + kSONG_WEIGHT + " INTEGER, " + "FOREIGN KEY (" + kSONG_PLAYLIST + ") REFERENCES " + kPLAYLIST_TABLE_NAME + " (" + kPLAYLIST_ID + "));";

	SQLPlaylistStore(final Context context)
	{
		super(context, kDATABASE_NAME, null, kDATABASE_VERSION);
	}

	@Override
	public void onCreate(final SQLiteDatabase db)
	{
		db.execSQL(kPLAYLIST_TABLE_CREATE);
		db.execSQL(kSONG_TABLE_CREATE);

		final Playlist playlist = new Playlist("Solid Rock", 0);
		playlist.addSong(new Song("Weak", 95));
		playlist.addSong(new Song("It's So Hard", 128));
		playlist.addSong(new Song("So Lonely", 160));
		playlist.addSong(new Song("Message In a Bottle", 152));
		playlist.addSong(new Song("You Oughta Know", 105));
		playlist.addSong(new Song("Hard To Handle", 104));
		playlist.addSong(new Song("Personal Jesus", 119));
		playlist.addSong(new Song("Run to you", 126));
		playlist.addSong(new Song("Don't Stop Me Now (1/2)", 101));
		playlist.addSong(new Song("Don't Stop Me Now (2/2)", 158));
		playlist.addSong(new Song("Welcome To The Jungle (1/2)", 100));
		playlist.addSong(new Song("Welcome To The Jungle (2/2)", 120));
		playlist.addSong(new Song("Jerusalem", 132));
		playlist.addSong(new Song("Girl", 136));
		playlist.addSong(new Song("Are You Gonna Go My Way", 130));
		playlist.addSong(new Song("Always On The Run", 88));
		playlist.addSong(new Song("Heavy Cross", 118));
		playlist.addSong(new Song("Dirty Diana", 131));
		playlist.addSong(new Song("Sweet Child O' Mine", 126));
		playlist.addSong(new Song("Paradise City (1/2)", 100));
		playlist.addSong(new Song("Paradise City (2/2)", 216));
		playlist.addSong(new Song("The Pretender", 87));
		playlist.addSong(new Song("Still Lovin' You", 106));
		playlist.addSong(new Song("Pride", 105));
		playlist.addSong(new Song("R U Kiddin' Me", 178));
		playlist.addSong(new Song("Black Is Black", 96));
		playlist.addSong(new Song("Thunder", 132));
		playlist.addSong(new Song("All Night Long", 138));
		playlist.addSong(new Song("Highway To Hell", 120));
		addPlaylist(db, playlist);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion)
	{
	}

	@Override
    public List<PlaylistHeader> readAllPlaylists()
	{
		final List<PlaylistHeader> playlists = new ArrayList<PlaylistHeader>();
		final Cursor playlistResult = getReadableDatabase().query(kPLAYLIST_TABLE_NAME, new String[] { kPLAYLIST_ID, kPLAYLIST_NAME, kPLAYLIST_WEIGHT }, null, null, null, null, kPLAYLIST_WEIGHT);
		while (playlistResult.moveToNext())
		{
			playlists.add(new PlaylistHeader(playlistResult.getLong(0), playlistResult.getString(1), playlistResult.getInt(2)));
		}
		return playlists;
	}

	@Override
    public Playlist readPlaylist(final long id)
	{
		final Cursor playlistResult = getReadableDatabase().query(kPLAYLIST_TABLE_NAME, new String[] { kPLAYLIST_NAME, kPLAYLIST_WEIGHT }, kPLAYLIST_ID + " = ?", new String[] { Long.toString(id) }, null, null, kPLAYLIST_WEIGHT);
		if (playlistResult.moveToNext())
		{
			final Playlist playlist = new Playlist(id, playlistResult.getString(0), playlistResult.getInt(1));
			final Cursor songResult = getReadableDatabase().query(kSONG_TABLE_NAME, new String[] { kSONG_ID, kSONG_NAME, kSONG_WEIGHT, kSONG_TEMPO }, kSONG_PLAYLIST + " = ?", new String[] { Long.toString(id) }, null, null, kSONG_WEIGHT);
			while (songResult.moveToNext())
			{
				playlist.onlyAddSong(new Song(songResult.getInt(0), songResult.getString(1), songResult.getInt(2), songResult.getInt(3)));
			}
			return playlist;
		}
		return null;
	}

	@Override
    public void storePlaylistHeader(final PlaylistHeader playlist)
	{
		storePlaylistHeader(getWritableDatabase(), playlist);
	}

	@Override
    public int getNextPlaylistWeight()
	{
		final Cursor result = getReadableDatabase().query(kPLAYLIST_TABLE_NAME, new String[] { "MAX(" + kPLAYLIST_WEIGHT + ")" }, null, null, null, null, null);
		if (result.moveToNext())
		{
			return result.getInt(0);
		}
		return 0;
	}

	@Override
    public void addPlaylist(final Playlist playlist)
	{
		addPlaylist(getWritableDatabase(), playlist);
	}

	@Override
    public void deletePlaylist(final PlaylistHeader playlist)
	{
		final String idObject = Long.toString(playlist.getID());
		getWritableDatabase().delete(kSONG_TABLE_NAME, kSONG_PLAYLIST + " = ?", new String[] { idObject });
		getWritableDatabase().delete(kPLAYLIST_TABLE_NAME, kPLAYLIST_ID + " = ?", new String[] { idObject });
	}

	private void addPlaylist(final SQLiteDatabase db, final Playlist playlist)
	{
		storePlaylistHeader(db, playlist);
		for (final Song song : playlist.getSongs())
		{
			storeSong(db, playlist, song);
		}
	}

	private void storePlaylistHeader(final SQLiteDatabase db, final PlaylistHeader playlist)
	{
		final ContentValues values = new ContentValues(1);
		values.put(kPLAYLIST_NAME, playlist.getName());
		values.put(kPLAYLIST_WEIGHT, Integer.valueOf(playlist.getWeight()));
		if (playlist.isNew())
		{
			final long result = db.insert(kPLAYLIST_TABLE_NAME, null, values);
			playlist.setID(result);
		}
		else
		{
			db.update(kPLAYLIST_TABLE_NAME, values, kPLAYLIST_ID + " = ?", new String[] { Long.toString(playlist.getID()) });
		}
	}

	@Override
    public void storeSong(final Playlist playlist, final Song song)
	{
		storeSong(getWritableDatabase(), playlist, song);
	}

	private void storeSong(final SQLiteDatabase db, final PlaylistHeader playlist, final Song song)
	{
		final ContentValues values = new ContentValues(3);
		values.put(kSONG_NAME, song.getName());
		values.put(kSONG_WEIGHT, Integer.valueOf(song.getWeight()));
		values.put(kSONG_TEMPO, Integer.valueOf(song.getTempo()));
		if (song.isNew())
		{
			values.put(kSONG_PLAYLIST, Long.valueOf(playlist.getID()));
			final long result = db.insert(kSONG_TABLE_NAME, null, values);
			song.setID(result);
		}
		else
		{
			db.update(kSONG_TABLE_NAME, values, kSONG_ID + " = ?", new String[] { Long.toString(song.getID()) });
		}
	}

	@Override
    public void deleteSong(final Playlist playlist, final Song song)
	{
		getWritableDatabase().delete(kSONG_TABLE_NAME, kSONG_ID + " = ?", new String[] { Long.toString(song.getID()) });
	}
}
