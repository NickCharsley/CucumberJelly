/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cucumber.api.jelly.glue;

import cucumber.api.java.en.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.netbeans.jellytools.actions.ActionNoBlock;

/**
 *
 * @author nick
 */
public class ActionNoBlockSteps {
/**/
    @When("^I click the \"([^\"]*)\" menu$")
    public void clickTheMenu(String menu) {
        new ActionNoBlock(menu, null).performMenu();
    }
/**/    
}
