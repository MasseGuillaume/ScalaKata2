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
            in empty package                 $loadClassEmptyPackage
            in any package                   $loasClassAnyPackage
        runtime errors                       $runtimeErrors

      The Security Module
        via security manager
          disallows stoping the jvm          $stopingJVM
          disallow exhausting resources      $resourcesExhaustion
          restrict reflection                $limitedReflection
        disallows non termination            $timeout
  """

  def reports = {
    eval("err").complilationInfos ==== Map(Error -> List(CompilationInfo("not found: value err", None)))
  }
  def typeInferance = { 
    // typeAt("List(1).reverse", i)
    // wont work with macro paradise
    pending 
  }
  def autocompleteScope = {
    autocomplete("", 0).map(_.name).contains("assert")
  }
  def autocompleteMembers = {
    autocomplete("List(1).", 8).map(_.name).contains("assert")
  }
  def loadClassEmptyPackage = pending
  def loasClassAnyPackage = pending
  def runtimeErrors = {
    eval("1 / 0").runtimeError ==== Some(RuntimeError("java.lang.ArithmeticException: / by zero", Some(3)))
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