package com.theneru.knocker.util;

public enum KnockerLook {
   DEFAULT,
   ANGRY,
   THEFT,
   PYRO,
   PSY;

   public static KnockerLook of(int id) {
      KnockerLook[] all = values();
      return id >= 0 && id < all.length ? all[id] : DEFAULT;
   }

   // $FF: synthetic method
   private static KnockerLook[] $values() {
      return new KnockerLook[]{DEFAULT, ANGRY, THEFT, PYRO, PSY};
   }
}
