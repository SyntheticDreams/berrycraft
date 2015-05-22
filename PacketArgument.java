package com.synthdreams;

public class PacketArgument {
   
   public PacketArgument() {
      // Initialize arguments to unused
      argType = MCIO.TYPE_NONE;
      
      valBoolean = false;
      valByte = 0;
      valShort = 0;
      valInt = 0;
      valLong = 0;
      valFloat = 0;
      valDouble = 0;
      
      valString = "";
   }
   
   public int argType; // Type of data this argument stores 
   
   // Actual argument value
   public boolean valBoolean;
   public byte valByte;
   public short valShort;
   public int valInt;
   public long valLong;
   public float valFloat;
   public double valDouble;
   public String valString;
}
