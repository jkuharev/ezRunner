/** ezRunner, ezRunner, 04.09.2018*/
package ezRunner;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JPanel;

import de.mz.jk.jsix.ui.TextWindowDragAndDropUI;
import de.mz.jk.jsix.ui.TextWindowDragAndDropUI.FileActionListener;
import de.mz.jk.jsix.utilities.Settings;

/**
 * <h3>{@link ezRunner}</h3>
 * @author Dr. Joerg Kuharev
 * @version 04.09.2018 07:38:21
 */
public class ezRunner implements ActionListener, FileActionListener
{
	private static String endl = "\n";
	private static String msg = ""
			+ "= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = " + endl
			+ "Let's wrap your binaries with 'ezRunner'" + endl
			+ "- set params in the config file" + endl
			+ "- drag and drop files" + endl
			+ "- click RUN" + endl
			+ "= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = " + endl
			+ "'THE BEER-WARE LICENSE':" + endl
			+ "As long as you retain this notice you can do" + endl
			+ "whatever you want with this application." + endl
			+ "If we meet some day & you think that ezRunner" + endl
			+ "is worth it, you can buy me a beer in return." + endl
			+ "(c) Joerg Kuharev, 2018" + endl
			+ "= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = " + endl;

	private TextWindowDragAndDropUI ui = null;
	private JButton btnReset = new JButton( "RESET" );
	private JButton btnPreview = new JButton( "PREVIEW" );
	private JButton btnRun = new JButton( "RUN" );
	private Settings cfg = new Settings( "ezrunner.ini", "Settings for ezRunner by Joerg Kuharev" );

	private Set<File> dndFiles = null;
	private String executable = "echo";
	private String params = "";
	// filter dragged files by a file filter, regular expression or substring
	private String dndFileRule = "*.*";
	String paramFileKey = "file";
	String[] paramKeys = null;
	private Map<String, String> paramValues = null;

	private boolean runOnDropEvent = false;
	private boolean resetFilesAfterExecution = false;

	public static void main(String[] args)
	{
		ezRunner r = new ezRunner();
	}

	public ezRunner()
	{
		initUI();
		reset( true );
	}

	/**
	 * 
	 */
	private void initUI()
	{
		ui = new TextWindowDragAndDropUI( "ezRunner", 480, 480, msg );
		Window win = ui.getWin();
		win.add( getToolBar( new JButton[] { btnReset, btnPreview, btnRun } ), BorderLayout.SOUTH );
		win.setVisible( true );
		ui.addFileActionListener( this );
	}

	private JPanel getToolBar(JButton[] btns)
	{
		JPanel toolBar = new JPanel( new FlowLayout( FlowLayout.CENTER ) );
		for ( JButton btn : btns )
		{
			btn.setActionCommand( btn.getText() );
			btn.addActionListener( this );
			toolBar.add( btn );
		}
		return toolBar;
	}

	@Override public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();
		switch (cmd)
		{
			case "RESET":
				reset( true );
				break;
			case "PREVIEW":
				preview();
				break;
			case "RUN":
				run();
				break;
			default:
				System.out.println( "unknown command: " + cmd );
				break;
		}
	}

	private void initConfig()
	{
		paramValues = new HashMap<String, String>();
		executable = cfg.getStringValue( "executableCommand", "echo", false );
		paramKeys = cfg.getArray( "executableParameterKeys", "file,user".split( "," ), false );
		paramFileKey = cfg.getStringValue( "fileParameterKey", "file", false );
		params = cfg.getStringValue( "executableParameters", "Hello $user! Thank you for dropping file: $file", false );
		dndFileRule = cfg.getStringValue( "fileAcceptanceFilter", ".*", false );
		
		runOnDropEvent = cfg.getBooleanValue( "application.autoRunOnDropEvent", false, false );
		resetFilesAfterExecution = cfg.getBooleanValue( "application.resetFilesAfterExecution", true, false );
		
		for(String key : paramKeys)
		{
			paramValues.put( key, cfg.getStringValue( key, "$" + key, false ) );
		}
	}

	/**
	 * @return
	 */
	private String parseParams()
	{
		String pars = params;
		for ( String key : paramKeys )
		{
			String value = paramValues.getOrDefault( key, key );
			pars = pars.replace( "$" + key, value );
		}
		return pars;
	}

	/** run command line for every file in the list */
	private void run()
	{
		try
		{
			if (dndFiles.size() < 1)
			{
				executeCommandLine();
			}
			else
			{
				for ( File f : dndFiles )
				{
					paramValues.put( paramFileKey, f.getAbsolutePath() );
					executeCommandLine();
				}
				// reset files
				if (resetFilesAfterExecution || runOnDropEvent)
					reset( false );
			}
		}
		catch (Exception e)
		{
			System.err.println( "Something went wrong. Execution aborted." );
			e.printStackTrace();
		}
	}

	private void executeCommandLine() throws Exception
	{
		String args = parseParams();
		String cmd = executable + " " + args;
		System.out.println( "executing command line:\n\t" + cmd );
		executeCommand( cmd );
	}

	/** list files and show the preparsed command line */
	private void preview()
	{
		System.out.println( "Queue: [" + dndFileRule + "]" );
		if (dndFiles != null && dndFiles.size() > 0)
		{
			for ( File f : dndFiles )
			{
				System.out.println( "	" + f.getAbsolutePath() );
			}
		}
		else
		{
			System.out.println( "	EMPTY! Please drag and drop files matching the rule: " + dndFileRule );
		}

		String p = parseParams();
		System.out.println( "Command:\n\t" + executable );
		System.out.println( "Arguments:\n\t" + p );
	}

	/** reload config and empty file list */
	private void reset(boolean clearTexts)
	{
		dndFiles = new TreeSet<>();
		initConfig();
		if (clearTexts)
		{
			ui.getOutputTextArea().setText( "" );
			ui.printWelcomeMessage();
			ui.getOutputTextArea().updateUI();
		}
	}

	@Override public List<File> filterTargetFiles(List<File> files)
	{
		List<File> acceptedFiles = new ArrayList<File>();
		for ( File f : files )
		{
			String n = f.getName();
			if (n.contains( dndFileRule ) || n.matches( dndFileRule ))
			{
				acceptedFiles.add( f );
				System.out.println( "accepting file: " + n );
			}
			else
			{
				System.out.println( "ignoring file: " + n );
			}
		}
		return acceptedFiles;
	}

	@Override public void doMultiFileAction(List<File> files)
	{
		dndFiles.addAll( files );
		if (runOnDropEvent && dndFiles.size() > 0)
		{
			run();
		}
	}

	/** don't touch as we already handle everything in the other method */
	@Override public void doSingleFileAction(File file){}

	private int executeCommand(String command)
	{
		StringBuffer output = new StringBuffer();
		Process p;
		try
		{
			p = Runtime.getRuntime().exec( command );
			redirectStream( p.getInputStream(), false );
			redirectStream( p.getErrorStream(), true );
			p.waitFor();
			return p.exitValue();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return 1;
	}

	private void redirectStream(InputStream in, final boolean stdErr)
	{
		final BufferedReader stdInput = new BufferedReader( new InputStreamReader( in ) );
		new Thread()
		{
			@Override public void run()
			{
				String s = null;
				try
				{
					while (( s = stdInput.readLine() ) != null)
					{
						if (stdErr)
							System.err.println( s );
						else
							System.out.println( s );
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}.start();
	}
}
