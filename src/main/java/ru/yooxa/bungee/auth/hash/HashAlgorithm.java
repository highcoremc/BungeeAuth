package ru.yooxa.bungee.auth.hash;

public enum HashAlgorithm
{
  SHA256(ru.yooxa.bungee.auth.hash.methods.SHA256.class),
  MD5(ru.yooxa.bungee.auth.hash.methods.MD5.class),
  XAUTH(ru.yooxa.bungee.auth.hash.methods.MD5.class);

  Class cls;


  HashAlgorithm(Class cls) { this.cls = cls; }


  
  public Class getclass() { return this.cls; }
}


