using System;
using Com.Creditcall.Chipdnamobile;

namespace ChipDnaSample.Droid.Services.ChipDna
{
  public class ConnectAndConfigureFinishedListener : Java.Lang.Object, IConnectAndConfigureFinishedListener
  {
    public void OnConnectAndConfigureFinished(Parameters parameters)
    {
      if (parameters.ContainsKey(ParameterKeys.Result) && parameters.GetValue(ParameterKeys.Result).Equals(ParameterValues.True, StringComparison.CurrentCultureIgnoreCase))
      {
        var result = parameters.GetValue(ParameterKeys.Result);
      }
      else
      {
        var error = parameters.GetValue(ParameterKeys.Errors);
      }
    }
  }
}