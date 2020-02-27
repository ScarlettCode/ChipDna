using System;
using System.Collections.Generic;
using Com.Creditcall.Chipdnamobile;
using Prism.Events;

namespace ChipDnaSample.Droid.Services.ChipDna
{
  public class OnAvailablePinPadsEventArgs : System.EventArgs
  {
    private readonly IDictionary<string, IList<string>> _pinPads;

    public OnAvailablePinPadsEventArgs(IDictionary<string, IList<string>> pinPads)
    {
      _pinPads = pinPads;
    }
  }

  public class AvailablePinPadsListener : Java.Lang.Object, IAvailablePinPadsListener
  {
    private readonly IEventAggregator _eventAggregator;
    
    public AvailablePinPadsListener(IEventAggregator eventAggregator)
    {
      _eventAggregator = eventAggregator;
    }

    public void OnAvailablePinPads(Parameters parameters)
    {
      if (parameters.ContainsKey(ParameterKeys.Errors))
      {
        var errors = parameters.GetValue(ParameterKeys.Errors);
      }

      if (parameters.ContainsKey(ParameterKeys.Result))
      {
        var result = parameters.GetValue(ParameterKeys.Result);
      }

      if (parameters.ContainsKey(ParameterKeys.AvailablePinPads))
      {


      }
    }
  }
}