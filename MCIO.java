package com.synthdreams;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.HttpsConnection;
import javax.microedition.io.StreamConnection;

import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.io.transport.*;

public class MCIO {
   public static final int ARGUMENTS_MAX = 10;
   
   public static final int TYPE_NONE = 0;
   public static final int TYPE_BOOLEAN = 1;
   public static final int TYPE_BYTE = 2;
   public static final int TYPE_SHORT = 3;
   public static final int TYPE_INT = 4;
   public static final int TYPE_LONG = 5;
   public static final int TYPE_FLOAT = 6;
   public static final int TYPE_DOUBLE = 7;
   public static final int TYPE_STRING = 8;
      
   public static final byte ID_KEEP_ALIVE = 0x00;
   public static final byte ID_LOGIN_REQUEST = 0x01;
   public static final byte ID_HANDSHAKE = 0x02;
   public static final byte ID_CHAT_MESSAGE = 0x03;
   public static final byte ID_TIME_UPDATE = 0x04;
   public static final byte ID_ENTITY_EQUIPMENT = 0x05;
   public static final byte ID_SPAWN_POSITION = 0x06;
   public static final byte ID_USE_ENTITY = 0x07;
   
   public static final int RETURN_OK = 0;
   public static final int RETURN_BADCONNECT = 1;
   public static final int RETURN_BADDISCONNECT = 2;
   public static final int RETURN_BADAUTH = 3;
   public static final int RETURN_BADJOIN = 4;
   public static final int RETURN_PACKETERROR = 5;
   
   private static PacketArgument[][] _argArray; 
   private static String[] _nameArray;
   private static StreamConnection _serverStream;
   private static DataOutputStream _serverWriter;
   private static DataInputStream _serverReader;
   private static String _sessionID;
   private static String _serverHash;
   private static boolean _connected;
   private static int _aliveTicks;
   
   static {
      _sessionID = "";
      _serverHash = "";
      _connected = false;
      _aliveTicks = 0;
      _nameArray = new String[256];
      _argArray = new PacketArgument[256][ARGUMENTS_MAX];
      
      for (int lcv = 0 ; lcv < 256 ; lcv++) {
         for (int lcv2 = 0 ; lcv2 < ARGUMENTS_MAX ; lcv2++) {
            _argArray[lcv][lcv2] = new PacketArgument();
         }
      }
      
      _nameArray[ID_KEEP_ALIVE] = "Keep Alive";
      _nameArray[ID_LOGIN_REQUEST] = "Login Request";
      _nameArray[ID_HANDSHAKE] = "Handshake";
      _nameArray[ID_CHAT_MESSAGE] = "Chat Message";
      _nameArray[ID_TIME_UPDATE] = "Time Update";
      _nameArray[ID_ENTITY_EQUIPMENT] = "Entity Equipment";
      _nameArray[ID_SPAWN_POSITION] = "Spawn Position";
      _nameArray[ID_USE_ENTITY] = "Use Entity";
      
      _argArray[ID_KEEP_ALIVE][0].argType = TYPE_INT; 
      _argArray[ID_LOGIN_REQUEST][0].argType = TYPE_INT;
      _argArray[ID_LOGIN_REQUEST][1].argType = TYPE_STRING;
      _argArray[ID_LOGIN_REQUEST][2].argType = TYPE_STRING;
      _argArray[ID_LOGIN_REQUEST][3].argType = TYPE_INT;
      _argArray[ID_LOGIN_REQUEST][4].argType = TYPE_INT;
      _argArray[ID_LOGIN_REQUEST][5].argType = TYPE_BYTE;
      _argArray[ID_LOGIN_REQUEST][6].argType = TYPE_BYTE;
      _argArray[ID_LOGIN_REQUEST][7].argType = TYPE_BYTE;
      _argArray[ID_HANDSHAKE][0].argType = TYPE_STRING;
      _argArray[ID_CHAT_MESSAGE][0].argType = TYPE_STRING;
      _argArray[ID_TIME_UPDATE][0].argType = TYPE_LONG;
      _argArray[ID_ENTITY_EQUIPMENT][0].argType = TYPE_INT;
      _argArray[ID_ENTITY_EQUIPMENT][1].argType = TYPE_SHORT;
      _argArray[ID_ENTITY_EQUIPMENT][2].argType = TYPE_SHORT;
      _argArray[ID_ENTITY_EQUIPMENT][3].argType = TYPE_SHORT;
      _argArray[ID_SPAWN_POSITION][0].argType = TYPE_INT;
      _argArray[ID_SPAWN_POSITION][1].argType = TYPE_INT;
      _argArray[ID_SPAWN_POSITION][2].argType = TYPE_INT;
      _argArray[ID_USE_ENTITY][0].argType = TYPE_INT;      
      _argArray[ID_USE_ENTITY][1].argType = TYPE_INT;
      _argArray[ID_USE_ENTITY][2].argType = TYPE_BOOLEAN;
   }

