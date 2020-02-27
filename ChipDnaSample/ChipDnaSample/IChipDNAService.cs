using System;
using System.Collections.Generic;

namespace ChipDnaSample
{
  public class MiuraDevice
  {
    public string ConnectionType { get; set; }
    public string DeviceName { get; set; }
  }

  public interface IChipDNAService
  {
    void Dispose();
    bool IsInitialize();
    void Initialize(string terminalId, string terminalKey, string password);
    void GetAvailablePinPads();
    bool SetSelectedPinPad(string pinPadName, string pinPadConnectionType);
    bool ConnectPinPad();
    void AuthorizeTransaction(string currency, string amount, string reference);
    void ConfirmTransaction(string amount, string reference);
    void VoidTransaction(string reference);
    void GetTransaction(string reference);
    void TerminateTransaction();
    void ForceTmsUpdate();
    MiuraDevice GetCurrentPinPad();
  }
}
