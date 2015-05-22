package com.synthdreams;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

class Settings extends MainScreen {
   private static EditField _serverField;
   private static EditField _nameField;
   private static EditField _passwordField;

   static {
      _serverField = new EditField("Server: ", "");       
      _nameField = new EditField("Username: ", "");      
      _passwordField = new EditField("Password: ", "");
   }
   
   public Settings()
   {
      super(NO_VERTICAL_SCROLL);

      LabelField titleField;
      MenuItem saveItem;
      MenuItem cancelItem;
      

      saveItem = new MenuItem("Save Settings", 10, 0) {
         public void run() {
            saveSettings();
            getScreen().close();
         }
      };
      
      cancelItem = new MenuItem("Cancel", 10, 0) {
         public void run() {
            getScreen().close();
         }
      };

      // Set Title
      titleField = new LabelField("BerryCraft Chat Settings");
      setTitle(titleField);
      
      // Set Menu Items
      addMenuItem(saveItem);
      addMenuItem(cancelItem);
      
      // Set Fields
      add(_serverField);
      add(_nameField);
      add(_passwordField);
   }

   static public void saveSettings() {
      // com.synthdreams.BerryCraftChat.Server
      // com.synthdreams.BerryCraftChat.Name
      // com.synthdreams.BerryCraftChat.Password
      long serverKey = 0x13619b5447826f54L;
      long nameKey = 0x5b30495a11ee06a5L;
      long passwordKey = 0x7915f108c1623307L; 
      PersistentObject persistObject;
      
      persistObject = PersistentStore.getPersistentObject(serverKey);
      persistObject.setContents(getServer());
      persistObject = PersistentStore.getPersistentObject(nameKey);
      persistObject.setContents(getName());
      persistObject = PersistentStore.getPersistentObject(passwordKey);
      persistObject.setContents(getPassword());
   }
   
   static public void loadSettings() {
      // com.synthdreams.BerryCraftChat.Server
      // com.synthdreams.BerryCraftChat.Name
      // com.synthdreams.BerryCraftChat.Password
      long serverKey = 0x13619b5447826f54L;
      long nameKey = 0x5b30495a11ee06a5L;
      long passwordKey = 0x7915f108c1623307L; 
      PersistentObject persistObject;
      
      persistObject = PersistentStore.getPersistentObject(serverKey);
      if (persistObject.getContents() != null) {
         _serverField.setText(persistObject.getContents().toString());
      }
      
      persistObject = PersistentStore.getPersistentObject(nameKey);
      if (persistObject.getContents() != null) {
         _nameField.setText(persistObject.getContents().toString());
      }
      
      persistObject = PersistentStore.getPersistentObject(passwordKey);
      if (persistObject.getContents() != null) {
         _passwordField.setText(persistObject.getContents().toString());
      }
   } 
   
   public static String getServer() {
      if (_serverField.getText().indexOf(":") > -1) {
         return _serverField.getText();
      }
      else {
         return _serverField.getText() + ":25565";
      }
   }
   
   public static String getName() {
      return _nameField.getText();
   }

   public static String getPassword() {
      return _passwordField.getText();
   }   
}