   private static String httpRequest(String passURL) throws IOException {
      ConnectionFactory tempFactory;
      ConnectionDescriptor tempDescriptor;
      HttpConnection mcConnection;
      byte[] mcResponseBytes;
      InputStream inStream;

      mcResponseBytes = new byte[8192];      
      
      // Setup Connection
      tempFactory = new ConnectionFactory();
      tempDescriptor = tempFactory.getConnection(passURL);
      
      mcConnection = (HttpConnection) tempDescriptor.getConnection();
      mcConnection.setRequestProperty("User-Agent", "Java/Blackberry");
      
      // Send Request
      mcConnection.getResponseCode();
         
      // Get Response
      inStream = mcConnection.openInputStream();
      inStream.read(mcResponseBytes);
      return new String(mcResponseBytes);      
   }  
   
   private static String httpsRequest(String passURL, URLEncodedPostData passParams) throws IOException {
      ConnectionFactory tempFactory;
      ConnectionDescriptor tempDescriptor;
      HttpsConnection mcConnection;
      byte[] mcPostBytes;
      byte[] mcResponseBytes;
      InputStream inStream;
      OutputStream outStream;

      mcResponseBytes = new byte[8192];      
      mcPostBytes = passParams.getBytes();
      
      // Setup Connection
      tempFactory = new ConnectionFactory();
      tempDescriptor = tempFactory.getConnection(passURL);
      
      mcConnection = (HttpsConnection) tempDescriptor.getConnection();
      mcConnection.setRequestMethod(HttpsConnection.POST);
      mcConnection.setRequestProperty("User-Agent", "Java/Blackberry");
      mcConnection.setRequestProperty("Content-Language", "en-US");
      mcConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      mcConnection.setRequestProperty("Content-Length", (new Integer(mcPostBytes.length)).toString());
      
      // Send Request
      outStream = mcConnection.openOutputStream();
      outStream.write(mcPostBytes);
      outStream.flush();
      mcConnection.getResponseCode();
      
      // Get Response
      inStream = mcConnection.openInputStream();
      inStream.read(mcResponseBytes);
      return new String(mcResponseBytes);      
   }
   
   private static boolean requestSession() {
      URLEncodedPostData mcPostData;
      String mcResponseString;
      
      mcPostData = new URLEncodedPostData(URLEncodedPostData.DEFAULT_CHARSET, false);
      mcPostData.append("user", Settings.getName());
      mcPostData.append("password", Settings.getPassword());
      mcPostData.append("version", "13");
      
      try {
         mcResponseString = httpsRequest("https://login.minecraft.net", mcPostData);
      }
      catch (IOException e) {
         return false;
      }
      
      // Return false if bad login
      if (mcResponseString.lastIndexOf(':') == -1) return false;     
      
      // Set session ID
      _sessionID = mcResponseString.substring(mcResponseString.lastIndexOf(':')+1).trim();
      Menu.debugMessage(_sessionID, 0);
      return true;
   }
   
   private static boolean requestJoin() {
      String joinURL;
      String mcResponseString;
      
      joinURL = "http://session.minecraft.net/game/joinserver.jsp";
      joinURL += "?user=" + Settings.getName();
      joinURL += "&sessionId=" + _sessionID;
      joinURL += "&serverId=" + _serverHash;
      
      Menu.debugMessage("About to send: " + joinURL, 0);
      
      try {
         mcResponseString = httpRequest(joinURL);
      }
      catch (IOException e) {
         return false;
      }
            
      return (mcResponseString.startsWith("OK"));
   }

   public static String getErrorMessage(int passError) {
      switch (passError) {
         case RETURN_OK: return "OK";
         case RETURN_BADCONNECT: return "Unable to connect to game server.";  
         case RETURN_BADDISCONNECT: return "Unable to disconnect from game server.";
         case RETURN_BADAUTH: return "Bad Minecraft.net username or password.";
         case RETURN_BADJOIN: return "Unable to join Minecraft.net.";
         case RETURN_PACKETERROR: return "Packet error.";
         default: return "Unknown Error.";
      }
   }
   
   public static int connect() {
      ConnectionFactory tempFactory;
      ConnectionDescriptor tempDescriptor;

      try {
         // Request Session ID from Minecraft.net
         if (requestSession() == false) return RETURN_BADAUTH;
         
         // Make connection to Minecraft game server
         tempFactory = new ConnectionFactory();
         tempDescriptor = tempFactory.getConnection("socket://" + Settings.getServer());
         _serverStream = (StreamConnection)tempDescriptor.getConnection();
         _serverWriter = new DataOutputStream(_serverStream.openDataOutputStream());
         _serverReader = new DataInputStream(_serverStream.openDataInputStream());
         _connected = true;
      }
      catch (IOException e) {
         return RETURN_BADCONNECT;
      }
      
      return RETURN_OK;
   }
   
