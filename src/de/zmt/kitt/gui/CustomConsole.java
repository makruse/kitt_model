package de.zmt.kitt.gui;



import java.awt.FileDialog;
import java.io.File;
import java.io.FilenameFilter;

import de.zmt.kitt.sim.Sim;

import sim.display.Console;
import sim.display.GUIState;
import sim.util.gui.Utilities;

public class CustomConsole extends Console {

	private Sim sim;
	private Gui gui;
	
	private static final long serialVersionUID = 1L;

	public CustomConsole(Gui gui) {
		super(gui);
		
		this.sim = gui.sim;
		this.gui = gui;
		
	}
	
	
    /** Lets the user save the current modelparams under a specific filename. */
    public void doSaveAs(){
        
    	String path="";
        FileDialog fd = new FileDialog(this, "Save Configuration File...", FileDialog.SAVE);
        fd.setFilenameFilter(new FilenameFilter(){
            public boolean accept(File dir, String name){
                return Utilities.ensureFileEndsWith(name, ".xml").equals(name);
            }
        });

        File file = new File(sim.params.currentPath);
        fd.setDirectory(file.getParentFile().getPath());
    	
        fd.setVisible(true);
        File f = null; 
        if (fd.getFile() != null){
            try{
                f = new File(fd.getDirectory(), fd.getFile());
                if( f.exists()==false){
	                boolean  b=f.createNewFile();
	                if( b == false)
	                	throw new Exception();
                }
                path =fd.getDirectory()+ fd.getFile();
                gui.save(path);
                                             
            }        
            catch (Throwable e) {
                Utilities.informOfError(e, 
                    "An error occurred while saving the configuration to the file " + 
                    (f == null ? fd.getFile(): f.getName()), null);
            }
        }
    }

    /** Reverts the current configuration to the configuration stored under filename. */
    public void doOpen(){
    	
        FileDialog fd = new FileDialog(this, "Load Configuration File...", FileDialog.LOAD);
        fd.setFilenameFilter(new FilenameFilter(){
            public boolean accept(File dir, String name){
                return Utilities.ensureFileEndsWith(name, ".xml").equals(name);
            }
        });

        File file = new File(sim.params.currentPath);
        fd.setDirectory(file.getParentFile().getPath());
                
        boolean failed = true;
        int originalPlayState = getPlayState();
        if (originalPlayState == PS_PLAYING) // need to put into paused mode
            pressPause();
                
        fd.setVisible(true);
        File f = null; 
        
        String path=null;
        
        if (fd.getFile() != null){
            try{
                f = new File(fd.getDirectory(), fd.getFile());

                path= fd.getDirectory()+ fd.getFile();
                
                gui.reload(path);
                
                failed = false;
            }
            catch (Throwable e) {
                Utilities.informOfError(e, 
                    "An error occurred while loading the simulation from the file " + 
                    (f == null ? fd.getFile(): f.getName()), null);
                return;
            }
                
	        // if we failed, reset play state.  If we were stopped, do nothing (we're still stopped).
	        if (failed && originalPlayState == PS_PLAYING)
	            pressPause();  
        }
                
    }
}
