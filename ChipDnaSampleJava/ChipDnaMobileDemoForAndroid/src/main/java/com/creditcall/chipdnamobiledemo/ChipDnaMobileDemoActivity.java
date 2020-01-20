package com.creditcall.chipdnamobiledemo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.creditcall.chipdnamobile.ChipDnaMobile;
import com.creditcall.chipdnamobile.ICardDetailsListener;
import com.creditcall.chipdnamobile.IConfigurationUpdateListener;
import com.creditcall.chipdnamobile.IConnectAndConfigureFinishedListener;
import com.creditcall.chipdnamobile.IDeferredAuthorizationListener;
import com.creditcall.chipdnamobile.IDeviceUpdateListener;
import com.creditcall.chipdnamobile.IForceAcceptanceListener;
import com.creditcall.chipdnamobile.IPartialApprovalListener;
import com.creditcall.chipdnamobile.IProcessReceiptFinishedListener;
import com.creditcall.chipdnamobile.ISignatureVerificationListener;
import com.creditcall.chipdnamobile.ITmsUpdateListener;
import com.creditcall.chipdnamobile.ITransactionFinishedListener;
import com.creditcall.chipdnamobile.ITransactionUpdateListener;
import com.creditcall.chipdnamobile.IVerifyIdListener;
import com.creditcall.chipdnamobile.IVoiceReferralListener;
import com.creditcall.chipdnamobile.Parameter;
import com.creditcall.chipdnamobile.ParameterKeys;
import com.creditcall.chipdnamobile.ParameterValues;
import com.creditcall.chipdnamobile.Parameters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChipDnaMobileDemoActivity extends AppCompatActivity {

    enum Command {
        SelectPinPad, ConnectPinPad, SubmitTransactionCommand
    }

    enum TransactionCommand {
        Authorisation, Confirm, Void, TransactionInfo
    }

    private static final String TID = "99940710";
    private static final String TK = "0372";
    private static final String APP_ID = "CEMDEMO";
    private static final String     CURRENCY = "USD";

    public static final String LOGGER_STR = "ChipDnaMobileDemo";
    private Button selectPinPadButton;
    private Button connectToPinPadButton;

    private final String[] AMOUNTS = {"1000", "500", "110000", "4723"};
    private DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private String currentlySelectedAmount = AMOUNTS[0];
    private CheckBox autoGenUserRefCheckbox;
    private EditText userRefText;

    private final TransactionCommand[] TRANSACTIONCOMMANDOPTIONS = {TransactionCommand.Authorisation, TransactionCommand.Confirm, //
            TransactionCommand.Void, TransactionCommand.TransactionInfo};
    private TransactionCommand currentTransactionCommandOption = TRANSACTIONCOMMANDOPTIONS[0];
    private Button submitTransactionCommand;

    private TextView loggerTextView;

    private Context context;
    private AlertDialog passwordAlertDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chipdna_mobile_demo);
        this.setupGui();
    }

    @Override
    protected void onResume() {
        super.onResume();

        context = this;

        // Initialise ChipDna Mobile before starting to interacting with the API. You can check if ChipDna Mobile has been initialised by using isInitialised()
        // It's possible that android may have cleaned up resources while the application has been in the background and needs to be re-initialised.
        if (!ChipDnaMobile.isInitialized()) {
            initChipDnaMobile();
        }
    }

    private void setupGui() {
        // Set up the user interface and attach listeners to buttons. The buttons initial state (Enabled/Disabled) is set in the Activities layout file.
        selectPinPadButton = (Button) findViewById(R.id.select_pinpad);
        connectToPinPadButton = (Button) findViewById(R.id.connect_to_pinpad);

        userRefText = (EditText) findViewById(R.id.user_reference);
        autoGenUserRefCheckbox = (CheckBox) findViewById(R.id.autogen_user_ref_checkbox);
        submitTransactionCommand = (Button) findViewById(R.id.submit_transaction_command);

        loggerTextView = (TextView) findViewById(R.id.logger_view);
        loggerTextView.setMovementMethod(new ScrollingMovementMethod());

        selectPinPadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPinPadButtonPushed();
            }
        });

        connectToPinPadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectButtonPushed();
            }
        });

        submitTransactionCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitTransactionCommandButtonPushed();
            }
        });

        // Set up the spinner view, the amounts defined in the stings.xml file.
        Spinner amountsDropdown = (Spinner) findViewById(R.id.amount_dropdown);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.amount_values, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        amountsDropdown.setAdapter(spinnerAdapter);
        amountsDropdown.setOnItemSelectedListener(amountItemSelectedListener);

        // Set up the spinner view, the buttons are defined in the stings.xml file.
        Spinner twoStageDropdown = (Spinner) findViewById(R.id.transaction_command_dropdown);
        ArrayAdapter<CharSequence> transactionCommandSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.transaction_command_values, android.R.layout.simple_spinner_item);
        transactionCommandSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        twoStageDropdown.setAdapter(transactionCommandSpinnerAdapter);
        twoStageDropdown.setOnItemSelectedListener(transactionCommandSelectedListener);
    }

    private synchronized void initChipDnaMobile() {
        if (!ChipDnaMobile.isInitialized()) {

            if (passwordAlertDialog == null || !passwordAlertDialog.isShowing()) {
                requestPassword();
            } else {
                log("Password dialog already showing.");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which activity is returning a result. We are only bothered about the SelectPinPadActivity.
        if (requestCode == SelectPinPadActivity.ACTIVITY_REQUEST_CODE) {
            switch (resultCode) {
                // Result ok, begin the process of connecting to the PINpad so enable the connectToPinPadButton.
                case SelectPinPadActivity.RESULT_OK:
                case SelectPinPadActivity.UPDATE: {
                    log("Selected PIN Pad: " + ChipDnaMobile.getInstance().getStatus(null).getValue(ParameterKeys.PinPadName));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectToPinPadButton.setEnabled(true);
                        }
                    });
                    break;
                }
                case SelectPinPadActivity.RESULT_FAILED: {
                    // Failed to select a PINpad. Exception is logged from SelectPinPadActivity.
                    log("PINpad selection failed");
                    break;
                }
            }
        }
    }

    private void requestPassword() {
        log("Requesting password");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (passwordAlertDialog == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChipDnaMobileDemoActivity.this.context);
                    builder.setTitle("ChipDNA Mobile Password");

                    // User a secure text field to allow the user to enter a password.
                    final EditText input = new EditText(ChipDnaMobileDemoActivity.this.context);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setView(input);

                    // Add a callback for our OK button.
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String inputStr = input.getText().toString();

                            if (inputStr.length() > 0) {

                                new AsyncTask<String, Void, Parameters>() {
                                    @Override
                                    protected Parameters doInBackground(String... params) {
                                        // send a request containing the entered password so it can be handled by ChipDnaMobile.
                                        Parameters requestParameters = new Parameters();
                                        requestParameters.add(ParameterKeys.Password, params[0]);
                                        Parameters response = ChipDnaMobile.initialize(getApplicationContext(), requestParameters);
                                        return response;
                                    }

                                    @Override
                                    protected void onPostExecute(Parameters response) {
                                        if (response.containsKey(ParameterKeys.Result) && response.getValue(ParameterKeys.Result).equalsIgnoreCase("True")) {
                                            // ChipDna Mobile has been successfully initialised.
                                            log("ChipDna Mobile initialised");
                                            log("Version: "+ChipDnaMobile.getSoftwareVersion()+", Name: " +ChipDnaMobile.getSoftwareVersionName());
                                            // We can start setting our ChipDna Mobile credentials.
                                            registerListeners();
                                            setCredentials();
                                        } else {
                                            // The password is incorrect, ChipDnaMobile cannot initialise
                                            log("Failed to initialise ChipDna Mobile");
                                            if(response.getValue(ParameterKeys.RemainingAttempts).equalsIgnoreCase("0")) {
                                                // If all password attempts have been used, the database is deleted and a new password is required.
                                                log("Reached password attempt limit");
                                            } else {
                                                log("Password attempts remaining: " + response.getValue(ParameterKeys.RemainingAttempts));
                                            }
                                            requestPassword();
                                        }
                                    }
                                }.execute(input.getText().toString());

                            }
                        }
                    });

                    // Add a callback for our Cancel button.
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            log("Initialization cancelled by user");
                        }
                    });


                    builder.setCancelable(false);

                    passwordAlertDialog = builder.create();
                }
                passwordAlertDialog.show();
            }
        });
    }

    private void setCredentials() {
        // Credentials are set in ChipDnaMobile Status object. It's recommended that you fetch fresh ChipDnaMobile Status object each time you wish to make changes.
        // This ensures the set of properties used is always up to date with the version of properties in ChipDnaMobile
        final Parameters statusParameters = ChipDnaMobile.getInstance().getStatus(null);

        // Entering this method means we have successfully initialised ChipDna Mobile and start setting our ChipDna Mobile credentials.
        log("Using TID: " + TID + ", TK: "+ TK );

        // Credentials are returned to ChipDnaMobile as a set of Parameters
        Parameters requestParameters = new Parameters();

        // The credentials consist of a terminal ID and transaction key. This demo application has these hard coded.
        // If you don't have a terminal ID or transaction key to use you can sign up for a test WebMIS account at
        // https://testwebmis.creditcall.com
        requestParameters.add(ParameterKeys.TerminalId, TID);
        requestParameters.add(ParameterKeys.TransactionKey, TK);

        // Set ChipDna Mobile to test mode. This means ChipDna Mobile is running in it's test environment, can configure test devices and perform test transaction.
        // Use test mode while developing your application.
        requestParameters.add(ParameterKeys.Environment, ParameterValues.TestEnvironment);

        // Set the Application Identifier value. This is used by the TMS platform to configure TMS properties specifically for an integrating application.
        requestParameters.add(ParameterKeys.ApplicationIdentifier, APP_ID.toUpperCase());

        // Once all changes have been made a call to .setProperties() is required in order for the changes to take effect.
        // Parameters are passed within this method and added to the ChipDna Mobile status object.
        ChipDnaMobile.getInstance().setProperties(requestParameters);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                selectPinPadButton.setEnabled(true);
                // Check if PINpad has already been selected. If so we can enable the connectToPinPadButton.
                if(statusParameters.getValue(ParameterKeys.PinPadName) != null && statusParameters.getValue(ParameterKeys.PinPadName).length() > 0){
                    connectToPinPadButton.setEnabled(true);
                }
            }
        });
    }


     /*
        Button Callbacks
     */

    private void selectPinPadButtonPushed() {
        log("Selecting PINpad");
        // Create a new intent to start the SelectPinPadActivity.
        Intent selectDeviceIntent = new Intent(ChipDnaMobileDemoActivity.this, SelectPinPadActivity.class);
        startActivityForResult(selectDeviceIntent, SelectPinPadActivity.ACTIVITY_REQUEST_CODE);
    }

    private void connectButtonPushed(){
        log("About to connect to PINpad");
        // Use an instance of ChipDnaMobile to begin connectAndConfigure of the device.
        // PINpad checks are completed within connectAndConfigure, deciding whether a TMS update will need to be completed.
        Parameters response = ChipDnaMobile.getInstance().connectAndConfigure(ChipDnaMobile.getInstance().getStatus(null));
        if(response.containsKey(ParameterKeys.Result) && response.getValue(ParameterKeys.Result).equals(ParameterValues.FALSE)) {
            log("Error: " + response.getValue(ParameterKeys.Error));
        }
    }

    private void submitTransactionCommandButtonPushed() {
        log("Starting: " + currentTransactionCommandOption);

        // Request Parameters are used as to communicate - with ChipDna Mobile - the parameters needed to complete a given command.
        // They are sent with the method call to ChipDna Mobile.
        Parameters requestParameters = new Parameters();

        switch (currentTransactionCommandOption) {
            case Authorisation:
                // The following parameters are essential for the completion of a transaction.
                // In the current example the parameters are initialised as constants. They will need to be dynamically collected and initialised.
                requestParameters.add(ParameterKeys.Amount, currentlySelectedAmount);
                requestParameters.add(ParameterKeys.AmountType, ParameterValues.AmountTypeActual);
                requestParameters.add(ParameterKeys.Currency, CURRENCY);

                // The user reference is needed to be to able to access the transaction on WEBMis.
                // The reference should be unique to a transaction, so it is suggested that the reference is generated, similar to the example below.
                if(autoGenUserRefCheckbox.isChecked()) {
                    userRefText.setText(String.format("CDM-%s" ,new SimpleDateFormat("yy-MM-dd-HH.mm.ss").format(new Date())));
                }
                requestParameters.add(ParameterKeys.UserReference, userRefText.getText().toString());

                requestParameters.add(ParameterKeys.TransactionType, ParameterValues.Sale);
                requestParameters.add(ParameterKeys.PaymentMethod, ParameterValues.Card);
                doAuthoriseTransaction(requestParameters);
                break;
            case Confirm:
                // The following parameters are used to confirm an authorised transaction.
                // The user reference is used to reference the transaction stored on WEBMis.
                requestParameters.add(ParameterKeys.UserReference, userRefText.getText().toString());
                requestParameters.add(ParameterKeys.Amount, currentlySelectedAmount);
                requestParameters.add(ParameterKeys.TipAmount, null);
                requestParameters.add(ParameterKeys.CloseTransaction,ParameterValues.TRUE);
                doConfirmTransaction(requestParameters);
                break;
            case Void:
                // The following parameters are used to void an authorised transaction.
                // The user reference is used to reference the transaction stored on WEBMis.
                requestParameters.add(ParameterKeys.UserReference, userRefText.getText().toString());
                doVoidTransaction(requestParameters);
                break;
            case TransactionInfo:
                // The following parameters are used to display information about a transaction.
                // The user reference is used to reference the transaction stored on WEBMis.
                requestParameters.add(ParameterKeys.UserReference, userRefText.getText().toString());
                doGetTransactionInformation(requestParameters);
                break;
        }
    }

    private void doAuthoriseTransaction(final Parameters authorisationParameters){
        log("Starting Transaction for amount: " + currentlySelectedAmount);

        // Use an instance of ChipDnaMobile to begin startTransaction.
        Parameters response = ChipDnaMobile.getInstance().startTransaction(authorisationParameters);

        if(response.containsKey(ParameterKeys.Result) && response.getValue(ParameterKeys.Result).equals(ParameterValues.FALSE)) {
            log("Error: " + response.getValue(ParameterKeys.Error));
        }
    }

    private void doConfirmTransaction(final Parameters confirmParameters) {
        log("Confirm Transaction");

        new AsyncTask<String, Void, Parameters>(){
            @Override
            protected Parameters doInBackground(String... params) {
                return ChipDnaMobile.getInstance().confirmTransaction(confirmParameters);
            }

            @Override
            protected void onPostExecute(Parameters response) {
                log("Confirm Transaction Response", response);
            }
        }.execute();
    }

    private void doVoidTransaction(final Parameters voidParameters) {
        log("Void Transaction");

        new AsyncTask<String, Void, Parameters>(){
            @Override
            protected Parameters doInBackground(String... params) {
                return ChipDnaMobile.getInstance().voidTransaction(voidParameters);
            }

            @Override
            protected void onPostExecute(Parameters response) {
                log("Transaction Void Response", response);
            }
        }.execute();
    }

    private void doGetTransactionInformation(final Parameters transactionInfoParameters) {
        log("Get Transaction Info");
        new AsyncTask<String, Void, Parameters>(){
            @Override
            protected Parameters doInBackground(String... params) {
                return ChipDnaMobile.getInstance().getTransactionInformation(transactionInfoParameters);
            }

            @Override
            protected void onPostExecute(Parameters response) {
                log("Transaction Information Response", response);
            }
        }.execute();
    }

     /*
        Listener Variables
     */

    // Listener for amount dropdown menu. We simply update the current amount with that which has been selected.
    private AdapterView.OnItemSelectedListener amountItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            currentlySelectedAmount = AMOUNTS[position];
            log("Selected: " + getResources().getStringArray(R.array.amount_values)[position]);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Nothing to do here
        }
    };

    // Listener for two-stage option dropdown menu. We simply update the current option with that which has been selected.
    private AdapterView.OnItemSelectedListener transactionCommandSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            currentTransactionCommandOption = TRANSACTIONCOMMANDOPTIONS[position];
            log("Selected: " + getResources().getStringArray(R.array.transaction_command_values)[position]);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Nothing to do here
        }
    };

     /*
        Listeners
     */

    private void registerListeners() {
        ChipDnaMobile.getInstance().addConnectAndConfigureFinishedListener(new ConnectAndConfigureFinishedListener());
        ChipDnaMobile.getInstance().addConfigurationUpdateListener(new ConfigurationUpdateListener());
        ChipDnaMobile.getInstance().addDeviceUpdateListener(new DeviceUpdateListener());
        ChipDnaMobile.getInstance().addCardDetailsListener(new CardDetailsListener());

        TransactionListener transactionListener = new TransactionListener();
        ChipDnaMobile.getInstance().addTransactionUpdateListener(transactionListener);
        ChipDnaMobile.getInstance().addTransactionFinishedListener(transactionListener);
        ChipDnaMobile.getInstance().addDeferredAuthorizationListener(transactionListener);
        ChipDnaMobile.getInstance().addSignatureVerificationListener(transactionListener);
        ChipDnaMobile.getInstance().addVoiceReferralListener(transactionListener);
        ChipDnaMobile.getInstance().addPartialApprovalListener(transactionListener);
        ChipDnaMobile.getInstance().addForceAcceptanceListener(transactionListener);
        ChipDnaMobile.getInstance().addVerifyIdListener(transactionListener);
        ChipDnaMobile.getInstance().addTmsUpdateListener(tmsUpdateListener);
        ChipDnaMobile.getInstance().addProcessReceiptFinishedListener(new ProcessReceiptListener());
    }

    private class ConnectAndConfigureFinishedListener implements IConnectAndConfigureFinishedListener {
        @Override
        public void onConnectAndConfigureFinished(Parameters parameters) {
            if(parameters.containsKey(ParameterKeys.Result) && parameters.getValue(ParameterKeys.Result).equalsIgnoreCase("True")){
                // Configuration has completed successfully and we are ready to perform transactions.
                log("Ready for transactions");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        userRefText.setEnabled(true);
                        autoGenUserRefCheckbox.setEnabled(true);
                        submitTransactionCommand.setEnabled(true);
                    }
                });
            }else {
                // Configuration of the PINpad has failed
                //
                // d. Check error code.
                log("Failed to initialise PINpad");
                log(parameters.getValue(ParameterKeys.Error));
            }
        }
    }

    private class ConfigurationUpdateListener implements IConfigurationUpdateListener {
        @Override
        public void onConfigurationUpdateListener(Parameters parameters) {
            log(parameters.getValue(ParameterKeys.ConfigurationUpdate));
        }
    }

    private class DeviceUpdateListener implements IDeviceUpdateListener {
        @Override
        public void onDeviceUpdate(Parameters parameters) {
            log(parameters.getValue(ParameterKeys.DeviceStatusUpdate));
        }
    }

    // This is not used in this version of the demo app.
    private class CardDetailsListener implements ICardDetailsListener {
        @Override
        public void onCardDetails(Parameters parameters) {
        }
    }

    private class TransactionListener implements ITransactionUpdateListener, ITransactionFinishedListener,
            IDeferredAuthorizationListener, ISignatureVerificationListener, IVoiceReferralListener,
            IPartialApprovalListener, IForceAcceptanceListener, IVerifyIdListener {
        @Override
        public void onTransactionUpdateListener(Parameters parameters) {
            log(parameters.getValue(ParameterKeys.TransactionUpdate));
        }

        @Override
        public void onTransactionFinishedListener(Parameters parameters) {
            log("onTransactionFinishedListener =>", parameters);
        }

        @Override
        public void onSignatureVerification(Parameters parameters) {
            log("Signature Check Required");

            if (!parameters.getValue(ParameterKeys.ResponseRequired).equals(ParameterValues.TRUE)) {
                // Signature handled on PINpad. No call to ChipDna Mobile required.
                return;
            }

            final boolean operatorPinRequired = parameters.getValue(ParameterKeys.OperatorPinRequired).equals(ParameterValues.TRUE);
            final String receiptDataXml = parameters.getValue(ParameterKeys.ReceiptData);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    requestSignatureCheck(operatorPinRequired, false, receiptDataXml);
                }
            }).start();
        }

        @Override
        public void onVoiceReferral(Parameters parameters) {
            log("Voice Referral Check Required");

            if (!parameters.getValue(ParameterKeys.ResponseRequired).equals(ParameterValues.TRUE)) {
                // Voice referral handled on PINpad. No call to ChipDna Mobile required.
                return;
            }

            final String phoneNumber = parameters.getValue(ParameterKeys.ReferralNumber);
            final boolean operatorPinRequired = parameters.getValue(ParameterKeys.OperatorPinRequired).equals(ParameterValues.TRUE);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    requestVoiceReferral(phoneNumber, operatorPinRequired);
                }
            }).start();
        }

          /*
            Other ChipDna Mobile Callbacks, not required in this demo.
            You may need to implement some of these depending on what your terminal supports.
          */

        @Override
        public void onVerifyId(Parameters parameters) {

        }

        @Override
        public void onDeferredAuthorizationListener(Parameters parameters) {

        }

        @Override
        public void onForceAcceptance(Parameters parameters) {

        }

        @Override
        public void onPartialApproval(Parameters parameters) {

        }
    }

    private ITmsUpdateListener tmsUpdateListener = new ITmsUpdateListener() {
        @Override
        public void onTmsUpdate(Parameters tmsUpdateParameters) {

        }
    };

    private class ProcessReceiptListener implements IProcessReceiptFinishedListener {
        @Override
        public void onProcessReceiptFinishedListener(Parameters parameters) {

        }
    }


     /*
        Logging
     */

    private ArrayList<String> logs = new ArrayList<String>();
    final int MAX_LOG = 100;
    private static Object LoggingLock = new Object();

    // Logging method which takes the string to be logged and display it in the logger text view.
    private void log(String toLog) {
        synchronized (LoggingLock) {
            if (logs.size() == MAX_LOG) {
                logs.remove(0);
            }

            logs.add(String.format("%s: %s\n", df.format(new Date()), toLog));
            Log.d(LOGGER_STR, toLog);
            StringBuilder sb = new StringBuilder("");
            for (String log : logs) {
                sb.append(String.format("%s", log));
            }
            final String logStr = sb.toString();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loggerTextView.setText(logStr);
                }
            });
        }
    }

    private void log(String title, Parameters parameters) {
        StringBuilder formattedLogBuilder = new StringBuilder();
        formattedLogBuilder.append(title);

        if(parameters != null) {
            for (Parameter parameter : parameters.toList()) {
                formattedLogBuilder.append(String.format("\t[%s]\n", parameter));
            }
        }

        log(formattedLogBuilder.toString());
    }


     /*
        Requests
     */

    private DialogInterface.OnClickListener getTerminateOnClickListener(){
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                log("ChipDnaMobile.terminateTransaction");
                Parameters response = ChipDnaMobile.getInstance().terminateTransaction(new Parameters());

                if(response.containsKey(ParameterKeys.Result) && response.getValue(ParameterKeys.Result).equals(ParameterValues.FALSE)) {
                    log("Error: " + response.getValue(ParameterKeys.Error));
                }
                log("Signature Check Terminated");
            }
        };
    }

    private void requestSignatureCheck(final boolean operatorPinRequired, final boolean signatureCaptureSupported, final String receiptDataXml) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                AlertDialog.Builder builder = new AlertDialog.Builder(ChipDnaMobileDemoActivity.this.context);
                builder.setTitle("Please check signature.");
                final EditText input = new EditText(ChipDnaMobileDemoActivity.this.context);

                // If operator PIN is required, Add an extra text field to allow the user to enter the operator PIN.
                if(operatorPinRequired){
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    input.setHint("Operator PIN");
                    builder.setView(input);
                }

                builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputStr = input.getText().toString();
                        if(operatorPinRequired){
                            // Check we have an operator PIN
                            if(inputStr.length() == 0) {
                                // No operator PIN found. Send request again.
                                log("Operator PIN required but not entered");
                                requestSignatureCheck(operatorPinRequired, signatureCaptureSupported, receiptDataXml);
                                return;
                            }
                        }

                        Parameters approveSignatureParameters = new Parameters();
                        approveSignatureParameters.add(ParameterKeys.Result, ParameterValues.TRUE);
                        approveSignatureParameters.add(ParameterKeys.OperatorPin, inputStr);

                        Parameters response = ChipDnaMobile.getInstance().continueSignatureVerification(approveSignatureParameters);

                        if (!response.containsKey(ParameterKeys.Result) || response.getValue(ParameterKeys.Result).equals(ParameterValues.FALSE)) {
                            requestSignatureCheck(operatorPinRequired, signatureCaptureSupported, receiptDataXml);
                        }

                    }
                });

                builder.setNegativeButton("Terminate", getTerminateOnClickListener());

                builder.setNeutralButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // The merchant wishes to decline the transaction.
                        // No operator PIN is required when declining the transaction.
                        Parameters declineParameters = new Parameters();
                        declineParameters.add(ParameterKeys.Result, ParameterValues.FALSE);
                        log("Signature Check Declined");
                        Parameters response = ChipDnaMobile.getInstance().continueSignatureVerification(declineParameters);

                        if (!response.containsKey(ParameterKeys.Result) || response.getValue(ParameterKeys.Result).equals(ParameterValues.FALSE)) {
                            requestSignatureCheck(operatorPinRequired, signatureCaptureSupported, receiptDataXml);
                        }
                    }
                });

                builder.show();
            }
        });
    }

    private void requestVoiceReferral(final String phoneNumber, final boolean operatorPinRequired) {
        log("Requesting voice referral");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ChipDnaMobileDemoActivity.this.context);
                LinearLayout contentView = new LinearLayout(ChipDnaMobileDemoActivity.this.context);
                contentView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                contentView.setOrientation(LinearLayout.VERTICAL);

                builder.setTitle("Voice Referral");
                TextView requestText = new TextView(ChipDnaMobileDemoActivity.this.context);

                // If returned display phone number for the merchant to call.
                if(phoneNumber != null && phoneNumber.length() > 0){
                    requestText.setText("Please ring your bank: "+phoneNumber);
                }else{
                    requestText.setText("Please ring your bank.");
                }
                contentView.addView(requestText);

                final EditText operatorPinInput = new EditText(ChipDnaMobileDemoActivity.this.context);
                operatorPinInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                operatorPinInput.setHint("Operator PIN");

                // If required add an extra text field to allow the user to enter their operator PIN.
                if(operatorPinRequired){
                    TextView operatorLabel = new TextView(ChipDnaMobileDemoActivity.this.context);
                    operatorLabel.setText("Operator PIN:");

                    contentView.addView(operatorLabel);
                    contentView.addView(operatorPinInput);
                }

                // Add a text field for the merchant to enter the authorization code given to them by the bank.
                final EditText authCodeInput = new EditText(ChipDnaMobileDemoActivity.this.context);
                authCodeInput.setInputType(InputType.TYPE_CLASS_TEXT);
                authCodeInput.setHint("Auth Code");
                TextView authCodeLabel = new TextView(ChipDnaMobileDemoActivity.this.context);
                authCodeLabel.setText("Authorization Code:");

                contentView.addView(authCodeLabel);
                contentView.addView(authCodeInput);

                builder.setView(contentView);

                builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // The bank has given the authorization code and the merchant can proceed with the transaction.
                        if(operatorPinRequired){
                            // If required check we have been given an operator PIN.
                            String inputStr = operatorPinInput.getText().toString();
                            if(inputStr.length() == 0) {
                                log("Operator PIN required but not entered");
                                // If no operator PIN, try again.
                                requestVoiceReferral(phoneNumber, operatorPinRequired);
                                return;
                            }
                        }

                        String authCodeStr = authCodeInput.getText().toString();
                        String operatorPinStr = operatorPinInput.getText().toString();

                        // Check we have an authorization code.
                        if(authCodeStr == null || authCodeStr.length() < 1){
                            log("Authorization code required and not entered");
                            // If not try again.
                            requestVoiceReferral(phoneNumber, operatorPinRequired);
                            return;
                        }

                        Parameters voiceReferralParameters = new Parameters();
                        voiceReferralParameters.add(ParameterKeys.Result, ParameterValues.TRUE);
                        voiceReferralParameters.add(ParameterKeys.AuthCode, authCodeStr);
                        voiceReferralParameters.add(ParameterKeys.OperatorPin, operatorPinStr);

                        Parameters response = ChipDnaMobile.getInstance().continueVoiceReferral(voiceReferralParameters);

                        if(!response.containsKey(ParameterKeys.Result) || response.getValue(ParameterKeys.Result).equals(ParameterValues.FALSE)){
                            requestVoiceReferral(phoneNumber, operatorPinRequired);
                        }
                    }
                });


                builder.setNegativeButton("Terminate", getTerminateOnClickListener());

                // The bank has instructed the merchant to decline the transaction. No authorization code or operator PIN is necessary.
                builder.setNeutralButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Parameters voiceReferralParameters = new Parameters();
                        voiceReferralParameters.add(ParameterKeys.Result, ParameterValues.FALSE);

                        log("Voice Referral Declined");
                        Parameters response = ChipDnaMobile.getInstance().continueVoiceReferral(voiceReferralParameters);

                        if(response.containsKey(ParameterKeys.Result) && response.getValue(ParameterKeys.Result).equals(ParameterValues.FALSE)) {
                            log("Error: " + response.getValue(ParameterKeys.Error));
                        }

                    }
                });

                builder.show();
            }
        });
    }
}
