using Com.Creditcall.Chipdnamobile;

namespace ChipDnaSample.Droid.Services.ChipDna
{
  public class TransactionFinishedListener : Java.Lang.Object, ITransactionFinishedListener
  {
    public void OnTransactionFinishedListener(Parameters parameters)
    {
      var xmlData = parameters.GetValue(ParameterKeys.TransactionResult);
    }
  }

  public class TransactionUpdateListener : Java.Lang.Object, ITransactionUpdateListener
  {
    public void OnTransactionUpdateListener(Parameters parameters)
    {
      var xmlData = parameters.GetValue(ParameterKeys.TransactionUpdate);
    }

  }

  public class TransactionDeferredAuthListener : Java.Lang.Object, IDeferredAuthorizationListener
  {
    public void OnDeferredAuthorizationListener(Parameters parameters)
    {
      var xmlData = parameters.GetValue(ParameterKeys.TransactionResult);
      var xmlDefData = parameters.GetValue(ParameterKeys.DeferredAuthorizationReason);
    }

  }

  public class TransactionSignatureVerificationListener : Java.Lang.Object, ISignatureVerificationListener
  {
    public void OnSignatureVerification(Parameters parameters)
    {
      var xmlData = parameters.GetValue(ParameterKeys.TransactionResult);
      var xmlSigData = parameters.GetValue(ParameterKeys.SignatureData);
    }
  }

  public class TransactionVoiceReferralListener : Java.Lang.Object, IVoiceReferralListener
  {
    public void OnVoiceReferral(Parameters parameters)
    {
      var xmlData = parameters.GetValue(ParameterKeys.TransactionResult);
    }
  }

  public class TransactionPartialApprovalListener : Java.Lang.Object, IPartialApprovalListener
  {
    public void OnPartialApproval(Parameters parameters)
    {
      var xmlData = parameters.GetValue(ParameterKeys.TransactionResult);
    }
  }

  public class TransactionForceAcceptanceListener : Java.Lang.Object, IForceAcceptanceListener
  {
    public void OnForceAcceptance(Parameters parameters)
    {
      var xmlData = parameters.GetValue(ParameterKeys.TransactionResult);
    }
  }

  public class TransactionVerifyIdListener : Java.Lang.Object, IVerifyIdListener
  {
    public void OnVerifyId(Parameters parameters)
    {
      var xmlData = parameters.GetValue(ParameterKeys.TransactionResult);
    }
  }
}