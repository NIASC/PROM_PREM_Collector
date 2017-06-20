package core;

import core.containers.MessageContainer;
import core.containers.Option;
import core.containers.OptionContainer;
import implement.*;

public class PROM_PREM_Collector
{
	private UserHandle userHandle;
	private Questionaire questionaire;
	private ViewData viewData;
	private final int ERROR = -1;
	private final int LOGOUT = 1;
	private final int START_QUESTIONAIRE = 2;
	private final int VIEW_DATA = 4;
	
	private UserInterface ui;

	public PROM_PREM_Collector()
	{
		ui = new UserInterface();
		userHandle = new UserHandle(ui);
		questionaire = new Questionaire(ui, userHandle);
		viewData = new ViewData(ui, userHandle);
	}

	public void start()
	{
		Messages.loadMessages();
		while(!userHandle.isLoggedIn())
		{
			int response = ui.displayLoginScreen();
			switch (response)
			{
			case UserInterface_Interface.EXIT:
				System.exit(0);
			case UserInterface_Interface.LOGIN:
				userHandle.login();
				break;
			case UserInterface_Interface.REGISTER:
				userHandle.register();
				break;
			default:
				ui.displayError(Messages.errorMessages.getMessage(
						"UNKNOWN_RESPONSE", "en"));
				break;
			}
		}

		OptionContainer options = new OptionContainer();
		options.fill(
				new Option[] {
						new Option(START_QUESTIONAIRE, "Start questionaire."),
						new Option(VIEW_DATA, "View statistics for this clinic."),
						new Option(LOGOUT, "Log out.")
				});
		boolean running = true;
		while (running)
		{
			options.setSelected(ui.selectOption(options));
			Option selected = options.getSelected();
			int response = ERROR;
			if (selected != null)
				response = selected.getIdentifier();
			switch (response)
			{
			case ERROR:
				ui.displayError(Messages.errorMessages.getMessage(
						"NULL_SELECTED", "en"));
				break;
			case LOGOUT:
				userHandle.logout();
				running = false;
				break;
			case START_QUESTIONAIRE:
				questionaire.start();
				break;
			case VIEW_DATA:
				viewData.start();
				break;
			default:
				ui.displayError(Messages.errorMessages.getMessage(
						"UNKNOWN_RESPONSE", "en"));
				break;
			}
		}
		ui.close();
	}
}
