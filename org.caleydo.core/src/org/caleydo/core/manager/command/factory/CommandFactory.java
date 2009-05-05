package org.caleydo.core.manager.command.factory;

import org.caleydo.core.command.ECommandType;
import org.caleydo.core.command.ICommand;
import org.caleydo.core.command.data.CmdDataCreateSet;
import org.caleydo.core.command.data.CmdDataCreateStorage;
import org.caleydo.core.command.data.CmdDataCreateVirtualArray;
import org.caleydo.core.command.data.CmdSetDataRepresentation;
import org.caleydo.core.command.data.filter.CmdDataFilterMinMax;
import org.caleydo.core.command.data.parser.CmdLoadFileLookupTable;
import org.caleydo.core.command.data.parser.CmdLoadFileNStorages;
import org.caleydo.core.command.system.CmdFetchPathwayData;
import org.caleydo.core.command.system.CmdLoadGlyphDefinition;
import org.caleydo.core.command.system.CmdLoadPathwayData;
import org.caleydo.core.command.system.CmdSystemExit;
import org.caleydo.core.command.system.path.CmdSetPathwayDatabasePath;
import org.caleydo.core.command.view.opengl.CmdCreateGLEventListener;
import org.caleydo.core.command.view.opengl.CmdCreateGLPathway;
import org.caleydo.core.command.view.rcp.CmdExternalActionTrigger;
import org.caleydo.core.command.view.rcp.CmdExternalFlagSetter;
import org.caleydo.core.command.view.rcp.CmdExternalObjectSetter;
import org.caleydo.core.command.view.rcp.CmdViewCreateRcpGLCanvas;
import org.caleydo.core.command.view.swt.CmdViewCreateDataEntitySearcher;
import org.caleydo.core.command.view.swt.CmdViewCreateGlyphConfiguration;
import org.caleydo.core.command.view.swt.CmdViewCreateHTMLBrowser;
import org.caleydo.core.command.view.swt.CmdViewCreateImage;
import org.caleydo.core.command.view.swt.CmdViewCreateMixer;
import org.caleydo.core.command.view.swt.CmdViewCreatePathway;
import org.caleydo.core.command.view.swt.CmdViewCreateSwtGLCanvas;
import org.caleydo.core.command.view.swt.CmdViewCreateUndoRedo;
import org.caleydo.core.command.window.swt.CmdContainerCreate;
import org.caleydo.core.command.window.swt.CmdWindowCreate;
import org.caleydo.core.manager.ICommandManager;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.general.GeneralManager;

/**
 * Class is responsible for creating the commands. The commands are created according to the command type.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 */
