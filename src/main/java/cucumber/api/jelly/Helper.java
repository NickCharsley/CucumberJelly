/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cucumber.api.jelly;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/**
 * A Collection of Cucumber Steps that perform common NBDialogOperator Actions.
 * 
 * 
 * @author nick
 */
public class Helper {        
    /**
     * Used to do a visibility check of the dialog box.
     * 
     * If we expect it to be showing we wait in case it is still appearing.
     * If we expect it to be hidden we just look for it.
     * 
     * @param dialog The Title of the Dialog.
     * @param visible Indicate is we expect the dialog to be visible or hidden.
     */
    public static final boolean dialogVisable(String dialog,boolean visible){       
        try {
            if (visible) {
                new NbDialogOperator(dialog).isShowing();
            }
        } catch (Exception e) {
            //NOP as we are looking for this.
            //Nothing to see here.        
        }        
        return JDialogOperator.findJDialog(dialog, false, false)!=null;
    }           
}
