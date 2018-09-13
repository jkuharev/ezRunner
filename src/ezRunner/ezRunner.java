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

import de.mz.jk.jsix.libs.XFiles;
import de.mz.jk.jsix.libs.XJava;
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

	Set<String> paramKeys = new TreeSet<String>( Arrays.asList( new String[] { "user" } ) );
	String[] builtinKeys = "file,date,time,timestamp".split( "," );

	String formatDate = "yyyyMMdd";
	String formatTime = "HHmmss";
	String formatTimeStamp = "yyyyMMdd-HHmmss";

	private Map<String, String> paramValues = null;

	private boolean runOnDropEvent = false;
	private boolean resetFilesAfterExecution = false;
	private boolean executeCommand = true;
	private boolean createBatchFile = true;
	private String batchFileNamePattern = "$timestamp_ezRunner.bat";

	public static void main(String[] args)
	{
		new ezRunner();
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
		executable = cfg.getStringValue( "exec.command", "echo", false );
		String[] userKeys = cfg.getStringValue( "exec.params.userKeys", "user", false ).split( "," );
		paramKeys.addAll( Arrays.asList( userKeys ) );

		// show internal keys to the user
		cfg.setValue( "exec.params.internalKeys", XJava.joinArray( builtinKeys, "," ) );
		params = cfg.getStringValue( "exec.params", "Hello $user! Thank you for dropping file: $file", false );
		dndFileRule = cfg.getStringValue( "app.droppableFileFilter", ".*", false );
		
		runOnDropEvent = cfg.getBooleanValue( "app.autoRunOnDropEvent", false, false );
		resetFilesAfterExecution = cfg.getBooleanValue( "app.autoResetFileQueue", true, false );
		
		executeCommand = cfg.getBooleanValue( "app.onRunEvent.executeCommand", true, false );
		createBatchFile = cfg.getBooleanValue( "app.onRunEvent.createBatchFile", false, false );
		batchFileNamePattern = cfg.getStringValue( "app.batchFile.namePattern", "./$timestamp_ezRunner.bat", false );
		
		formatDate = cfg.getStringValue( "exec.params.format.date", formatDate, false );
		formatTime = cfg.getStringValue( "exec.params.format.time", formatTime, false );
		formatTimeStamp = cfg.getStringValue( "exec.params.format.timeStamp", formatTimeStamp, false );

		for(String key : paramKeys)
		{
			paramValues.put( key, cfg.getStringValue( key, "$" + key, false ) );
		}

		for ( String key : builtinKeys )
		{
			paramKeys.add( key );
			paramValues.put( key, "$" + key );
		}
	}

	/**
	 * @return
	 */
	private String parseParams(String paramTemplate)
	{
		paramValues.put( "time", XJava.timeStamp( formatTime ) );
		paramValues.put( "date", XJava.timeStamp( formatDate ) );
		paramValues.put( "timestamp", XJava.timeStamp( formatTimeStamp ) );

		String pars = paramTemplate;
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
		File batchFile = null;
		try
		{
			if (createBatchFile)
			{
				String fileName = parseParams( this.batchFileNamePattern );
				batchFile = new File( fileName );
				XFiles.writeFile( batchFile, "\n" );
				System.out.println( "writing command line commands to batch file ... " );
				System.out.println( "	" + batchFile.getAbsolutePath() );
			}

			if (dndFiles.size() < 1)
			{
				String cmd = getCommandLine();
				if (createBatchFile && batchFile.canWrite()) XFiles.writeFile( batchFile, cmd + "\n" );
				executeCommand( cmd );
			}
			else
			{
				String cmdLines = "";
				for ( File f : dndFiles )
				{
					paramValues.put( "file", f.getAbsolutePath() );
					String cmd = getCommandLine();
					cmdLines += cmd + "\n";
					executeCommand( cmd );
				}
				if (createBatchFile && batchFile.canWrite()) XFiles.writeFile( batchFile, cmdLines );

				// reset files
				if (resetFilesAfterExecution || runOnDropEvent)
				{
					reset( false );
				}
				else
				{
					System.out.println( "DO NOT FORGET TO CLEAR THE QUEUE!" );
				}
			}
		}
		catch (Exception e)
		{
			System.err.println( "Something went wrong. Execution aborted." );
			e.printStackTrace();
		}
	}

	private String getCommandLine() throws Exception
	{
		String args = parseParams( params );
		String cmd = executable + " " + args;
		return cmd;
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
		String p = parseParams( params );

		System.out.println( "Keys:" );
		for ( String k : paramKeys )
			System.out.println( "	" + k + ": " + paramValues.getOrDefault( k, k ) );


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

	private int executeCommand(String cmd)
	{
		System.out.println( "command line:\n\t" + cmd );
		if (executeCommand)
		{
			System.out.println( "executing command ... " );
			StringBuffer output = new StringBuffer();
			Process p;
			try
			{
				p = Runtime.getRuntime().exec( cmd );
				redirectStream( p.getInputStream(), false );
				redirectStream( p.getErrorStream(), true );
				p.waitFor();
				return p.exitValue();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println( "Execution of the command has been switched off by user settings!" );
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
