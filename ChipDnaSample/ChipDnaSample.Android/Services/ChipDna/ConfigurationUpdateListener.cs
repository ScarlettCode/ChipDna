using Com.Creditcall.Chipdnamobile;

namespace ChipDnaSample.Droid.Services.ChipDna
{
  public class ConfigurationUpdateListener : Java.Lang.Object, IConfigurationUpdateListener
  {
    public void OnConfigurationUpdateListener(Parameters parameters)
    {
      var result = parameters.GetValue(ParameterKeys.ConfigurationUpdate);
    }
  }
}