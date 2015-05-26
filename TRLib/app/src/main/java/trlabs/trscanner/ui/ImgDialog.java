package trlabs.trscanner.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;

import java.io.File;

import trlabs.trscanner.R;
import trlabs.trscanner.trtabs.config.GlobalConsts;
import trlabs.trscanner.utils.BitmapHelper;
import trlabs.trscanner.utils.CircularImageView;
import trlabs.trscanner.utils.Constants;

public class ImgDialog extends DialogFragment {

    Context mContext;
    private Uri origUri;
    private Uri cropUri;
    private File protraitFile;
    private String protraitPath;
    private String origFileName;
    private String cropFileName;
    private final static int CROP = 200;
    CircularImageView user_icon;

    public ImgDialog() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String FILE_SAVEPATH = GlobalConsts.ROOT_PATH;
        origFileName = getArguments().getString("origFileName");
        cropFileName = getArguments().getString("cropFileName");
        protraitPath = FILE_SAVEPATH + cropFileName;
        protraitFile = new File(protraitPath);
        origUri = Uri.fromFile(new File(FILE_SAVEPATH, origFileName));
        cropUri = Uri.fromFile(protraitFile);
        GlobalConsts.protraitPath = protraitPath;
        GlobalConsts.origUri = origUri;
        GlobalConsts.cropUri = cropUri;

        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.upload_img))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.img_from_album,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                startActionPickCrop(cropUri);
                                //getActivity().onActivityResult(getTargetRequestCode(), Constants.Extra.REQUEST_CODE_GETIMAGE_BYSDCARD, get.getIntent());
                            }
                        }
                )
                .setNegativeButton(R.string.img_from_camera,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            startActionCamera(origUri);
                            //getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
                    }
                })
                .create();
    }

    private void startActionPickCrop(Uri output) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra("output", output);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", CROP);
        intent.putExtra("outputY", CROP);
        getActivity().startActivityForResult(Intent.createChooser(intent, "select image"), Constants.Extra.REQUEST_CODE_GETIMAGE_BYSDCARD);
    }

    private void startActionCamera(Uri output) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
        getActivity().startActivityForResult(intent, Constants.Extra.REQUEST_CODE_GETIMAGE_BYCAMERA);
    }

    public static ImgDialog newInstance(Context context) {
        ImgDialog dialogFragment = new ImgDialog();
        dialogFragment.mContext = context;
        return dialogFragment;
    }
}
