/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cucumber.api.jelly.glue;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/**
 * A Collection of Cucumber Steps that perform common NBDialogOperator Actions.
 * 
 * 
 * @author nick
 */
public class NbDialogOperatorSteps {    
    
    /**
     * Jelly Tools Step: I can click the $dialog close button
     * 
     * Performs a click of the close button of the dialog.
     * 
     * @param dialog The Title of the Dialog.
     */
    @When("^I can click the \"([^\"]*)\" close button$")
    public void clickTheCloseButton(String dialog) {
        new NbDialogOperator(dialog).closeByButton();
    }

    /**
     * Used to do a visibility check of the dialog box.
     * 
     * If we expect it to be showing we wait in case it is still appearing.
     * If we expect it to be hidden we just look for it.
     * 
     * @param dialog The Title of the Dialog.
     * @param visible Indicate is we expect the dialog to be visible or hidden.
     */
    static final void dialogVisable(String dialog,boolean visible){       
        try {
            if (visible) {
                new NbDialogOperator(dialog).isShowing();
            }
        } catch (Exception e) {
            //NOP as we are looking for this.
            //Nothing to see here.
        }        
        assertThat("The '"+dialog+"' Dialog is"+(visible?"displayed":"hidden")
                  ,JDialogOperator.findJDialog(dialog, false, false)
                  ,(visible?is(notNullValue()):is(nullValue())));        
    }           
    
    /**
     * Jelly Tools Step: the $dialog Dialog is hidden
     * 
     * Checks to see if the dialog is hidden.
     * 
     * @param dialog The Title of the Dialog.
     */
    @Then("^the \"([^\"]*)\" Dialog is hidden$")
    public void theDialogIsHidden(String dialog) {
        dialogVisable(dialog,false);
    }    

    /**
     * Jelly Tools Step: the $dialog Dialog is displayed
     * 
     * Checks to see if the dialog is displayed.
     * 
     * @param dialog The Title of the Dialog.
     */
    @Then("^the \"([^\"]*)\" Dialog is displayed$")
    public void theDialogIsDisplayed(String dialog) {
        dialogVisable(dialog,true);
    }
/**/ 
}
