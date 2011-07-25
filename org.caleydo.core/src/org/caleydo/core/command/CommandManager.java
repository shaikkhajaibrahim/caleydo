package org.caleydo.core.command;

import org.caleydo.core.command.data.CmdDataCreateDimension;
import org.caleydo.core.command.data.CmdDataCreateTable;
import org.caleydo.core.command.data.parser.CmdLoadFileLookupTable;
import org.caleydo.core.command.data.parser.CmdLoadFileNStorages;
import org.caleydo.core.parser.parameter.ParameterHandler;

/**
 * Manager for creating commands.
 * 
 * @author Marc Streit
 */
public class CommandManager {

	/**
	 * Create a new command. Calls createCommandByType(CommandType) internal.
	 * 
	 * @param phAttributes
	 *            Define several attributes and assign them in new Command
	 * @return new Command with attributes defined in phAttributes
	 */
	public ICommand createCommand(final ParameterHandler phAttributes) {

		CommandType cmdType =
			CommandType.valueOf(phAttributes.getValueString(CommandType.TAG_TYPE.getXmlKey()));

		ICommand createdCommand = createCommandByType(cmdType);

		if (phAttributes != null) {
			createdCommand.setParameterHandler(phAttributes);
		}

		createdCommand.doCommand();

		return createdCommand;
	}

	/**
	 * Create a new command assigned to a cmdType.
	 * 
	 * @param cmdType
	 *            specify the ICommand to be created.
	 * @return new ICommand
	 */
	public ICommand createCommandByType(final CommandType cmdType) {
		ICommand createdCommand = null;

		switch (cmdType) {

			case CREATE_ID_CATEGORY: {
				createdCommand = new CmdCreateIDCategory();
				break;
			}
			case CREATE_ID_TYPE: {
				createdCommand = new CmdCreateIDType();
				break;
			}
			case LOAD_LOOKUP_TABLE_FILE: {
				createdCommand = new CmdLoadFileLookupTable();
				break;
			}
			case LOAD_DATA_FILE: {
				createdCommand = new CmdLoadFileNStorages();
				break;
			}
			case CREATE_DIMENSION: {
				createdCommand = new CmdDataCreateDimension();
				break;
			}
			case CREATE_DATA_TABLE: {
				createdCommand = new CmdDataCreateTable();
				break;
			}

			default:
				throw new IllegalStateException("Unsupported CommandQueue key= [" + cmdType + "]");
		}

		return createdCommand;
	}
}
