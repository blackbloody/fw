package com.darker.fwvideoplayer.custom_dialog;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.util.ContentLengthInputStream;
import com.darker.fwvideoplayer.R;
import com.darker.fwvideoplayer.constant.ESortStyle;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SortSheetDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    public static final String TAG = "SortSheetDialog";
    private SortSheetDialog.IClickSortDialog clickSortDialog;

    public void setClickSortDialog(IClickSortDialog clickSortDialog) {
        this.clickSortDialog = clickSortDialog;
    }
    public interface IClickSortDialog {
        void onClickSortDialog(ESortStyle sortStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sort_sheet_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        v.findViewById(R.id.tv_date_new).setOnClickListener(this);
        v.findViewById(R.id.tv_date_old).setOnClickListener(this);
        v.findViewById(R.id.tv_size_large).setOnClickListener(this);
        v.findViewById(R.id.tv_size_small).setOnClickListener(this);
        v.findViewById(R.id.tv_duration_large).setOnClickListener(this);
        v.findViewById(R.id.tv_duration_short).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_date_new:
                clickSortDialog.onClickSortDialog(ESortStyle.DATE_NEW);
                break;
            case R.id.tv_date_old:
                clickSortDialog.onClickSortDialog(ESortStyle.DATE_OLD);
                break;
            case R.id.tv_size_large:
                clickSortDialog.onClickSortDialog(ESortStyle.SIZE_LARGE);
                break;
            case R.id.tv_size_small:
                clickSortDialog.onClickSortDialog(ESortStyle.SIZE_SMALL);
                break;
            case R.id.tv_duration_large:
                clickSortDialog.onClickSortDialog(ESortStyle.DURATION_LONGEST);
                break;
            case R.id.tv_duration_short:
                clickSortDialog.onClickSortDialog(ESortStyle.DURATION_SHORTEST);
                break;
        }
        dismiss();
    }
}
