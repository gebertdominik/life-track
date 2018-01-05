package pl.gebert.lifetrack;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FilePickActivity extends Activity {
    private ListView fileListView;
    private ArrayAdapter<String> fileListViewAdapter;
    private File filesDir;
    File[] files;
    ArrayList<String> listItems = new ArrayList<String>();
    Map<String,String> filenamePathMap = new HashMap<String,String>();
    Intent shareIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_pick);
        shareIntent = new Intent(Intent.ACTION_SEND);
        fileListView = findViewById(R.id.fileList);

        filesDir = getExternalFilesDir(null);
        files = filesDir.listFiles();

        for (File file : files) {
            listItems.add(file.getName());
            filenamePathMap.put(file.getName(),file.getAbsolutePath());
        }
        Collections.sort(listItems);
        fileListViewAdapter = new ArrayAdapter<String>(this, R.layout.file_list_item, listItems);
        fileListView.setAdapter(fileListViewAdapter);
        fileListView.setClickable(true);
        fileListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId) {
                        File requestFile = new File(filenamePathMap.get(listItems.get(position)));
                        try {
                            Uri fileUri = FileProvider.getUriForFile(FilePickActivity.this,"pl.gebert.lifetrack.FileProvider", requestFile);
                            if (fileUri != null) {
                                shareIntent = crateFileShareIntent(fileUri);
                                startActivity(Intent.createChooser(shareIntent, "Share file"));
                                FilePickActivity.this.setResult(Activity.RESULT_OK, shareIntent);
                            } else {
                                shareIntent.setDataAndType(null, "");
                                FilePickActivity.this.setResult(RESULT_CANCELED, shareIntent);
                            }

                        } catch (IllegalArgumentException e) {
                            Log.e("FilePickActivity",
                                    "The selected file can't be shared: " + e.getMessage());
                        } catch (Exception e) {
                            Log.e("FilePickActivity",
                                    "Unexpected exception: " + e.getMessage());
                        }
                    }
                });
    }

    private Intent crateFileShareIntent(Uri fileUri){
        Intent intent= new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(fileUri, getContentResolver().getType(fileUri));
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        return intent;
    }
}
