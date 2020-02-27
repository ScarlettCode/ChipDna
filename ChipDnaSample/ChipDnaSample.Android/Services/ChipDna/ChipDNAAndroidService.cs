using System;
using System.Collections.Generic;
using Com.Creditcall.Chipdnamobile;
using Prism.Events;

namespace ChipDnaSample.Droid.Services.ChipDna
{
  public class ChipDNAAndroidService : IChipDNAService
  {
    private readonly IEventAggregator _eventAggregator;

    public ChipDNAAndroidService(IEventAggregator eventAggregator)
    {
      _eventAggregator = eventAggregator;
    }

    private static bool _debugMode
    {
      get
      {
#if DEBUG
        {
          return true;
        }
#else
        {
          return false;
        }
#endif
      }
    }

    public void Dispose()
    {
      ChipDnaMobile.Instance.Dispose();
    }

    public bool IsInitialize() => ChipDnaMobile.IsInitialized;

    public void Initialize(string terminalId, string terminalKey, string password)
    {
      if (IsInitialize())
      {
        ChipDnaMobile.Dispose(null);
      }

      if (!IsInitialize())
      {
        Parameters requestParameters = new Parameters();
        requestParameters.Add(ParameterKeys.Password, password);
        Parameters response = ChipDnaMobile.Initialize(Android.App.Application.Context, requestParameters);

        if (response.ContainsKey(ParameterKeys.Result) && response.GetValue(ParameterKeys.Result).Equals(ParameterValues.True, StringComparison.InvariantCultureIgnoreCase))
        {
          RegisterListeners();

          SetCredentials(terminalId, terminalKey);
        }
        else
        {
          // The password is incorrect, ChipDnaMobile cannot initialise
          if (response.GetValue(ParameterKeys.RemainingAttempts).Equals("0"))
          {
            // If all password attempts have been used, the database is deleted and a new password is required.
            var passwordAttempts = response.GetValue(ParameterKeys.RemainingAttempts);
          }
          else
          {
            var passwordAttempts = response.GetValue(ParameterKeys.RemainingAttempts);
          }

          SetCredentials(terminalId, terminalKey);
        }
      }

      //var pinPad = GetCurrentPinPad();
      //if (pinPad.DeviceName.Length > 0)
      //{
      //  ConnectPinPad();
      //}
    }

    public MiuraDevice GetCurrentPinPad()
    {
      var statusParameters = ChipDnaMobile.Instance.GetStatus(new Parameters());

      var pinPadName = statusParameters.GetValue(ParameterKeys.PinPadName);
      var pinPadConnectionType = statusParameters.GetValue(ParameterKeys.PinPadConnectionType);

      return new MiuraDevice
      {
        ConnectionType = pinPadConnectionType,
        DeviceName = pinPadName
      };
    }

    private void SetCredentials(string terminalId, string terminalKey)
    {
      // Credentials are set in ChipDnaMobile Status object. It's recommended that you fetch fresh ChipDnaMobile Status object each time you wish to make changes.
      // This ensures the set of properties used is always up to date with the version of properties in ChipDnaMobile
      var statusParameters = ChipDnaMobile.Instance.GetStatus(null);

      // Entering this method means we have successfully initialised ChipDna Mobile and start setting our ChipDna Mobile credentials.
      //log("Using _terminalId: " + _terminalId + ", _terminalKey: "+ _terminalKey );

      // Credentials are returned to ChipDnaMobile as a set of Parameters
      Parameters requestParameters = new Parameters();

      // The credentials consist of a terminal ID and transaction key
      requestParameters.Add(ParameterKeys.TerminalId, terminalId);
      requestParameters.Add(ParameterKeys.TransactionKey, terminalKey);

      // Set ChipDna Mobile to test mode. This means ChipDna Mobile is running in it's test environment, can configure test devices and perform test transaction.
      // Use test mode while developing your application.

      if (_debugMode)
      {
        requestParameters.Add(ParameterKeys.Environment, ParameterValues.TestEnvironment);
      }
      else
      {
        requestParameters.Add(ParameterKeys.Environment, ParameterValues.LiveEnvironment);
      }

      // Set the Application Identifier value. This is used by the TMS platform to configure TMS properties specifically for an integrating application.
      requestParameters.Add(ParameterKeys.ApplicationIdentifier, "KUULEATS");

      ChipDnaMobile.Instance.SetProperties(requestParameters);
    }

