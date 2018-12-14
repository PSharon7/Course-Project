(* module Syntax: syntax trees and associated support functions *)

open Support.Pervasive
open Support.Error

(* Data type definitions *)
type ty =
    TyRecord of (string * ty) list
  | TyTop
  | TyVar of int * int
  | TyArr of ty * ty
  | TyBool
  | TyNat

type term =
    TmTrue of info
  | TmFalse of info
  | TmIf of info * term * term * term
  | TmZero of info
  | TmSucc of info * term
  | TmPred of info * term
  | TmIsZero of info * term
  | TmVar of info * int * int
  | TmAbs of info * string * ty * term
  | TmApp of info * term * term
  | TmRecord of info * (string * term) list
  | TmProj of info * term * string

type binding = 
    NameBind 
  | TyVarBind
  | VarBind of ty
  | TmAbbBind of term * (ty option)
  | TyAbbBind of ty

type command =
  | Eval of info * term
  | Bind of info * string * binding

(* Context *)
type context

val emptyContext : context 
val cxtLength : context -> int
val addBinding : context -> string -> binding -> context
val addName: context -> string -> context
val getBinding : info -> context -> int -> binding
val index2Name : info -> context -> int -> string
val name2Index : info -> context -> string -> int
val isNameBound : context -> string -> bool
val getTypeFromContext : info -> context -> int -> ty

(* Shifting and Substitution *)
val termShifting : int -> term -> term
val termSubstTop : term -> term -> term
val typeShifting : int -> ty -> ty
val typeSubstTop: ty -> ty -> ty
val typeTermSubstTop: ty -> term -> term

(* Printing *)
val printtm: context -> term -> unit
val printtm_ATerm: bool -> context -> term -> unit
val printty : context -> ty -> unit
val printBinding : context -> binding -> unit

(* Misc *)
val tmInfo: term -> info

