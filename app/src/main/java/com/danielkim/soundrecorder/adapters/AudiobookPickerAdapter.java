package com.danielkim.soundrecorder.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.danielkim.soundrecorder.Audiobook;
import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.listeners.OnDatabaseChangedListener;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class AudiobookPickerAdapter extends RecyclerView.Adapter<AudiobookPickerAdapter.AudiobooksViewHolder>
    implements OnDatabaseChangedListener{

    private static final String LOG_TAG = "AudiobookPickerAdapter";

    private DBHelper mDatabase;

    Audiobook item;
    Context mContext;
    LinearLayoutManager llm;

    public AudiobookPickerAdapter(Context context, LinearLayoutManager linearLayoutManager) {
        super();
        mContext = context;
        mDatabase = new DBHelper(mContext);
        mDatabase.setOnDatabaseChangedListener(this);
        mDatabase.synchronizeAudiobooksWithFileSystem(Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiobooks");
        llm = linearLayoutManager;
    }

    @Override
    public void onBindViewHolder(final AudiobooksViewHolder holder, int position) {

        item = getItem(position);
        long itemDuration = item.getDuration();

        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);

        holder.alias.setText(item.getName());
        holder.duration.setText(String.format("%02d:%02d", minutes, seconds));
        holder.lastOpened.setText(
            DateUtils.formatDateTime(
                mContext,
                item.getLastOpened(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR
            )
        );

//        // define an on click listener to open PlaybackFragment
//        holder.cardView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    PlaybackFragment playbackFragment =
//                            new PlaybackFragment().newInstance(getItem(holder.getPosition()));
//
//                    FragmentTransaction transaction = ((FragmentActivity) mContext)
//                            .getSupportFragmentManager()
//                            .beginTransaction();
//
//                    playbackFragment.show(transaction, "dialog_playback");
//
//                } catch (Exception e) {
//                    Log.e(LOG_TAG, "exception", e);
//                }
//            }
//        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                ArrayList<String> entries = new ArrayList<String>();
                entries.add(mContext.getString(R.string.dialog_file_rename));

                final CharSequence[] items = entries.toArray(new CharSequence[entries.size()]);


                // File delete confirm
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getString(R.string.dialog_title_options));
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            renameFileDialog(holder.getPosition());
                        }
                    }
                });
                builder.setCancelable(true);
                builder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();

                return false;
            }
        });
    }

    @Override
    public AudiobooksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.card_view, parent, false);

        mContext = parent.getContext();

        return new AudiobooksViewHolder(itemView);
    }

    public static class AudiobooksViewHolder extends RecyclerView.ViewHolder {
        protected TextView alias;
        protected TextView duration;
        protected TextView lastOpened;
        protected View cardView;

        public AudiobooksViewHolder(View v) {
            super(v);
            alias = (TextView) v.findViewById(R.id.file_name_text);
            duration = (TextView) v.findViewById(R.id.file_length_text);
            lastOpened = (TextView) v.findViewById(R.id.file_date_added_text);
            cardView = v.findViewById(R.id.card_view);
        }
    }

    @Override
    public int getItemCount() {
        return mDatabase.getAudiobookCount();
    }

    public Audiobook getItem(int position) {
        return mDatabase.getAudiobookItemAt(position);
    }

    @Override
    public void onNewDatabaseEntryAdded() {
    }

    @Override
    public void onDatabaseEntryRenamed() {

    }

    public void rename(int position, String name) {
        mDatabase.renameAudiobook(getItem(position), name);
        notifyItemChanged(position);
    }

    public void renameFileDialog (final int position) {
        // File rename dialog
        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(mContext);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_rename_audiobook_alias, null);

        final EditText input = (EditText) view.findViewById(R.id.new_name);

        renameFileBuilder.setTitle(mContext.getString(R.string.dialog_alias_rename));
        renameFileBuilder.setCancelable(true);
        renameFileBuilder.setPositiveButton(mContext.getString(R.string.dialog_action_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            String value = input.getText().toString().trim();
                            rename(position, value);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        renameFileBuilder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        renameFileBuilder.setView(view);
        AlertDialog alert = renameFileBuilder.create();
        alert.show();
    }

}