    public void GetAvailablePinPads()
    {
      Parameters parameters = new Parameters();

      parameters.Add(ParameterKeys.SearchConnectionTypeBluetooth, ParameterValues.True);

      ChipDnaMobile.Instance.ClearAllAvailablePinPadsListeners();

      var availablePinPadsListener = new AvailablePinPadsListener(_eventAggregator);
      ChipDnaMobile.Instance.AddAvailablePinPadsListener(availablePinPadsListener);

      var response = ChipDnaMobile.Instance.GetAvailablePinPads(parameters);

      if (response.ContainsKey(ParameterKeys.Result) && response.GetValue(ParameterKeys.Result).Equals(ParameterValues.False, StringComparison.CurrentCultureIgnoreCase))
      {
        var error = response.GetValue(ParameterKeys.Errors);
      }

      if (response.ContainsKey(ParameterKeys.Result) && response.GetValue(ParameterKeys.Result).Equals(ParameterValues.True, StringComparison.CurrentCultureIgnoreCase))
      {
        var result = response.GetValue(ParameterKeys.Result);
      }
    }

    public bool SetSelectedPinPad(string pinPadName, string pinPadConnectionType)
    {
      Parameters requestParameters = new Parameters();
      requestParameters.Add(ParameterKeys.PinPadName, pinPadName);
      requestParameters.Add(ParameterKeys.PinPadConnectionType, pinPadConnectionType);
      var response = ChipDnaMobile.Instance.SetProperties(requestParameters);

      if (response.ContainsKey(ParameterKeys.Result) && response.GetValue(ParameterKeys.Result).Equals(ParameterValues.False, StringComparison.CurrentCultureIgnoreCase))
      {
        var error = response.GetValue(ParameterKeys.Errors);
        return false;
      }

      if (response.ContainsKey(ParameterKeys.Result) && response.GetValue(ParameterKeys.Result).Equals(ParameterValues.True, StringComparison.CurrentCultureIgnoreCase))
      {
        var result = response.GetValue(ParameterKeys.Result);
        return true;
      }

      return false;
    }

    public bool ConnectPinPad()
    {
      // Use an instance of ChipDnaMobile to begin connectAndConfigure of the device.
      // PINpad checks are completed within connectAndConfigure, deciding whether a TMS update will need to be completed.

      var statusParameters = ChipDnaMobile.Instance.GetStatus(new Parameters());
      var deviceStatus = statusParameters.GetValue(ParameterKeys.DeviceStatus);
      var pinPadName = statusParameters.GetValue(ParameterKeys.PinPadName);
      var chipDnaStatus = statusParameters.GetValue(ParameterKeys.ChipDnaStatus);

      Parameters response = ChipDnaMobile.Instance.ConnectAndConfigure(statusParameters);

      if (response.ContainsKey(ParameterKeys.Result) && response.GetValue(ParameterKeys.Result).Equals(ParameterValues.False, StringComparison.CurrentCultureIgnoreCase))
      {
        var error = response.GetValue(ParameterKeys.Errors);
        return false;
      }

      if (response.ContainsKey(ParameterKeys.Result) && response.GetValue(ParameterKeys.Result).Equals(ParameterValues.True, StringComparison.CurrentCultureIgnoreCase))
      {
        var result = response.GetValue(ParameterKeys.Result);
        return true;
      }

      return false;
    }

    // amount in cents
    public void AuthorizeTransaction(string currency, string amount, string reference)
    {
      Parameters requestParameters = new Parameters();

      // The following parameters are essential for the completion of a transaction.
      // In the current example the parameters are initialised as constants. They will need to be dynamically collected and initialised.
      requestParameters.Add(ParameterKeys.Amount, amount);
      requestParameters.Add(ParameterKeys.AmountType, ParameterValues.AmountTypeActual);
      requestParameters.Add(ParameterKeys.Currency, currency);

      //requestParameters.Add(ParameterKeys.PANKeyEntry, ParameterValues.True);


      // The user reference is needed to be to able to access the transaction on WEBMis.
      // The reference should be unique to a transaction, so it is suggested that the reference is generated, similar to the example below.
      requestParameters.Add(ParameterKeys.UserReference, reference);
      requestParameters.Add(ParameterKeys.TransactionType, ParameterValues.Sale);

      requestParameters.Add(ParameterKeys.PaymentMethod, ParameterValues.Card);


      requestParameters.Add(ParameterKeys.TippingType, ParameterValues.OnDeviceTipping);

      Parameters response = ChipDnaMobile.Instance.StartTransaction(requestParameters);

      if (response.ContainsKey(ParameterKeys.Result) && response.GetValue(ParameterKeys.Result).Equals(ParameterValues.False, StringComparison.CurrentCultureIgnoreCase))
      {
        var error = response.GetValue(ParameterKeys.Errors);
      }
    }

