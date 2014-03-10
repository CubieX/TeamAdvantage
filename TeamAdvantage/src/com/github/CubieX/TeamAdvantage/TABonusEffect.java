package com.github.CubieX.TeamAdvantage;

public class TABonusEffect
{
   private String name = null;
   private String descr = null;
   private String category = null;
   private int price = 0;
   private int durationSeconds = 0; // CAUTION: duration will always stored in seconds, regardless of time unit in config

   public TABonusEffect(String name, String descr, String category)
   {
      this.name = name;
      this.descr = descr;
      this.category = category;
   }

   public String getName()
   {
      return (name);
   }

   public String getDescription()
   {
      return (descr);
   }

   public String getCategory()
   {
      return (category);
   }

   public void setPrice(int price)
   {
      if(price > 0)
      {
         this.price = price;
      }
   }

   public int getPrice()
   {
      return (price);
   }

   public void setDuration(int durationSeconds)
   {
      if(durationSeconds > 0)
      {
         this.durationSeconds = durationSeconds;
      }
   }

   public int getDuration()
   {
      return (durationSeconds);
   }
}
