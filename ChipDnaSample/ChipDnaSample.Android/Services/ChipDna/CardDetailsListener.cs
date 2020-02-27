using Com.Creditcall.Chipdnamobile;

namespace ChipDnaSample.Droid.Services.ChipDna
{
  public class CardDetailsListener : Java.Lang.Object, ICardDetailsListener
  {
    public void OnCardDetails(Parameters parameters)
    {
      var xmlData = parameters.GetValue(ParameterKeys.Result);
    }
  }
}