    public void ConfirmTransaction(string amount, string reference)
    {
      Parameters requestParameters = new Parameters();

      // The following parameters are used to confirm an authorised transaction.
      // The user reference is used to reference the transaction stored on WEBMis.
      requestParameters.Add(ParameterKeys.UserReference, reference);
      requestParameters.Add(ParameterKeys.Amount, amount);
      requestParameters.Add(ParameterKeys.CloseTransaction, ParameterValues.True);

      var response = ChipDnaMobile.Instance.ConfirmTransaction(requestParameters);

      if (response.ContainsKey(ParameterKeys.Result) && response.GetValue(ParameterKeys.Result).Equals(ParameterValues.False, StringComparison.CurrentCultureIgnoreCase))
      {
        var error = response.GetValue(ParameterKeys.Errors);
      }
    }

    public void VoidTransaction(string reference)
    {
      Parameters requestParameters = new Parameters();

      // The following parameters are used to void an authorised transaction.
      // The user reference is used to reference the transaction stored on WEBMis.
      requestParameters.Add(ParameterKeys.UserReference, reference);

      var response = ChipDnaMobile.Instance.VoidTransaction(requestParameters);

      if (response.ContainsKey(ParameterKeys.Result) && response.GetValue(ParameterKeys.Result).Equals(ParameterValues.False, StringComparison.CurrentCultureIgnoreCase))
      {
        var error = response.GetValue(ParameterKeys.Errors);
      }
    }

    public void GetTransaction(string reference)
    {
      Parameters requestParameters = new Parameters();

      // The following parameters are used to display information about a transaction.
      // The user reference is used to reference the transaction stored on WEBMis.
      requestParameters.Add(ParameterKeys.UserReference, Guid.NewGuid().ToString());

      var response = ChipDnaMobile.Instance.GetTransactionInformation(requestParameters);

      if (response.ContainsKey(ParameterKeys.Result) && response.GetValue(ParameterKeys.Result).Equals(ParameterValues.False, StringComparison.CurrentCultureIgnoreCase))
      {
        var error = response.GetValue(ParameterKeys.Errors);
      }
    }

    public void TerminateTransaction()
    {
      Parameters response = ChipDnaMobile.Instance.TerminateTransaction(new Parameters());

      if (response.ContainsKey(ParameterKeys.Result) && response.GetValue(ParameterKeys.Result).Equals(ParameterValues.False, StringComparison.CurrentCultureIgnoreCase))
      {
        var error = response.GetValue(ParameterKeys.Errors);
      }
    }

    public void ForceTmsUpdate()
    {
      Parameters tmsParams = new Parameters();
      tmsParams.Add(ParameterKeys.ForceTmsUpdate, ParameterValues.True);
      tmsParams.Add(ParameterKeys.FullTmsUpdate, ParameterValues.False);
      ChipDnaMobile.Instance.RequestTmsUpdate(tmsParams);
    }

    private void RegisterListeners()
    {
      // Clear all listeners

      ChipDnaMobile.Instance.ClearAllTransactionFinishedListener();


      // Now register all listeners

      ChipDnaMobile.Instance.AddConnectAndConfigureFinishedListener(new ConnectAndConfigureFinishedListener());
      ChipDnaMobile.Instance.AddConfigurationUpdateListener(new ConfigurationUpdateListener());
      ChipDnaMobile.Instance.AddDeviceUpdateListener(new DeviceUpdateListener());
      ChipDnaMobile.Instance.AddTmsUpdateListener(new TmsUpdateListener());
      ChipDnaMobile.Instance.AddProcessReceiptFinishedListener(new ProcessReceiptListener());

      //ChipDnaMobile.Instance.AddCardDetailsListener(new CardDetailsListener());

      //ChipDnaMobile.Instance.AddTransactionUpdateListener(new TransactionUpdateListener());
      ChipDnaMobile.Instance.AddTransactionFinishedListener(new TransactionFinishedListener());
      //ChipDnaMobile.Instance.AddDeferredAuthorizationListener(new TransactionDeferredAuthListener());
      ChipDnaMobile.Instance.AddSignatureVerificationListener(new TransactionSignatureVerificationListener());
      ChipDnaMobile.Instance.AddVoiceReferralListener(new TransactionVoiceReferralListener());
      //ChipDnaMobile.Instance.AddPartialApprovalListener(new TransactionPartialApprovalListener());
      //ChipDnaMobile.Instance.AddForceAcceptanceListener(new TransactionForceAcceptanceListener());
      //ChipDnaMobile.Instance.AddVerifyIdListener(new TransactionVerifyIdListener());


    }
  }
}