using Com.Creditcall.Chipdnamobile;
using Prism.Events;

namespace ChipDnaSample.Droid.Services.ChipDna
{
  public class ProcessReceiptListener : Java.Lang.Object, IProcessReceiptFinishedListener
  {
    public ProcessReceiptListener()
    {

    }

    public void OnProcessReceiptFinishedListener(Parameters parameters)
    {
      if (parameters.ContainsKey(ParameterKeys.Errors))
      {
        var errors = parameters.GetValue(ParameterKeys.Errors);
      }

      if (parameters.ContainsKey(ParameterKeys.Result))
      {
        var result = parameters.GetValue(ParameterKeys.Result);
      }

      //if (parameters.ContainsKey(ParameterKeys.AvailablePinPads))
      //{
      //  var receiptDataXml = parameters.GetValue(ParameterKeys.ReceiptData);

      //  var receiptData = ChipDnaMobileSerializer.DeserializeReceiptData(receiptDataXml);

      //  var chip = _eventAggregator.GetEvent<KuulEatsMobile.Events.GetOnProcessReceiptFinishedListener>();
      //  chip.Publish(receiptData);
      //}
    }
  }
}