   public static int disconnect() {
      _connected = false;
      
      try {
         _serverWriter.close();
         _serverReader.close();
         _serverStream.close();
      }
      catch (IOException e) {
         return RETURN_BADDISCONNECT;
      }
      
      return RETURN_OK;
   }
   
   public static boolean getConnected() {
      return _connected;
   }

  
   public static int receiveCommand() {
      byte packetCode;
      short tempLength;
      byte[] tempArray;

      try {     
         // Read in packet ID code
         packetCode = _serverReader.readByte();
         Menu.debugMessage("GOT PACKET CODE", 0);
         // Read in arguments
         for (int lcv = 0 ; lcv < ARGUMENTS_MAX ; lcv++) {
            // If we're done with arguments, exit
            if (_argArray[packetCode][lcv].argType == TYPE_NONE) break;
            Menu.debugMessage("READING ARG", 0);
            switch (_argArray[packetCode][lcv].argType) {
               case TYPE_BOOLEAN: _argArray[packetCode][lcv].valBoolean = _serverReader.readBoolean(); break;
               case TYPE_BYTE: _argArray[packetCode][lcv].valByte = _serverReader.readByte(); break;
               case TYPE_SHORT: _argArray[packetCode][lcv].valShort = _serverReader.readShort(); break;
               case TYPE_INT: _argArray[packetCode][lcv].valInt = _serverReader.readInt(); break;
               case TYPE_LONG: _argArray[packetCode][lcv].valLong = _serverReader.readLong(); break;
               case TYPE_FLOAT: _argArray[packetCode][lcv].valFloat = _serverReader.readFloat(); break;
               case TYPE_DOUBLE: _argArray[packetCode][lcv].valDouble = _serverReader.readDouble(); break;
               case TYPE_STRING: tempLength = _serverReader.readShort();
                                 tempArray = new byte[tempLength * 2];
                                 _serverReader.readFully(tempArray, 0, tempArray.length);
                                 _argArray[packetCode][lcv].valString = new String(tempArray, 0, tempArray.length, "UTF-16BE"); 
                                 break;
            }
         }
      }
      catch (IOException e) {
         return RETURN_PACKETERROR;
      }

      Menu.debugMessage("Received '" + _nameArray[packetCode] + "'", 0);
      
      switch (packetCode) {
         case ID_HANDSHAKE: return processHandshake();
         case ID_LOGIN_REQUEST: return processLoginRequest();
      }
      
      return RETURN_OK;
   }

   public static int sendCommand(byte passCode) {
      try {
         // Send packet ID code
         _serverWriter.writeByte(passCode);
         // Send arguments
         for (int lcv = 0 ; lcv < ARGUMENTS_MAX ; lcv++) {
            // If we're done with arguments, exit
            if (_argArray[passCode][lcv].argType == TYPE_NONE) break;
            
            Menu.debugMessage("Just sent code, now sending arg", 0);
            
            switch (_argArray[passCode][lcv].argType) {
               case TYPE_BOOLEAN: _serverWriter.writeBoolean(_argArray[passCode][lcv].valBoolean); break;
               case TYPE_BYTE: _serverWriter.writeByte(_argArray[passCode][lcv].valByte); break;
               case TYPE_SHORT: _serverWriter.writeShort(_argArray[passCode][lcv].valShort); break;
               case TYPE_INT: _serverWriter.writeInt(_argArray[passCode][lcv].valInt); break;
               case TYPE_LONG: _serverWriter.writeLong(_argArray[passCode][lcv].valLong); break;
               case TYPE_FLOAT: _serverWriter.writeFloat(_argArray[passCode][lcv].valFloat); break;
               case TYPE_DOUBLE: _serverWriter.writeDouble(_argArray[passCode][lcv].valDouble); break;
               case TYPE_STRING: _serverWriter.writeShort(_argArray[passCode][lcv].valString.length());
                                 _serverWriter.write(_argArray[passCode][lcv].valString.getBytes("UTF-16BE")); 
                                 break;
            }
         }
      }
      catch (IOException e) {
         return RETURN_PACKETERROR;
      }
      
      return RETURN_OK;
   }
   
   public static int sendHandshake() {
      _argArray[ID_HANDSHAKE][0].valString = Settings.getName() + ";" + Settings.getServer();
      return sendCommand(ID_HANDSHAKE);
   }
   
   public static int processHandshake() {
      Menu.debugMessage("In handshake, got: " + _argArray[ID_HANDSHAKE][0].valString, 0);
      _serverHash = _argArray[ID_HANDSHAKE][0].valString;
      
      // Join Minecraft.net servers now that we have server hash
      if (requestJoin() == false) return RETURN_BADJOIN;
      
      return RETURN_OK;
   }

   public static int sendLoginRequest() {
      _argArray[ID_LOGIN_REQUEST][0].valInt = 29;
      _argArray[ID_LOGIN_REQUEST][1].valString = Settings.getName();
      return sendCommand(ID_LOGIN_REQUEST);
   }
   
   public static int processLoginRequest() {
      _connected = true;
      
      return RETURN_OK;
   }
}

