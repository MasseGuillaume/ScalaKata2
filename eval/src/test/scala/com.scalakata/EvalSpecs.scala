package com.scalakata

import org.specs2._

class EvalSpecs extends Specification { def is = s2"""
  Eval Specifications
    The Scala Compiler
      displays info/warning/errors         $reports
      show type infered type at position   $typeAt
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

  def reports = pending
  def typeAt = pending
  def autocompleteScope = pending
  def autocompleteMembers = pending
  def loadClassEmptyPackage = pending
  def loasClassAnyPackage = pending
  def runtimeErrors = pending
  def stopingJVM = pending
  def resourcesExhaustion = pending
  def limitedReflection = pending
  def timeout = pending
}