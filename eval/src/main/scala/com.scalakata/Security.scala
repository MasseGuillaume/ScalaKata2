package com.scalakata

import java.security._
import java.io._


class Secured(security: Boolean) {
  class ScalaKataSecurityPolicy extends Policy {
    val read1 = new java.io.FilePermission("../-", "read")
    val read2 = new java.io.FilePermission("../.", "read")
    val other = new java.net.NetPermission("specifyStreamHandler")

    override def implies(domain: ProtectionDomain, permission: Permission) = {
      List(read1, read2, other).exists(_.implies(permission)) ||
      Thread.currentThread().getStackTrace().find(_.getFileName == "(inline)").isEmpty
    }
  }
  def apply[T](f: => T): T = {
    if(security) {
      Policy.setPolicy(new ScalaKataSecurityPolicy)
      System.setSecurityManager(new SecurityManager)
    }
    val t = f
    if(security) {
      Policy.setPolicy(null)
      System.setSecurityManager(null)
    }
    t
  }
}
