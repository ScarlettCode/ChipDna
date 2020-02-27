using Prism.Commands;
using Prism.Mvvm;
using Prism.Navigation;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;

namespace ChipDnaSample.ViewModels
{
  public class MainPageViewModel : ViewModelBase
  {
    private readonly IChipDNAService _chipDnaService;
    private DelegateCommand TMSUpdateCommand;

    public MainPageViewModel(INavigationService navigationService, IChipDNAService chipDnaService)
        : base(navigationService)
    {
      _chipDnaService = chipDnaService;
      Title = "Main Page";

      PayCommand = new DelegateCommand(PayAction);
      ConnectCommand = new DelegateCommand(ConnectAction);
      TMSUpdateCommand = new DelegateCommand(TMSUpdateAction);
    }

    private void TMSUpdateAction()
    {
      _chipDnaService.ForceTmsUpdate();
    }

    private void ConnectAction()
    {
      _chipDnaService.Initialize("99966000", "6csMXHYbWTwVQ2vf", "1234");

      _chipDnaService.SetSelectedPinPad("Miura 330", "BLUETOOTH_CONNECTION_TYPE");
      _chipDnaService.ConnectPinPad();
    }

    public DelegateCommand ConnectCommand { get; set; }

    public DelegateCommand PayCommand { get; set; }

    private void PayAction()
    {
      _chipDnaService.AuthorizeTransaction("GBP", "1000", $"{Guid.NewGuid()}");

    }
  }
}
