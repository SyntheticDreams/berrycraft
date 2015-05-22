/*
 * BerryCraft.java
 *
 * © Synthetic Dreams, 2010
 * Confidential and proprietary.
 */

package com.synthdreams;

import net.rim.device.api.ui.UiApplication;


public class BerryCraft extends UiApplication {

   
   public static void main(String[] args) {
      BerryCraft berryCraft = new BerryCraft();
      
      berryCraft.enterEventDispatcher();
        
   }
    
   public BerryCraft() {

      pushScreen(new Menu());
      
   }
} 
