package trlabs.trscanner.trtabs.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import trlabs.trscanner.R;
import trlabs.trscanner.trtabs.File.FileIconHelper;
import trlabs.trscanner.trtabs.File.FileInfo;
import trlabs.trscanner.trtabs.File.FileListItem;
import trlabs.trscanner.trtabs.File.FileViewInteractionHub;

public class FileListAdapter extends ArrayAdapter<FileInfo> {
    private LayoutInflater mInflater;

    private FileViewInteractionHub mFileViewInteractionHub;

    private FileIconHelper mFileIcon;

    private Context mContext;

    public FileListAdapter(Context context, int resource,
            List<FileInfo> objects, FileViewInteractionHub f,
            FileIconHelper fileIcon) {
        super(context, resource, objects);
        mInflater = LayoutInflater.from(context);
        mFileViewInteractionHub = f;
        mFileIcon = fileIcon;
        mContext = context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(R.layout.file_browser_item, parent, false);
        }

        FileInfo lFileInfo = mFileViewInteractionHub.getItem(position);
        FileListItem.setupFileListItemInfo(mContext, view, lFileInfo,
                mFileIcon, mFileViewInteractionHub);
        view.findViewById(R.id.file_checkbox_area).setOnClickListener(
                new FileListItem.FileItemOnClickListener(mContext,
                        mFileViewInteractionHub));
        return view;
    }


}























