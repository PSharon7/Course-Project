(* module Core

   Core typechecking and evaluation functions
*)

open Syntax
open Support.Error

val eval : context -> term -> term 
val typeOf : context -> term -> ty
val evalBinding : context -> binding -> binding 
val typeEqv : context -> ty -> ty -> bool
val simplifyTy : context -> ty -> ty
val subType : context -> ty -> ty -> bool