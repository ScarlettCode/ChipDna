using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Android.App;
using Android.Content;
using Android.OS;
using Android.Runtime;
using Android.Views;
using Android.Widget;
using Com.Creditcall.Chipdnamobile;

namespace ChipDnaSample.Droid
{
  public class ChipDnaService
  {
    public static String AvailablePinPads = "AVAILABLE_PINPADS";

    public IDictionary<string, IList<string>> GetAvailablePinPads()
    {
      var pinPads = ChipDnaMobileSerializer.DeserializeAvailablePinPads(AvailablePinPads);

      return pinPads;
    }
  }
}