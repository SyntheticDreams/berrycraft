package com.synthdreams;

import java.io.IOException;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;

class Menu extends MainScreen {
   private ServerReceiver _serverReceiver;
   private static LabelField _titleField;
   private static EditField _outputField;
   private static EditField _userField;
   private static EditField _inputField;
   private static HorizontalFieldManager _outputManager;
   private static HorizontalFieldManager _userManager;
   private static HorizontalFieldManager _inputManager;
   
   private class ServerReceiver extends Thread {
      ServerReceiver() {
         start();
      }
   
      public void run() {
         int retVal;
         
         while (MCIO.getConnected() == true) {
            retVal = MCIO.receiveCommand();
            if (retVal != MCIO.RETURN_OK) { 
               Menu.debugMessage(MCIO.getErrorMessage(retVal), 0); 
               MCIO.disconnect();
               return;
            }
         }
      }
   }

   static {
      _titleField = new LabelField();
      _outputField = new EditField(Field.USE_ALL_WIDTH | Field.READONLY);      
      _userField = new EditField(Field.USE_ALL_WIDTH);
      _inputField = new EditField(Field.USE_ALL_WIDTH) {
         protected boolean keyChar(char character, int status, int time) {
            if (character == Characters.ENTER) {
               
               /*try {
                  MCIO.sendChat(getText());
                  setText("");
               }
               catch (IOException e) {}*/                 
               return true;
            }
            return super.keyChar(character, status, time);
         }
      };
      
      _outputManager = new HorizontalFieldManager(HorizontalFieldManager.VERTICAL_SCROLL | HorizontalFieldManager.VERTICAL_SCROLLBAR) {
         protected void sublayout(int width, int height) {
            super.sublayout((int)(Display.getWidth() * 1), height);
            setExtent((int)(Display.getWidth() * 1), Display.getHeight() - getFont().getHeight() * 2 - 2);
         }         
      };

      _userManager = new HorizontalFieldManager(HorizontalFieldManager.VERTICAL_SCROLL | HorizontalFieldManager.VERTICAL_SCROLLBAR) {
         protected void sublayout(int width, int height) {
            super.sublayout((int)(Display.getWidth() * 0.33), height);
            setExtent((int)(Display.getWidth() * 0.33), Display.getHeight() - getFont().getHeight() * 2 - 2);
         }         
      };
      
      _inputManager = new HorizontalFieldManager();
      
      _outputManager.add(_outputField);
      _userManager.add(_userField);
      _inputManager.add(_inputField);
   }
   
   public Menu()
   {
      super(NO_VERTICAL_SCROLL);

      final Settings settings = new Settings();      
      MenuItem connectItem;
      MenuItem disconnectItem;
      MenuItem settingsItem;
      HorizontalFieldManager topManager;     

      connectItem = new MenuItem("Connect", 10, 0) {
         public void run() {
            int retVal;

            // Connect to server
            retVal = MCIO.connect();
            if (retVal != MCIO.RETURN_OK) { Menu.debugMessage(MCIO.getErrorMessage(retVal), 0); return;}
            
            // Send and receive handshake
            retVal = MCIO.sendHandshake();
            if (retVal != MCIO.RETURN_OK) { Menu.debugMessage(MCIO.getErrorMessage(retVal), 0); return;}
            retVal = MCIO.receiveCommand();
            if (retVal != MCIO.RETURN_OK) { Menu.debugMessage(MCIO.getErrorMessage(retVal), 0); return;}
                        
            // Send login
            retVal = MCIO.sendLoginRequest();
            if (retVal != MCIO.RETURN_OK) { Menu.debugMessage(MCIO.getErrorMessage(retVal), 0); return;}            
            retVal = MCIO.receiveCommand();
            if (retVal != MCIO.RETURN_OK) { Menu.debugMessage(MCIO.getErrorMessage(retVal), 0); return;}
            
            if (MCIO.getConnected()) {
               // If sucessful connection, start receiver
               Menu.debugMessage("Connected!", 0);
               _serverReceiver = new ServerReceiver();
            }
            else {
               Menu.debugMessage("Not Connected!", 0);
            }
         }
      };
      
      disconnectItem = new MenuItem("Disconnect", 10, 0) {
         public void run() {
            int retVal;
            
            retVal = MCIO.disconnect();
            if (retVal != MCIO.RETURN_OK) { Menu.debugMessage(MCIO.getErrorMessage(retVal), 0); return;}
            
            Menu.debugMessage("Logged Out", 0);
         }
      };

      settingsItem = new MenuItem("Settings", 20, 0) {
         public void run() {
            getUiEngine().pushScreen(settings);
         }
      };     
      
      topManager = new HorizontalFieldManager(HorizontalFieldManager.NO_HORIZONTAL_SCROLL | HorizontalFieldManager.NO_VERTICAL_SCROLL) {
         protected void sublayout(int width, int height) {
            super.sublayout(Display.getWidth(), Display.getHeight() - getFont().getHeight() * 2 - 2);
            setExtent(Display.getWidth(), Display.getHeight() - getFont().getHeight() * 2 - 2);
         }
         
         protected void paint(Graphics g) {
            int divLine1, divLine2;
            
            super.paint(g);
            divLine1 = Display.getHeight() - getFont().getHeight() * 2 - 3;
            divLine2 = (int)(Display.getWidth() * 1);
            g.drawLine(0,divLine1, getWidth(), divLine1);
            //g.drawLine(divLine2, 0, divLine2, divLine1);
         }
      };
      
      // Load Settings
      Settings.loadSettings();
      
      // Set Title
      updateTime();
      setTitle(_titleField);
      
      // Set Menu Items
      addMenuItem(connectItem);
      addMenuItem(disconnectItem);
      addMenuItem(settingsItem);
      
      // Set Managers and Fields
      topManager.add(_outputManager);
      //topManager.add(_userManager);
      add(topManager);
      add(_inputManager);
      
      _inputField.setFocus();
   }
      
   public static void updateTime() {
      _titleField.setText("BerryCraft Chat | Game Time: " + Game.time / 10);
   }
   
   public static void debugMessage(String passString, int passLevel) {
      synchronized (Application.getEventLock()) {
         if (passLevel < 1) {
            _outputField.setText(_outputField.getText() + "\n" + passString);
            _outputField.setEditable(true);
            _outputField.setFocus();
            _outputField.setEditable(false);
            _inputField.setFocus();
         }
      }
   }
}
