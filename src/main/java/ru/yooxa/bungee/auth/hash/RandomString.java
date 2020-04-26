package ru.yooxa.bungee.auth.hash;

import java.util.Random;

public class RandomString
{
  private static final char[] chars = new char[36];

  
  static  {
    for (int idx = 0; idx < 10; idx++) {
      chars[idx] = (char)(48 + idx);
    }
    
    for (int idx = 10; idx < 36; idx++) {
      chars[idx] = (char)(97 + idx - 10);
    }
  }

  
  private final Random random = new Random();
  private final char[] buf;
  
  public RandomString(int length) {
    if (length < 1) {
      throw new IllegalArgumentException("length < 1: " + length);
    }
    this.buf = new char[length];
  }

  
  public String nextString() {
    for (int idx = 0; idx < this.buf.length; idx++) {
      this.buf[idx] = chars[this.random.nextInt(chars.length)];
    }
    
    return new String(this.buf);
  }
}


