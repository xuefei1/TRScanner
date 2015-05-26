package trlabs.trscanner.ui;


    import android.app.AlertDialog;
    import android.app.Dialog;
    import android.content.Context;
    import android.content.DialogInterface;
    import android.os.Bundle;
    import android.provider.Settings;
    import android.support.v4.app.DialogFragment;
    import android.text.Editable;
    import android.text.TextWatcher;
    import android.view.KeyEvent;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.widget.EditText;
    import android.widget.ImageButton;
    import android.widget.TextView;
    import android.widget.Toast;

    import java.io.StringBufferInputStream;

    import cn.pedant.SweetAlert.SweetAlertDialog;
    import trlabs.trscanner.R;
    import trlabs.trscanner.trtabs.FileCategoryActivity;
    import trlabs.trscanner.trtabs.config.GlobalConsts;
    import trlabs.trscanner.users.journals.Journal;
    import trlabs.trscanner.users.journals.JournalDB;
    import trlabs.trscanner.utils.DialogUtil;

public class JournalDialog extends DialogFragment {
        // non default constructor in fragment to access parent activity data, just call getActivity()
        JournalDB journalDB;


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            final View v = inflater.inflate(R.layout.journal_dialog, null);
            final EditText userInput = (EditText) v.findViewById(R.id.journal_textEdit);
            final ImageButton btn_close = (ImageButton) v.findViewById(R.id.journal_close_button);

            journalDB = new JournalDB(getActivity().getApplicationContext());
            if (journalDB.isExistingKey(GlobalConsts.CALENDAR_CURRENT_DATE)) {
                userInput.setText(journalDB.getJournalByDate(GlobalConsts.CALENDAR_CURRENT_DATE).getContent());
                TextView counter = (TextView) v.findViewById(R.id.journal_wordcount);
                counter.setText(String.valueOf(userInput.length()));
            }

            btn_close.setOnClickListener(new View.OnClickListener() {
                @Override
                    public void onClick(View v ){
                    //http://stackoverflow.com/questions/11201022/how-to-correctly-dismiss-a-dialogfragment
                        getDialog().dismiss();
                }
            });

            userInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //String[] words = s.toString().split(" ");
                    //if (words.length > Constants.MAX_JOURNAL_WORDS) {
                    TextView counter = (TextView) v.findViewById(R.id.journal_wordcount);
                    counter.setText(String.valueOf(s.length()));
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

           userInput.setOnKeyListener(new View.OnKeyListener() {
               public boolean onKey(View v, int keyCode, KeyEvent event) {

                    // if enter is pressed start calculating
                   if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
                       int editTextLineCount = ((EditText)v).getLineCount();
                       if (editTextLineCount >= GlobalConsts.JOURNAL_MAX_LINES)
                           return true;
                   }
                   return false;
               }


           });


            builder.setView(v)
                    // Add action buttons
                    .setPositiveButton(R.string.journal_enter_button,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (GlobalConsts.JOURNAL_REEDIT) {
                                         //journalDB.deleteByDate(GlobalConsts.CALENDAR_CURRENT_DATE);
                                        journalDB.updateColumn(GlobalConsts.CALENDAR_CURRENT_DATE, "", userInput.getText().toString());
                                        GlobalConsts.JOURNAL_REEDIT = false;

                                    } else {
                                        Journal journal = new Journal(null, userInput.getText().toString(), GlobalConsts.CALENDAR_CURRENT_DATE);
                                        journalDB.save_with_id(journal);

                                    }
                                    // Journal jj = journalDB.getJournalByDate(GlobalConsts.CALENDAR_CURRENT_DATE);
                                    // journalDB.save_with_id(journal);
                                    // Journal jb = journalDB.getJournalById((int)i);

                                }

                            })
                    .setNegativeButton(R.string.journal_delete_button, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            journalDB.deleteByDate(GlobalConsts.CALENDAR_CURRENT_DATE);
                            Toast.makeText(getActivity(), R.string.journal_file_delete,
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            getDialog().dismiss();
                        }
                    });

            // Create the AlertDialog object and return it
            return builder.create();
        }



        // /**
        // * The system calls this to get the DialogFragment's layout, regardless of
        // * whether it's being displayed as a dialog or an embedded fragment.
        // */
        // @Override
        // public View onCreateView(LayoutInflater inflater, ViewGroup container,
        // Bundle savedInstanceState) {
        // // Inflate the layout to use as dialog or embedded fragment
        // return inflater.inflate(R.layout.dialog_signin, container, false);
        // }
        //
        // /** The system calls this only when creating the layout in a dialog. */
        // @Override
        // public Dialog onCreateDialog(Bundle savedInstanceState) {
        // // The only reason you might override this method when using
        // // onCreateView() is to modify any dialog characteristics. For example,
        // // the dialog includes a title by default, but your custom layout might
        // // not need it. So here you can remove the dialog title, but you must
        // // call the superclass to get the Dialog.
        // Dialog dialog = super.onCreateDialog(savedInstanceState);
        // dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // return dialog;
        // }

    }