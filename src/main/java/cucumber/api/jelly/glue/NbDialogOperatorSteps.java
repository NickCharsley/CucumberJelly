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
 *
 * @author nick
 */
public class NbDialogOperatorSteps {    
/**/    
    @When("^I can click the \"([^\"]*)\" close button$")
    public void clickTheCloseButton(String dialog) {
        new NbDialogOperator(dialog).closeByButton();
    }

    static final void dialogVisable(String dialog,boolean visable){       
        try {
            if (visable) {
                new NbDialogOperator(dialog).isShowing();
            }
        } catch (Exception e) {
            //NOP as we are looking for this.
            //Nothing to see here.
        }
        
        assertThat("The '"+dialog+"' Dialog is"+(visable?"displayed":"hidden")
                  ,JDialogOperator.findJDialog(dialog, false, false)
                  ,(visable?is(notNullValue()):is(nullValue())));
        
    }           
    
    @Then("^the \"([^\"]*)\" Dialog is hidden$")
    public void theDialogIsHidden(String dialog) {
        dialogVisable(dialog,false);
    }    

    @Then("^the \"([^\"]*)\" Dialog is displayed$")
    public void theDialogIsDisplayed(String dialog) {
        dialogVisable(dialog,true);
    }
/**/ 
}
