package com.scalakata

import org.specs2._

class EvalSpecs extends Specification with EvalSetup { 
  def is = s2"""
    Eval Specifications
      The Scala Compiler
        displays info/warning/errors         $reports
        show type infered type at position   $typeInferance
        autocompletes
          scope                              $autocompleteScope
          member                             $autocompleteMembers

      The Runtime Module
        classloader
          retreive Instrumented class
            in any package                   $loasClassAnyPackage
          allow working on multiple packages $multiPackage
        runtime errors                       $runtimeErrors
        paradise crash                       $paradiseCrash

      The Security Module
        via security manager
          disallows stoping the jvm          $stopingJVM
          disallow exhausting resources      $resourcesExhaustion
          restrict reflection                $limitedReflection
        disallows non termination            $timeout
  """

  def reports = {
    eval("err").complilationInfos ==== Map(Error -> List(CompilationInfo("not found: value err", Some(RangePosition(56,56,56)) )))
  }
  def typeInferance = { 
    typeAt("List(1).reverse", 15).map(_.tpe) ==== Some("List[Int]")
  }
  def autocompleteScope = {
    autocomplete("", 0).map(_.name).contains("assert")
  }
  def autocompleteMembers = {
    autocomplete("List(1).", 8).map(_.name).contains("map")
  }
  def loadClassEmptyPackage = pending
  def loasClassAnyPackage = pending
  def multiPackage = {
    val before = 
      """|package a {
         |  package b {
         |    class X { def z = 42 } 
         |  }
         |  object V extends b.X
         |}""".stripMargin

    eval2(before, "a.V.z").runtimeError ==== None
  }
  def runtimeErrors = {
    eval("1 / 0").runtimeError ==== Some(RuntimeError("java.lang.ArithmeticException: / by zero", Some(3)))
  }
  def paradiseCrash = {
    ! eval("val a: String").complilationInfos(Error).
      exists(_.message.contains("exception during macro expansion"))
  }
  def stopingJVM = {
    // eval("System.exit(0)").runtimeError ==== Some(RuntimeError(
    //   "java.security.AccessControlException: access denied (\"java.lang.RuntimePermission\" \"exitVM.0\")",
    //   Some(3)
    // ))
    pending // classloader issues ?
  }

  def resourcesExhaustion = {
    // disallow creating a lot of threads / using a lot of memory
    pending
  }
  def limitedReflection = {
    // allow reflection from akka libs
    // but disallow in user space
    pending
  }
  def timeout = {
   eval("while(true){}").timeout ==== true
  }
}