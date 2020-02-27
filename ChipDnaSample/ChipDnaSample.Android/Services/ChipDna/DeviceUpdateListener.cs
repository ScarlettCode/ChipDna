using Com.Creditcall.Chipdnamobile;

namespace ChipDnaSample.Droid.Services.ChipDna
{
  public class DeviceUpdateListener : Java.Lang.Object, IDeviceUpdateListener
  {
    public void OnDeviceUpdate(Parameters parameters)
    {
      var result = parameters.GetValue(ParameterKeys.DeviceStatusUpdate);
    }
  }
}