package com.darker.fwvideoplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.PermissionChecker;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.darker.fwvideoplayer.adapter.VideoGridAdapter;
import com.darker.fwvideoplayer.adapter.VideoListAdapter;
import com.darker.fwvideoplayer.constant.ESortStyle;
import com.darker.fwvideoplayer.custom_dialog.SortSheetDialog;
import com.darker.fwvideoplayer.helper.MdlVideoLoader;
import com.darker.fwvideoplayer.models.MdlVideo;

import java.io.Serializable;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<MdlVideo>>,
        VideoListAdapter.OnSelected, View.OnClickListener, VideoGridAdapter.OnSelected, SortSheetDialog.IClickSortDialog {

    private static final String TAG = "MainActivity";
    final static int READ_EXTERNAL_STORAGE = 4376;
    RelativeLayout lNoData;
    RecyclerView listView, listViewGrid;
    VideoListAdapter listAdapter;
    VideoGridAdapter gridAdapter;
    private RecyclerView.LayoutManager mLayoutManagerList, mLayoutManagerGrid;
    Parcelable state, stateGrid;

    List<MdlVideo> listVideo = new ArrayList<>();
    ImageView ivGrid, ivList;
    TextView tvError;

    private Menu mainMenu = null;
    private boolean isGrid = true;
    private ESortStyle mainSortStyle = ESortStyle.DATE_NEW;

    private String[] listPermission = { Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lNoData = findViewById(R.id.l_no_data);
        ivGrid = findViewById(R.id.iv_grid);
        listViewGrid = findViewById(R.id.listViewGrid);
        ivList = findViewById(R.id.iv_list);
        listView = findViewById(R.id.listView);
        tvError = findViewById(R.id.tv_error);

        GetDevicePermission();
        lNoData.setVisibility(View.GONE);
        ivList.setVisibility(View.GONE);
        ivGrid.setVisibility(View.VISIBLE);

        ivList.setOnClickListener(this);
        ivGrid.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.mainMenu = menu;
        getMenuInflater().inflate(R.menu.main_activity_menu, mainMenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_layout_style:
                if (isGrid) {
                    mainMenu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_view_list_black));
                    isGrid = false;
                    OnSetAdapter(true);
                }
                else {
                    mainMenu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_view_module_black));
                    isGrid = true;
                    OnSetAdapter(false);
                }
                return true;
            case R.id.action_sort:
                SortSheetDialog sortSheetDialog = new SortSheetDialog();
                sortSheetDialog.setClickSortDialog(this);
                sortSheetDialog.show(getSupportFragmentManager().beginTransaction(), SortSheetDialog.TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void OnSetAdapter(boolean isList){
        if (isList){
            listView.scrollToPosition(0);
            listView.setVisibility(View.VISIBLE);
            listViewGrid.setVisibility(View.GONE);
        }
        else {
            listViewGrid.scrollToPosition(0);
            listView.setVisibility(View.GONE);
            listViewGrid.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            state = mLayoutManagerList.onSaveInstanceState();
            stateGrid = mLayoutManagerGrid.onSaveInstanceState();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            if (listView.getVisibility() == View.VISIBLE)
                editor.putBoolean("isList", true);
            else
                editor.putBoolean("isList", false);
            editor.apply();
        }
        catch (Exception e){
            Log.e(TAG, "onPause: " + e);
        }
    }

    private void GetDevicePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
            PermissionChecker.PERMISSION_GRANTED){
                requestPermissions(listPermission, READ_EXTERNAL_STORAGE);
            }
            else {
                LoaderManager.getInstance(this).initLoader(143, null, this).forceLoad();
            }
        }
        else {
            LoaderManager.getInstance(this).initLoader(143, null, this).forceLoad();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_EXTERNAL_STORAGE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //getSupportLoaderManager().initLoader(0,null,this);
                LoaderManager.getInstance(this).initLoader(143, null, this).forceLoad();
            }
            else if (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "onRequestPermissionsResult: " + "Write External Storage");
            }
            else
                {
                    lNoData.setVisibility(View.VISIBLE);
                    tvError.setText(R.string.permission_denied);
                    listView.setVisibility(View.GONE);
                    listViewGrid.setVisibility(View.GONE);
                }
        }
    }

    @NonNull
    @Override
    public Loader<List<MdlVideo>> onCreateLoader(int id, @Nullable Bundle args) {
        return new MdlVideoLoader(getApplicationContext(), mainSortStyle);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<MdlVideo>> loader, List<MdlVideo> data) {
        this.listVideo = data;

        mLayoutManagerList = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mLayoutManagerGrid = new GridLayoutManager(this, 2);
        listView.setLayoutManager(mLayoutManagerList);
        listViewGrid.setLayoutManager(mLayoutManagerGrid);

        listAdapter = new VideoListAdapter(this, listVideo);
        gridAdapter = new VideoGridAdapter(this, listVideo);
        listView.setAdapter(listAdapter);
        listViewGrid.setAdapter(gridAdapter);

        listView.setVisibility(View.GONE);
        listViewGrid.setVisibility(View.VISIBLE);

        ivGrid.setVisibility(View.VISIBLE);
        ivList.setVisibility(View.GONE);
        mLayoutManagerList.onRestoreInstanceState(state);
        mLayoutManagerGrid.onRestoreInstanceState(stateGrid);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<MdlVideo>> loader) {
        listAdapter = new VideoListAdapter(this, null);
        gridAdapter = new VideoGridAdapter(this, null);
        listView.setAdapter(listAdapter);
    }

    @Override
    public void OnClickItem(MdlVideo video, List<MdlVideo> listItem, int position) {
        Intent i = new Intent(this, VideoPlayerActivity.class);
        i.putExtra("isMain", true);
        i.putExtra("mdlVideo", video);
        i.putExtra("list", (Serializable)listItem);
        startActivity(i);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_grid:
            case R.id.iv_list:
                if (ivList.getVisibility() == View.VISIBLE){
                    if (listVideo == null)
                        break;
                    ivGrid.setVisibility(View.VISIBLE);
                    ivList.setVisibility(View.GONE);
                    OnSetAdapter(false);
                }
                else {
                    if (listVideo == null)
                        break;
                    ivGrid.setVisibility(View.GONE);
                    ivList.setVisibility(View.VISIBLE);
                    OnSetAdapter(true);
                }
                break;
        }
    }

    @Override
    public void onClickSortDialog(ESortStyle sortStyle) {
        mainSortStyle = sortStyle;
        LoaderManager.getInstance(this).restartLoader(143, null, this).forceLoad();
    }
}