public class CommandFactory
	implements ICommandFactory {
	private ICommand lastCommand;

	protected final IGeneralManager generalManager;

	protected final ICommandManager commandManager;

	/**
	 * Constructor.
	 */
	public CommandFactory() {
		this.generalManager = GeneralManager.get();
		this.commandManager = generalManager.getCommandManager();
	}

	@Override
	public ICommand createCommandByType(final ECommandType cmdType) {
		ICommand createdCommand = null;

		switch (cmdType) {
			case LOAD_LOOKUP_TABLE_FILE: {
				createdCommand = new CmdLoadFileLookupTable(cmdType);
				break;
			}
			case LOAD_DATA_FILE: {
				createdCommand = new CmdLoadFileNStorages(cmdType);
				break;
			}
			case CREATE_STORAGE: {
				createdCommand = new CmdDataCreateStorage(cmdType);
				break;
			}
			case CREATE_VIRTUAL_ARRAY: {
				createdCommand = new CmdDataCreateVirtualArray(cmdType);
				break;
			}
			case CREATE_SET_DATA: {
				createdCommand = new CmdDataCreateSet(cmdType);
				break;
			}
			case CREATE_SWT_WINDOW: {
				createdCommand = new CmdWindowCreate(cmdType);
				break;
			}
			case CREATE_SWT_CONTAINER: {
				createdCommand = new CmdContainerCreate(cmdType);
				break;
			}
			case CREATE_VIEW_SWT_GLCANVAS: {
				createdCommand = new CmdViewCreateSwtGLCanvas(cmdType);
				break;
			}
			case CREATE_VIEW_RCP_GLCANVAS: {
				createdCommand = new CmdViewCreateRcpGLCanvas(cmdType);
				break;
			}
			case CREATE_VIEW_PATHWAY: {
				createdCommand = new CmdViewCreatePathway(cmdType);
				break;
			}
			case CREATE_VIEW_MIXER: {
				createdCommand = new CmdViewCreateMixer(cmdType);
				break;
			}
			case CREATE_VIEW_GLYPHCONFIG: {
				createdCommand = new CmdViewCreateGlyphConfiguration(cmdType);
				break;
			}
			case CREATE_VIEW_BROWSER: {
				createdCommand = new CmdViewCreateHTMLBrowser(cmdType);
				break;
			}
			case CREATE_VIEW_IMAGE: {
				createdCommand = new CmdViewCreateImage(cmdType);
				break;
			}
			case CREATE_VIEW_UNDO_REDO: {
				createdCommand = new CmdViewCreateUndoRedo(cmdType);
				break;
			}
			case CREATE_VIEW_DATA_ENTITY_SEARCHER: {
				createdCommand = new CmdViewCreateDataEntitySearcher(cmdType);
				break;
			}
			case CREATE_GL_PATHWAY_3D: {
				createdCommand = new CmdCreateGLPathway(cmdType);
				break;
			}
				// the next entries are all CmdCreateGLEventListener, so we do
				// it only once
			case CREATE_GL_HEAT_MAP_3D:
			case CREATE_GL_PROPAGATION_HEAT_MAP_3D:
			case CREATE_GL_TEXTURE_HEAT_MAP_3D:
			case CREATE_GL_GLYPH:
			case CREATE_GL_GLYPH_SLIDER:
			case CREATE_GL_PARALLEL_COORDINATES:
			case CREATE_GL_BUCKET_3D:
			case CREATE_GL_JUKEBOX_3D:
			case CREATE_GL_CELL:
			case CREATE_GL_REMOTE_GLYPH:
			case CREATE_GL_RADIAL_HIERARCHY:
			case CREATE_GL_HYPERBOLIC:
			case CREATE_GL_HISTOGRAM:
			case CREATE_GL_DENDROGRAM_VERTICAL:
			case CREATE_GL_DENDROGRAM_HORIZONTAL: {
				createdCommand = new CmdCreateGLEventListener(cmdType);
				break;
			}
			case EXTERNAL_OBJECT_SETTER: {
				createdCommand = new CmdExternalObjectSetter(cmdType);
				break;
			}
			case EXTERNAL_FLAG_SETTER: {
				createdCommand = new CmdExternalFlagSetter(cmdType);
				break;
			}
			case EXTERNAL_ACTION_TRIGGER: {
				createdCommand = new CmdExternalActionTrigger(cmdType);
				break;
			}
			case SYSTEM_SHUT_DOWN: {
				createdCommand = new CmdSystemExit(cmdType);
				break;
			}
			case SET_SYSTEM_PATH_PATHWAYS: {
				createdCommand = new CmdSetPathwayDatabasePath(cmdType);
				break;
			}
			case LOAD_GLYPH_DEFINITIONS: {
				createdCommand = new CmdLoadGlyphDefinition(cmdType);
				break;
			}
			case LOAD_PATHWAY_DATA: {
				createdCommand = new CmdLoadPathwayData(cmdType);
				break;
			}
			case FETCH_PATHWAY_DATA: {
				createdCommand = new CmdFetchPathwayData(cmdType);
				break;
			}
			case SET_DATA_REPRESENTATION: {
				createdCommand = new CmdSetDataRepresentation(cmdType);
				break;
			}
			case DATA_FILTER_MIN_MAX: {
				createdCommand = new CmdDataFilterMinMax(cmdType);
				break;
			}
			default:
				throw new IllegalStateException("Unsupported CommandQueue key= [" + cmdType + "]");
		} // end switch

		return createdCommand;
	}

	// @Override
	// public ICommand createCommandQueue(final String sCmdType, final String
	// sProcessType,
	// final int iCmdId, final int iCmdQueueId, final int sQueueThread,
	// final int sQueueThreadWait)
	// {
	//
	// ECommandType queueType;
	//
	// /**
	// * Create a new uniqueId if necessary
	// */
	// int iNewUniqueId = iCmdId;
	// if (iCmdId < 0)
	// {
	// // TODO: review when implementing ID management
	// iNewUniqueId = -1; // commandManager.createId(null);
	// }
	// /**
	// * End: Create a new uniqueId if necessary
	// */
	//
	// // try
	// // {
	// queueType = ECommandType.valueOf(sCmdType);
	// // }
	// // catch (IllegalArgumentException iae)
	// // {
	// // throw new CaleydoRuntimeException(
	// // "Undefined CommandQueue key= [" + sCmdType + "]",
	// // CaleydoRuntimeExceptionType.SAXPARSER);
	// // }
	//
	// switch (queueType)
	// {
	// case COMMAND_QUEUE_OPEN:
	// {
	// ICommand cmdQueue = new CommandQueueVector(queueType, iCmdQueueId);
	// return cmdQueue;
	// }
	//
	// case COMMAND_QUEUE_RUN:
	// return new CmdSystemRunCmdQueue(queueType, iCmdQueueId);
	//
	// default:
	// throw new IllegalStateException("Unsupported CommandQueue key= [" +
	// sCmdType
	// + "]");
	// }
	//
	// }

	/**
	 * Since the last created command is stored its reference is returned. Note: be carefully with this
	 * method, because maybe the command was already executed or destroyed, or a new command was created
	 * meanwhile
	 * 
	 * @return reference to last created command
	 */
	protected ICommand getLastCreatedCommand() {
		return lastCommand;
	}
}
